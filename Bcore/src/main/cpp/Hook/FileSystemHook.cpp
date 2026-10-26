#include "FileSystemHook.h"
#include "Log.h"
#include "IO.h"
#include "BoxCore.h"
#include "xdl.h"
#include "Dobby/dobby.h"

#include <sys/stat.h>
#include <fcntl.h>
#include <stdarg.h>
#include <cstring>
#include <string>
#include <errno.h>
#include <unistd.h>
#include <sys/syscall.h>

#ifndef __NR_memfd_create
#if defined(__aarch64__)
#define __NR_memfd_create 279
#elif defined(__arm__)
#define __NR_memfd_create 385
#else
#define __NR_memfd_create 319
#endif
#endif

#ifndef MFD_CLOEXEC
#define MFD_CLOEXEC 0x0001U
#endif

static int (*orig_openat)(int dirfd, const char *pathname, int flags, ...) = nullptr;
static int (*orig_open)(const char *pathname, int flags, ...) = nullptr;
static int (*orig_faccessat)(int dirfd, const char *pathname, int mode, int flags) = nullptr;
static int (*orig_access)(const char *pathname, int mode) = nullptr;
static int (*orig_newfstatat)(int dirfd, const char *pathname, struct stat *buf, int flags) = nullptr;
static ssize_t (*orig_readlinkat)(int dirfd, const char *pathname, char *buf, size_t bufsiz) = nullptr;
static ssize_t (*orig_readlink)(const char *pathname, char *buf, size_t bufsiz) = nullptr;

static inline bool isValidUserString(const char *ptr) {
    if (!ptr) return false;
    uintptr_t uptr = (uintptr_t)ptr;
#if defined(__aarch64__)
    uintptr_t untagged = uptr & 0x00FFFFFFFFFFFFFFULL;
    if (untagged < 0x1000 || untagged >= 0x0000800000000000ULL) return false;
#elif defined(__arm__)
    if (uptr < 0x1000 || uptr >= 0xC0000000UL) return false;
#endif
    return true;
}

static bool is_proc_path(const char *path, const char *name) {
    if (!path) return false;
    if (strncmp(path, "/proc/self/", 11) == 0 && strcmp(path + 11, name) == 0) {
        return true;
    }
    if (strncmp(path, "/proc/", 6) == 0) {
        const char *p = path + 6;
        while (*p >= '0' && *p <= '9') p++;
        if (*p == '/' && strcmp(p + 1, name) == 0) {
            char pid_str[32];
            snprintf(pid_str, sizeof(pid_str), "/proc/%d/%s", getpid(), name);
            if (strcmp(path, pid_str) == 0) {
                return true;
            }
        }
    }
    return false;
}

static const char *sBlockedSuFiles[] = {
    "/system/xbin/su",
    "/system/bin/su",
    "/sbin/su",
    "/system/app/Superuser.apk",
    "/system/app/SuperSU.apk",
    "/system/xbin/daemonsu",
    "/system/xbin/sugote",
    "/data/local/xbin/su",
    "/data/local/bin/su",
    "/data/local/tmp/su",
    "/system/bin/magisk",
    "/system/xbin/magisk",
    "/sbin/magisk",
    "/data/adb/magisk",
    "/system/xposed.prop",
    "/system/framework/XposedBridge.jar",
    "/data/data/de.robv.android.xposed.installer",
    "/data/data/org.meowcat.edxposed.manager",
    "/data/data/top.canyie.dreamland.manager",
    nullptr
};

static bool is_blocked_root_file(const char *path) {
    if (!path) return false;
    for (int i = 0; sBlockedSuFiles[i]; ++i) {
        if (strcmp(path, sBlockedSuFiles[i]) == 0) {
            return true;
        }
    }
    return false;
}

static int create_in_memory_fd(const char *name) {
    int fd = syscall(__NR_memfd_create, name, MFD_CLOEXEC);
    if (fd >= 0) return fd;

    int pipefd[2];
    if (pipe2(pipefd, O_CLOEXEC) == 0) {
        close(pipefd[1]);
        return pipefd[0];
    }
    return -1;
}

static int create_cloaked_maps_fd() {
    int real_fd = orig_openat ? orig_openat(AT_FDCWD, "/proc/self/maps", O_RDONLY, 0)
                              : open("/proc/self/maps", O_RDONLY);
    if (real_fd < 0) return -1;

    int mem_fd = create_in_memory_fd("maps");
    if (mem_fd < 0) {
        close(real_fd);
        return -1;
    }

    const std::string &hostPkg = BoxCore::getHostPackage();
    const std::string &guestPkg = BoxCore::getGuestPackage();
    bool needReplaceHost = !hostPkg.empty() && !guestPkg.empty() && (hostPkg != guestPkg);

    char line[1024];
    size_t line_len = 0;
    char ch;
    while (read(real_fd, &ch, 1) == 1) {
        if (line_len < sizeof(line) - 1) {
            line[line_len++] = ch;
        }
        if (ch == '\n') {
            line[line_len] = '\0';
            line_len = 0;

            if (strstr(line, "blackbox") ||
                strstr(line, "libblackbox") ||
                strstr(line, "dobby") ||
                strstr(line, "sandhook") ||
                strstr(line, "libdobby")) {
                continue;
            }

            if (needReplaceHost && strstr(line, hostPkg.c_str())) {
                std::string s(line);
                size_t pos = 0;
                while ((pos = s.find(hostPkg, pos)) != std::string::npos) {
                    s.replace(pos, hostPkg.length(), guestPkg);
                    pos += guestPkg.length();
                }
                write(mem_fd, s.data(), s.length());
            } else {
                write(mem_fd, line, strlen(line));
            }
        }
    }
    close(real_fd);
    lseek(mem_fd, 0, SEEK_SET);
    return mem_fd;
}

static int create_cloaked_cmdline_fd() {
    int mem_fd = create_in_memory_fd("cmdline");
    if (mem_fd < 0) return -1;

    const std::string &procName = BoxCore::getProcessName();
    const std::string &guestPkg = BoxCore::getGuestPackage();
    const std::string &target = !procName.empty() ? procName : guestPkg;

    if (!target.empty()) {
        write(mem_fd, target.c_str(), target.length() + 1);
    } else {
        write(mem_fd, "\0", 1);
    }
    lseek(mem_fd, 0, SEEK_SET);
    return mem_fd;
}

static int create_cloaked_comm_fd() {
    int mem_fd = create_in_memory_fd("comm");
    if (mem_fd < 0) return -1;

    const std::string &procName = BoxCore::getProcessName();
    const std::string &guestPkg = BoxCore::getGuestPackage();
    const std::string &target = !procName.empty() ? procName : guestPkg;

    std::string comm = target.substr(0, 15) + "\n";
    write(mem_fd, comm.c_str(), comm.length());
    lseek(mem_fd, 0, SEEK_SET);
    return mem_fd;
}

static int create_cloaked_status_fd() {
    int real_fd = orig_openat ? orig_openat(AT_FDCWD, "/proc/self/status", O_RDONLY, 0)
                              : open("/proc/self/status", O_RDONLY);
    if (real_fd < 0) return -1;

    int mem_fd = create_in_memory_fd("status");
    if (mem_fd < 0) {
        close(real_fd);
        return -1;
    }

    const std::string &procName = BoxCore::getProcessName();
    const std::string &guestPkg = BoxCore::getGuestPackage();
    std::string targetComm = (!procName.empty() ? procName : guestPkg).substr(0, 15);

    char line[1024];
    size_t line_len = 0;
    char ch;
    while (read(real_fd, &ch, 1) == 1) {
        if (line_len < sizeof(line) - 1) {
            line[line_len++] = ch;
        }
        if (ch == '\n') {
            line[line_len] = '\0';
            line_len = 0;

            if (strncmp(line, "Name:\t", 6) == 0 && !targetComm.empty()) {
                std::string newLine = "Name:\t" + targetComm + "\n";
                write(mem_fd, newLine.data(), newLine.length());
            } else if (strncmp(line, "TracerPid:\t", 11) == 0) {
                write(mem_fd, "TracerPid:\t0\n", 13);
            } else {
                write(mem_fd, line, strlen(line));
            }
        }
    }
    close(real_fd);
    lseek(mem_fd, 0, SEEK_SET);
    return mem_fd;
}

int my_openat(int dirfd, const char *pathname, int flags, ...) {
    mode_t mode = 0;
    if ((flags & O_CREAT) || ((flags & O_TMPFILE) == O_TMPFILE)) {
        va_list args;
        va_start(args, flags);
        mode = va_arg(args, mode_t);
        va_end(args);
    }

    if (!isValidUserString(pathname) || pathname[0] == '\0') {
        return orig_openat ? orig_openat(dirfd, pathname, flags, mode) : -1;
    }

    if (is_proc_path(pathname, "maps")) {
        if ((flags & 3) == O_RDONLY) {
            int fd = create_cloaked_maps_fd();
            if (fd >= 0) return fd;
        }
    } else if (is_proc_path(pathname, "cmdline")) {
        if ((flags & 3) == O_RDONLY) {
            int fd = create_cloaked_cmdline_fd();
            if (fd >= 0) return fd;
        }
    } else if (is_proc_path(pathname, "comm")) {
        if ((flags & 3) == O_RDONLY) {
            int fd = create_cloaked_comm_fd();
            if (fd >= 0) return fd;
        }
    } else if (is_proc_path(pathname, "status")) {
        if ((flags & 3) == O_RDONLY) {
            int fd = create_cloaked_status_fd();
            if (fd >= 0) return fd;
        }
    }

    if (is_blocked_root_file(pathname)) {
        errno = ENOENT;
        return -1;
    }

    const char *redirected = IO::redirectPath(pathname);
    if (!redirected) {
        errno = EACCES;
        return -1;
    }
    if (strcmp(redirected, "/dev/null") == 0) {
        errno = ENOENT;
        return -1;
    }

    if (redirected != pathname) {
        return orig_openat ? orig_openat(AT_FDCWD, redirected, flags, mode) : -1;
    }

    return orig_openat ? orig_openat(dirfd, pathname, flags, mode) : -1;
}

int my_open(const char *pathname, int flags, ...) {
    mode_t mode = 0;
    if ((flags & O_CREAT) || ((flags & O_TMPFILE) == O_TMPFILE)) {
        va_list args;
        va_start(args, flags);
        mode = va_arg(args, mode_t);
        va_end(args);
    }
    return my_openat(AT_FDCWD, pathname, flags, mode);
}

int my_faccessat(int dirfd, const char *pathname, int mode, int flags) {
    if (!isValidUserString(pathname) || pathname[0] == '\0') {
        return orig_faccessat ? orig_faccessat(dirfd, pathname, mode, flags) : -1;
    }

    if (is_proc_path(pathname, "maps") ||
        is_proc_path(pathname, "cmdline") ||
        is_proc_path(pathname, "comm") ||
        is_proc_path(pathname, "status")) {
        if (mode & W_OK) {
            errno = EACCES;
            return -1;
        }
        return 0;
    }

    if (is_blocked_root_file(pathname)) {
        errno = ENOENT;
        return -1;
    }

    const char *redirected = IO::redirectPath(pathname);
    if (!redirected || strcmp(redirected, "/dev/null") == 0) {
        errno = ENOENT;
        return -1;
    }

    if (redirected != pathname) {
        return orig_faccessat ? orig_faccessat(AT_FDCWD, redirected, mode, flags) : -1;
    }

    return orig_faccessat ? orig_faccessat(dirfd, pathname, mode, flags) : -1;
}

int my_access(const char *pathname, int mode) {
    return my_faccessat(AT_FDCWD, pathname, mode, 0);
}

int my_newfstatat(int dirfd, const char *pathname, struct stat *buf, int flags) {
    if (!isValidUserString(pathname) || pathname[0] == '\0') {
        return orig_newfstatat ? orig_newfstatat(dirfd, pathname, buf, flags) : -1;
    }

    if (is_proc_path(pathname, "maps")) {
        return orig_newfstatat ? orig_newfstatat(AT_FDCWD, "/proc/self/maps", buf, flags) : -1;
    }
    if (is_proc_path(pathname, "cmdline")) {
        return orig_newfstatat ? orig_newfstatat(AT_FDCWD, "/proc/self/cmdline", buf, flags) : -1;
    }
    if (is_proc_path(pathname, "comm")) {
        return orig_newfstatat ? orig_newfstatat(AT_FDCWD, "/proc/self/comm", buf, flags) : -1;
    }
    if (is_proc_path(pathname, "status")) {
        return orig_newfstatat ? orig_newfstatat(AT_FDCWD, "/proc/self/status", buf, flags) : -1;
    }

    if (is_blocked_root_file(pathname)) {
        errno = ENOENT;
        return -1;
    }

    const char *redirected = IO::redirectPath(pathname);
    if (!redirected || strcmp(redirected, "/dev/null") == 0) {
        errno = ENOENT;
        return -1;
    }

    if (redirected != pathname) {
        return orig_newfstatat ? orig_newfstatat(AT_FDCWD, redirected, buf, flags) : -1;
    }

    return orig_newfstatat ? orig_newfstatat(dirfd, pathname, buf, flags) : -1;
}

ssize_t my_readlinkat(int dirfd, const char *pathname, char *buf, size_t bufsiz) {
    if (!isValidUserString(pathname) || pathname[0] == '\0') {
        return orig_readlinkat ? orig_readlinkat(dirfd, pathname, buf, bufsiz) : -1;
    }

    if (is_blocked_root_file(pathname)) {
        errno = ENOENT;
        return -1;
    }

    const char *redirected = IO::redirectPath(pathname);
    if (!redirected || strcmp(redirected, "/dev/null") == 0) {
        errno = ENOENT;
        return -1;
    }

    if (redirected != pathname) {
        return orig_readlinkat ? orig_readlinkat(AT_FDCWD, redirected, buf, bufsiz) : -1;
    }

    return orig_readlinkat ? orig_readlinkat(dirfd, pathname, buf, bufsiz) : -1;
}

ssize_t my_readlink(const char *pathname, char *buf, size_t bufsiz) {
    return my_readlinkat(AT_FDCWD, pathname, buf, bufsiz);
}

void FileSystemHook::init() {
    static bool sInitialized = false;
    if (sInitialized) return;
    sInitialized = true;

    ALOGD("FileSystemHook: Initializing native hooks with Dobby");
    void* handle = xdl_open("libc.so", XDL_DEFAULT);
    if (!handle) {
        ALOGE("FileSystemHook: Failed to open libc.so");
        return;
    }

    void* openat_sym = xdl_dsym(handle, "openat", nullptr);
    if (!openat_sym) openat_sym = xdl_sym(handle, "openat", nullptr);
    if (openat_sym) {
        if (DobbyHook(openat_sym, (void*)my_openat, (void**)&orig_openat) == 0) {
            ALOGD("FileSystemHook: Successfully hooked openat");
        } else {
            ALOGE("FileSystemHook: Failed to hook openat");
        }
    }

    void* open_sym = xdl_dsym(handle, "open", nullptr);
    if (!open_sym) open_sym = xdl_sym(handle, "open", nullptr);
    if (open_sym && open_sym != openat_sym) {
        if (DobbyHook(open_sym, (void*)my_open, (void**)&orig_open) == 0) {
            ALOGD("FileSystemHook: Successfully hooked open");
        }
    }

    void* faccessat_sym = xdl_dsym(handle, "faccessat", nullptr);
    if (!faccessat_sym) faccessat_sym = xdl_sym(handle, "faccessat", nullptr);
    if (faccessat_sym) {
        if (DobbyHook(faccessat_sym, (void*)my_faccessat, (void**)&orig_faccessat) == 0) {
            ALOGD("FileSystemHook: Successfully hooked faccessat");
        }
    }

    void* access_sym = xdl_dsym(handle, "access", nullptr);
    if (!access_sym) access_sym = xdl_sym(handle, "access", nullptr);
    if (access_sym && access_sym != faccessat_sym) {
        if (DobbyHook(access_sym, (void*)my_access, (void**)&orig_access) == 0) {
            ALOGD("FileSystemHook: Successfully hooked access");
        }
    }

    void* newfstatat_sym = xdl_dsym(handle, "newfstatat", nullptr);
    if (!newfstatat_sym) newfstatat_sym = xdl_sym(handle, "newfstatat", nullptr);
    if (!newfstatat_sym) newfstatat_sym = xdl_sym(handle, "fstatat64", nullptr);
    if (newfstatat_sym) {
        if (DobbyHook(newfstatat_sym, (void*)my_newfstatat, (void**)&orig_newfstatat) == 0) {
            ALOGD("FileSystemHook: Successfully hooked newfstatat");
        }
    }

    void* readlinkat_sym = xdl_dsym(handle, "readlinkat", nullptr);
    if (!readlinkat_sym) readlinkat_sym = xdl_sym(handle, "readlinkat", nullptr);
    if (readlinkat_sym) {
        if (DobbyHook(readlinkat_sym, (void*)my_readlinkat, (void**)&orig_readlinkat) == 0) {
            ALOGD("FileSystemHook: Successfully hooked readlinkat");
        }
    }

    void* readlink_sym = xdl_dsym(handle, "readlink", nullptr);
    if (!readlink_sym) readlink_sym = xdl_sym(handle, "readlink", nullptr);
    if (readlink_sym && readlink_sym != readlinkat_sym) {
        if (DobbyHook(readlink_sym, (void*)my_readlink, (void**)&orig_readlink) == 0) {
            ALOGD("FileSystemHook: Successfully hooked readlink");
        }
    }

    xdl_close(handle);
    ALOGD("FileSystemHook: All native libc hooks installed successfully");
}

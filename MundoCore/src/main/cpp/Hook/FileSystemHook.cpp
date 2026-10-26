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
static int (*orig_stat)(const char *pathname, struct stat *buf) = nullptr;
static int (*orig_lstat)(const char *pathname, struct stat *buf) = nullptr;
static int (*orig_mkdirat)(int dirfd, const char *pathname, mode_t mode) = nullptr;
static int (*orig_mkdir)(const char *pathname, mode_t mode) = nullptr;
static int (*orig_renameat)(int olddirfd, const char *oldpath, int newdirfd, const char *newpath) = nullptr;
static int (*orig_renameat2)(int olddirfd, const char *oldpath, int newdirfd, const char *newpath, unsigned int flags) = nullptr;
static int (*orig_rename)(const char *oldpath, const char *newpath) = nullptr;
static int (*orig_unlinkat)(int dirfd, const char *pathname, int flags) = nullptr;
static int (*orig_unlink)(const char *pathname) = nullptr;
static int (*orig_rmdir)(const char *pathname) = nullptr;
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

static void ensure_parent_dir_exists(const char *path) {
    if (!path || path[0] == '\0') return;
    char temp[1024];
    size_t len = strlen(path);
    if (len >= sizeof(temp)) return;
    memcpy(temp, path, len + 1);

    char *slash = strrchr(temp, '/');
    if (!slash || slash == temp) return;
    *slash = '\0';

    char *p = temp;
    if (*p == '/') p++;
    while (*p) {
        if (*p == '/') {
            *p = '\0';
            if (orig_mkdirat) {
                orig_mkdirat(AT_FDCWD, temp, 0777);
            } else if (orig_mkdir) {
                orig_mkdir(temp, 0777);
            }
            *p = '/';
        }
        p++;
    }
    if (orig_mkdirat) {
        orig_mkdirat(AT_FDCWD, temp, 0777);
    } else if (orig_mkdir) {
        orig_mkdir(temp, 0777);
    }
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
                              : (orig_open ? orig_open("/proc/self/maps", O_RDONLY) : -1);
    if (real_fd < 0) return -1;

    int mem_fd = create_in_memory_fd("maps");
    if (mem_fd < 0) {
        close(real_fd);
        return -1;
    }

    std::string hostPkg = BoxCore::getHostPackage();
    bool needReplaceHost = !hostPkg.empty();

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
                strstr(line, "libdobby") ||
                strstr(line, "MundoCore") ||
                strstr(line, "libMundoCore")) {
                continue;
            }

            write(mem_fd, line, strlen(line));
        }
    }
    close(real_fd);
    lseek(mem_fd, 0, SEEK_SET);
    return mem_fd;
}

static int my_openat(int dirfd, const char *pathname, int flags, ...) {
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
        if ((flags & O_CREAT) || (flags & O_WRONLY) || (flags & O_RDWR)) {
            ensure_parent_dir_exists(redirected);
        }
        return orig_openat ? orig_openat(AT_FDCWD, redirected, flags, mode) : -1;
    }

    return orig_openat ? orig_openat(dirfd, pathname, flags, mode) : -1;
}

static int my_open(const char *pathname, int flags, ...) {
    mode_t mode = 0;
    if ((flags & O_CREAT) || ((flags & O_TMPFILE) == O_TMPFILE)) {
        va_list args;
        va_start(args, flags);
        mode = va_arg(args, mode_t);
        va_end(args);
    }
    return my_openat(AT_FDCWD, pathname, flags, mode);
}

static int my_faccessat(int dirfd, const char *pathname, int mode, int flags) {
    if (!isValidUserString(pathname) || pathname[0] == '\0') {
        return orig_faccessat ? orig_faccessat(dirfd, pathname, mode, flags) : -1;
    }

    if (is_proc_path(pathname, "maps")) {
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

static int my_access(const char *pathname, int mode) {
    return my_faccessat(AT_FDCWD, pathname, mode, 0);
}

static int my_newfstatat(int dirfd, const char *pathname, struct stat *buf, int flags) {
    if (!isValidUserString(pathname) || pathname[0] == '\0') {
        return orig_newfstatat ? orig_newfstatat(dirfd, pathname, buf, flags) : -1;
    }

    if (is_proc_path(pathname, "maps")) {
        return orig_newfstatat ? orig_newfstatat(AT_FDCWD, "/proc/self/maps", buf, flags) : -1;
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

static int my_stat(const char *pathname, struct stat *buf) {
    return my_newfstatat(AT_FDCWD, pathname, buf, 0);
}

static int my_lstat(const char *pathname, struct stat *buf) {
    return my_newfstatat(AT_FDCWD, pathname, buf, AT_SYMLINK_NOFOLLOW);
}

static int my_mkdirat(int dirfd, const char *pathname, mode_t mode) {
    if (!isValidUserString(pathname) || pathname[0] == '\0') {
        return orig_mkdirat ? orig_mkdirat(dirfd, pathname, mode) : -1;
    }

    const char *redirected = IO::redirectPath(pathname);
    if (!redirected || strcmp(redirected, "/dev/null") == 0) {
        errno = EACCES;
        return -1;
    }

    if (redirected != pathname) {
        ensure_parent_dir_exists(redirected);
        return orig_mkdirat ? orig_mkdirat(AT_FDCWD, redirected, mode) : -1;
    }

    return orig_mkdirat ? orig_mkdirat(dirfd, pathname, mode) : -1;
}

static int my_mkdir(const char *pathname, mode_t mode) {
    return my_mkdirat(AT_FDCWD, pathname, mode);
}

static int my_renameat(int olddirfd, const char *oldpath, int newdirfd, const char *newpath) {
    if (!isValidUserString(oldpath) || !isValidUserString(newpath)) {
        return orig_renameat ? orig_renameat(olddirfd, oldpath, newdirfd, newpath) : -1;
    }

    const char *redirected_old = IO::redirectPath(oldpath);
    const char *redirected_new = IO::redirectPath(newpath);

    if (redirected_old != oldpath || redirected_new != newpath) {
        if (redirected_new) {
            ensure_parent_dir_exists(redirected_new);
        }
        return orig_renameat ? orig_renameat(AT_FDCWD, redirected_old, AT_FDCWD, redirected_new) : -1;
    }

    return orig_renameat ? orig_renameat(olddirfd, oldpath, newdirfd, newpath) : -1;
}

static int my_rename(const char *oldpath, const char *newpath) {
    return my_renameat(AT_FDCWD, oldpath, AT_FDCWD, newpath);
}

static int my_renameat2(int olddirfd, const char *oldpath, int newdirfd, const char *newpath, unsigned int flags) {
    if (!isValidUserString(oldpath) || !isValidUserString(newpath)) {
        return orig_renameat2 ? orig_renameat2(olddirfd, oldpath, newdirfd, newpath, flags) : -1;
    }
    const char *redirected_old = IO::redirectPath(oldpath);
    const char *redirected_new = IO::redirectPath(newpath);
    if (redirected_old != oldpath || redirected_new != newpath) {
        if (redirected_new) {
            ensure_parent_dir_exists(redirected_new);
        }
        return orig_renameat2 ? orig_renameat2(AT_FDCWD, redirected_old, AT_FDCWD, redirected_new, flags) : -1;
    }
    return orig_renameat2 ? orig_renameat2(olddirfd, oldpath, newdirfd, newpath, flags) : -1;
}

static int my_unlinkat(int dirfd, const char *pathname, int flags) {
    if (!isValidUserString(pathname) || pathname[0] == '\0') {
        return orig_unlinkat ? orig_unlinkat(dirfd, pathname, flags) : -1;
    }

    const char *redirected = IO::redirectPath(pathname);
    if (redirected != pathname) {
        return orig_unlinkat ? orig_unlinkat(AT_FDCWD, redirected, flags) : -1;
    }

    return orig_unlinkat ? orig_unlinkat(dirfd, pathname, flags) : -1;
}

static int my_unlink(const char *pathname) {
    return my_unlinkat(AT_FDCWD, pathname, 0);
}

static int my_rmdir(const char *pathname) {
    return my_unlinkat(AT_FDCWD, pathname, AT_REMOVEDIR);
}

static ssize_t my_readlinkat(int dirfd, const char *pathname, char *buf, size_t bufsiz) {
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

static ssize_t my_readlink(const char *pathname, char *buf, size_t bufsiz) {
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

    void *hooked_addrs[32] = {nullptr};
    void *hooked_origs[32] = {nullptr};
    int hooked_count = 0;

    auto hook_symbol = [&](const char *sym_name, void *new_func, void **orig_func) {
        void *sym = xdl_dsym(handle, sym_name, nullptr);
        if (!sym) sym = xdl_sym(handle, sym_name, nullptr);
        if (sym) {
            for (int i = 0; i < hooked_count; ++i) {
                if (hooked_addrs[i] == sym) {
                    ALOGD("FileSystemHook: %s shares address with already hooked symbol", sym_name);
                    *orig_func = hooked_origs[i];
                    return;
                }
            }
            if (DobbyHook(sym, new_func, orig_func) == 0) {
                hooked_addrs[hooked_count] = sym;
                hooked_origs[hooked_count] = *orig_func;
                hooked_count++;
                ALOGD("FileSystemHook: Successfully hooked %s", sym_name);
            } else {
                ALOGE("FileSystemHook: Failed to hook %s", sym_name);
            }
        }
    };

    hook_symbol("openat", (void*)my_openat, (void**)&orig_openat);
    hook_symbol("open", (void*)my_open, (void**)&orig_open);
    hook_symbol("faccessat", (void*)my_faccessat, (void**)&orig_faccessat);
    hook_symbol("faccessat2", (void*)my_faccessat, (void**)&orig_faccessat);
    hook_symbol("access", (void*)my_access, (void**)&orig_access);
    hook_symbol("newfstatat", (void*)my_newfstatat, (void**)&orig_newfstatat);
    hook_symbol("fstatat64", (void*)my_newfstatat, (void**)&orig_newfstatat);
    hook_symbol("stat", (void*)my_stat, (void**)&orig_stat);
    hook_symbol("lstat", (void*)my_lstat, (void**)&orig_lstat);
    hook_symbol("mkdirat", (void*)my_mkdirat, (void**)&orig_mkdirat);
    hook_symbol("mkdir", (void*)my_mkdir, (void**)&orig_mkdir);
    hook_symbol("renameat", (void*)my_renameat, (void**)&orig_renameat);
    hook_symbol("renameat2", (void*)my_renameat2, (void**)&orig_renameat2);
    hook_symbol("rename", (void*)my_rename, (void**)&orig_rename);
    hook_symbol("unlinkat", (void*)my_unlinkat, (void**)&orig_unlinkat);
    hook_symbol("unlink", (void*)my_unlink, (void**)&orig_unlink);
    hook_symbol("rmdir", (void*)my_rmdir, (void**)&orig_rmdir);
    hook_symbol("readlinkat", (void*)my_readlinkat, (void**)&orig_readlinkat);
    hook_symbol("readlink", (void*)my_readlink, (void**)&orig_readlink);

    xdl_close(handle);
    ALOGD("FileSystemHook: All native libc hooks installed successfully");
}

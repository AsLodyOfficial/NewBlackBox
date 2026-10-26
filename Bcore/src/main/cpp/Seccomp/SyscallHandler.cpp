#include "SyscallHandler.h"
#include "../IO.h"
#include "../Log.h"
#include "RawSyscall.h"
#include <cstring>
#include <string>
#include <sys/syscall.h>
#include <fcntl.h>
#include <unistd.h>

static std::string sHostPackage;
static std::string sGuestPackage;
static int sLastResult = 0;

static inline bool isValidUserString(const char *ptr) {
    if (!ptr) {
        return false;
    }
    uintptr_t uptr = (uintptr_t)ptr;
#if defined(__aarch64__)
    uintptr_t untagged = uptr & 0x00FFFFFFFFFFFFFFULL;
    if (untagged < 0x1000 || untagged >= 0x0000800000000000ULL) {
        return false;
    }
#elif defined(__arm__)
    if (uptr < 0x1000 || uptr >= 0xC0000000UL) {
        return false;
    }
#endif
    return true;
}

long HookOpenAt(SyscallContext &ctx) {
    int dirfd = (int)ctx.args[0];
    const char *pathname = (const char *)ctx.args[1];
    int flags = (int)ctx.args[2];
    mode_t mode = (mode_t)ctx.args[3];

    if (!isValidUserString(pathname) || pathname[0] == '\0') {
        return raw_syscall(ctx.nr, dirfd, (long)pathname, flags, mode, 0, 0);
    }

    const char *redirected = IO::redirectPath(pathname);
    if (!redirected) {
        return -Error::AccessDenied;
    }
    if (strcmp(redirected, "/dev/null") == 0) {
        return -Error::NotFound;
    }

    return raw_syscall(ctx.nr, dirfd, (long)redirected, flags, mode, 0, 0);
}

long HookOpenAt2(SyscallContext &ctx) {
    int dirfd = (int)ctx.args[0];
    const char *pathname = (const char *)ctx.args[1];
    void *how = (void *)ctx.args[2];
    size_t size = (size_t)ctx.args[3];

    if (!isValidUserString(pathname) || pathname[0] == '\0') {
        return raw_syscall(ctx.nr, dirfd, (long)pathname, (long)how, (long)size, 0, 0);
    }

    const char *redirected = IO::redirectPath(pathname);
    if (!redirected) {
        return -Error::AccessDenied;
    }
    if (strcmp(redirected, "/dev/null") == 0) {
        return -Error::NotFound;
    }

    return raw_syscall(ctx.nr, dirfd, (long)redirected, (long)how, (long)size, 0, 0);
}

long HookReadlinkAt(SyscallContext &ctx) {
    int dirfd = (int)ctx.args[0];
    const char *pathname = (const char *)ctx.args[1];
    char *buf = (char *)ctx.args[2];
    size_t bufsiz = (size_t)ctx.args[3];

    if (!isValidUserString(pathname) || pathname[0] == '\0') {
        return raw_syscall(ctx.nr, dirfd, (long)pathname, (long)buf, (long)bufsiz, 0, 0);
    }

    const char *redirected = IO::redirectPath(pathname);
    if (!redirected) {
        return -Error::AccessDenied;
    }
    if (strcmp(redirected, "/dev/null") == 0) {
        return -Error::NotFound;
    }

    return raw_syscall(ctx.nr, dirfd, (long)redirected, (long)buf, (long)bufsiz, 0, 0);
}

long HookGetdents64(SyscallContext &ctx) {
    int fd = (int)ctx.args[0];
    void *dirp = (void *)ctx.args[1];
    unsigned int count = (unsigned int)ctx.args[2];

    return raw_syscall(ctx.nr, fd, (long)dirp, count, 0, 0, 0);
}

long HookPathAccess(SyscallContext &ctx) {
    int dirfd = (int)ctx.args[0];
    const char *pathname = (const char *)ctx.args[1];
    int mode = (int)ctx.args[2];
    int flags = (int)ctx.args[3];

    if (!isValidUserString(pathname) || pathname[0] == '\0') {
        return raw_syscall(ctx.nr, dirfd, (long)pathname, mode, flags, 0, 0);
    }

    const char *redirected = IO::redirectPath(pathname);
    if (!redirected) {
        return -Error::AccessDenied;
    }
    if (strcmp(redirected, "/dev/null") == 0) {
        return -Error::NotFound;
    }

    return raw_syscall(ctx.nr, dirfd, (long)redirected, mode, flags, 0, 0);
}

long HookFstatat(SyscallContext &ctx) {
    int dirfd = (int)ctx.args[0];
    const char *pathname = (const char *)ctx.args[1];
    void *statbuf = (void *)ctx.args[2];
    int flags = (int)ctx.args[3];

    if (!isValidUserString(pathname) || pathname[0] == '\0') {
        return raw_syscall(ctx.nr, dirfd, (long)pathname, (long)statbuf, flags, 0, 0);
    }

    const char *redirected = IO::redirectPath(pathname);
    if (!redirected) {
        return -Error::AccessDenied;
    }
    if (strcmp(redirected, "/dev/null") == 0) {
        return -Error::NotFound;
    }

    return raw_syscall(ctx.nr, dirfd, (long)redirected, (long)statbuf, flags, 0, 0);
}

long HookStatx(SyscallContext &ctx) {
    int dirfd = (int)ctx.args[0];
    const char *pathname = (const char *)ctx.args[1];
    int flags = (int)ctx.args[2];
    unsigned int mask = (unsigned int)ctx.args[3];
    void *statxbuf = (void *)ctx.args[4];

    if (!isValidUserString(pathname) || pathname[0] == '\0') {
        return raw_syscall(ctx.nr, dirfd, (long)pathname, flags, mask, (long)statxbuf, 0);
    }

    const char *redirected = IO::redirectPath(pathname);
    if (!redirected) {
        return -Error::AccessDenied;
    }
    if (strcmp(redirected, "/dev/null") == 0) {
        return -Error::NotFound;
    }

    return raw_syscall(ctx.nr, dirfd, (long)redirected, flags, mask, (long)statxbuf, 0);
}

long HookPrctl(SyscallContext &ctx) {
    int option = (int)ctx.args[0];
    if (option == 3 /* PR_GET_DUMPABLE */) {
        return 1;
    }
    return raw_syscall(ctx.nr, ctx.args[0], ctx.args[1], ctx.args[2], ctx.args[3], ctx.args[4], ctx.args[5]);
}

int InitializeRuntime(const char *Host, const char *Guest) {
    if (Host) sHostPackage = Host;
    if (Guest) sGuestPackage = Guest;

    FSeccomp::Hook(Seccomp::FSyscalls::OpenAt, HookOpenAt);
    FSeccomp::Hook(Seccomp::FSyscalls::OpenAt2, HookOpenAt2);
    FSeccomp::Hook(Seccomp::FSyscalls::ReadlinkAt, HookReadlinkAt);
    FSeccomp::Hook(Seccomp::FSyscalls::GetDents64, HookGetdents64);
    FSeccomp::Hook(Seccomp::FSyscalls::Faccessat, HookPathAccess);
    FSeccomp::Hook(Seccomp::FSyscalls::FaccessAt2, HookPathAccess);
    FSeccomp::Hook(Seccomp::FSyscalls::NewFstatAt, HookFstatat);
    FSeccomp::Hook(Seccomp::FSyscalls::Statx, HookStatx);
    FSeccomp::Hook(Seccomp::FSyscalls::Prctl, HookPrctl);

    FSeccomp::Install();
    if (const char *Error = FSeccomp::GetErrorMsg()) {
        (void)Error;
        return -Error::Io;
    }
    return 0;
}

void SaveFailure(int result) {
    sLastResult = result;
    if (result != 0) {
        ALOGE("FSeccomp: Runtime initialization failure code: %d", result);
    } else {
        ALOGD("FSeccomp: Runtime initialized successfully.");
    }
}

int Start(const char *Host, const char *Guest) {
    const int Result = InitializeRuntime(Host, Guest);
    SaveFailure(Result);
    return Result;
}

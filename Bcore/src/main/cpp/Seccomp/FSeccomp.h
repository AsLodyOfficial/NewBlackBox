#ifndef SECCOMP_FSECCOMP_H
#define SECCOMP_FSECCOMP_H

#include <cstdint>
#include <cstddef>
#include <string>
#include "RawSyscall.h"

namespace Error {
    enum Code {
        Success = 0,
        NotFound = 2,
        Io = 5,
        AccessDenied = 13,
        Invalid = 22
    };
}

namespace Seccomp {
    enum class FSyscalls {
        OpenAt,
        OpenAt2,
        ReadlinkAt,
        GetDents64,
        Faccessat,
        FaccessAt2,
        NewFstatAt,
        Statx,
        Prctl
    };
}

struct SyscallContext {
    long nr;
    long args[6];
    uintptr_t ip;
    void *ucontext;
};

using SyscallHookFn = long (*)(SyscallContext &ctx);

class FSeccomp {
public:
    static void Hook(Seccomp::FSyscalls syscall, SyscallHookFn handler);
    static bool Install();
    static const char *GetErrorMsg();
    static int GetSyscallNr(Seccomp::FSyscalls syscall);
};

int Start(const char *Host, const char *Guest);

#endif // SECCOMP_FSECCOMP_H

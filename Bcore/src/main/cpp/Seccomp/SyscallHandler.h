#ifndef SECCOMP_SYSCALL_HANDLER_H
#define SECCOMP_SYSCALL_HANDLER_H

#include "FSeccomp.h"

long HookOpenAt(SyscallContext &ctx);
long HookOpenAt2(SyscallContext &ctx);
long HookReadlinkAt(SyscallContext &ctx);
long HookGetdents64(SyscallContext &ctx);
long HookPathAccess(SyscallContext &ctx);
long HookFstatat(SyscallContext &ctx);
long HookStatx(SyscallContext &ctx);
long HookPrctl(SyscallContext &ctx);

int InitializeRuntime(const char *Host, const char *Guest);
void SaveFailure(int result);

#endif // SECCOMP_SYSCALL_HANDLER_H

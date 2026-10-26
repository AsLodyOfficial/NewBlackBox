#ifndef SECCOMP_RAW_SYSCALL_H
#define SECCOMP_RAW_SYSCALL_H

#include <cstdint>

#ifdef __cplusplus
extern "C" {
#endif

long raw_syscall(long nr, long a0, long a1, long a2, long a3, long a4, long a5);

extern void raw_syscall_start();
extern void raw_syscall_end();

#ifdef __cplusplus
}
#endif

#endif // SECCOMP_RAW_SYSCALL_H

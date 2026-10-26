#include "FSeccomp.h"
#include "../Log.h"
#include <unordered_map>
#include <vector>
#include <cstddef>
#include <cstring>
#include <cerrno>
#include <csignal>
#include <sys/prctl.h>
#include <sys/syscall.h>
#include <linux/filter.h>
#include <linux/seccomp.h>
#include <linux/audit.h>
#include <ucontext.h>

#ifndef AUDIT_ARCH_AARCH64
#define AUDIT_ARCH_AARCH64 (0x40000000 | 0xb7)
#endif
#ifndef AUDIT_ARCH_ARM
#define AUDIT_ARCH_ARM (0x28)
#endif

#ifndef SECCOMP_MODE_FILTER
#define SECCOMP_MODE_FILTER 2
#endif
#ifndef SECCOMP_RET_ALLOW
#define SECCOMP_RET_ALLOW 0x7fff0000U
#endif
#ifndef SECCOMP_RET_TRAP
#define SECCOMP_RET_TRAP 0x00030000U
#endif
#ifndef SECCOMP_SET_MODE_FILTER
#define SECCOMP_SET_MODE_FILTER 1
#endif
#ifndef __NR_seccomp
#if defined(__aarch64__)
#define __NR_seccomp 277
#elif defined(__arm__)
#define __NR_seccomp 383
#endif
#endif

static SyscallHookFn sHooksTable[512] = {nullptr};
static std::vector<int> sHookedNrs;
static std::string sLastError;

int FSeccomp::GetSyscallNr(Seccomp::FSyscalls syscall) {
#if defined(__aarch64__)
    switch (syscall) {
        case Seccomp::FSyscalls::OpenAt: return __NR_openat;
        case Seccomp::FSyscalls::OpenAt2:
#ifdef __NR_openat2
            return __NR_openat2;
#else
            return 437;
#endif
        case Seccomp::FSyscalls::ReadlinkAt: return __NR_readlinkat;
        case Seccomp::FSyscalls::GetDents64: return __NR_getdents64;
        case Seccomp::FSyscalls::Faccessat: return __NR_faccessat;
        case Seccomp::FSyscalls::FaccessAt2:
#ifdef __NR_faccessat2
            return __NR_faccessat2;
#else
            return 439;
#endif
        case Seccomp::FSyscalls::NewFstatAt: return __NR_newfstatat;
        case Seccomp::FSyscalls::Statx:
#ifdef __NR_statx
            return __NR_statx;
#else
            return 291;
#endif
        case Seccomp::FSyscalls::Prctl: return __NR_prctl;
    }
#elif defined(__arm__)
    switch (syscall) {
        case Seccomp::FSyscalls::OpenAt: return __NR_openat;
        case Seccomp::FSyscalls::OpenAt2: return 437;
        case Seccomp::FSyscalls::ReadlinkAt: return __NR_readlinkat;
        case Seccomp::FSyscalls::GetDents64: return __NR_getdents64;
        case Seccomp::FSyscalls::Faccessat: return __NR_faccessat;
        case Seccomp::FSyscalls::FaccessAt2: return 439;
        case Seccomp::FSyscalls::NewFstatAt:
#ifdef __NR_fstatat64
            return __NR_fstatat64;
#else
            return 327;
#endif
        case Seccomp::FSyscalls::Statx:
#ifdef __NR_statx
            return __NR_statx;
#else
            return 397;
#endif
        case Seccomp::FSyscalls::Prctl: return __NR_prctl;
    }
#endif
    return -1;
}

void FSeccomp::Hook(Seccomp::FSyscalls syscall, SyscallHookFn handler) {
    int nr = GetSyscallNr(syscall);
    if (nr > 0 && nr < 512) {
        sHooksTable[nr] = handler;
        sHookedNrs.push_back(nr);
        ALOGD("FSeccomp: Registered hook for syscall %d", nr);
    }
}

static void SigsysAction(int sig, siginfo_t *info, void *context) {
    if (!context) return;
    ucontext_t *uctx = reinterpret_cast<ucontext_t *>(context);
    SyscallContext ctx{};
    ctx.ucontext = uctx;

#if defined(__aarch64__)
    ctx.nr = (long)uctx->uc_mcontext.regs[8];
    ctx.args[0] = (long)uctx->uc_mcontext.regs[0];
    ctx.args[1] = (long)uctx->uc_mcontext.regs[1];
    ctx.args[2] = (long)uctx->uc_mcontext.regs[2];
    ctx.args[3] = (long)uctx->uc_mcontext.regs[3];
    ctx.args[4] = (long)uctx->uc_mcontext.regs[4];
    ctx.args[5] = (long)uctx->uc_mcontext.regs[5];
    ctx.ip = (uintptr_t)uctx->uc_mcontext.pc;
#elif defined(__arm__)
    ctx.nr = (long)uctx->uc_mcontext.arm_r7;
    ctx.args[0] = (long)uctx->uc_mcontext.arm_r0;
    ctx.args[1] = (long)uctx->uc_mcontext.arm_r1;
    ctx.args[2] = (long)uctx->uc_mcontext.arm_r2;
    ctx.args[3] = (long)uctx->uc_mcontext.arm_r3;
    ctx.args[4] = (long)uctx->uc_mcontext.arm_r4;
    ctx.args[5] = (long)uctx->uc_mcontext.arm_r5;
    ctx.ip = (uintptr_t)uctx->uc_mcontext.arm_pc;
#else
    return;
#endif

    SyscallHookFn fn = (ctx.nr >= 0 && ctx.nr < 512) ? sHooksTable[ctx.nr] : nullptr;
    long ret = 0;
    if (fn) {
        ret = fn(ctx);
    } else {
        ret = raw_syscall(ctx.nr, ctx.args[0], ctx.args[1], ctx.args[2], ctx.args[3], ctx.args[4], ctx.args[5]);
    }

#if defined(__aarch64__)
    uctx->uc_mcontext.regs[0] = (unsigned long)ret;
    uintptr_t pc_val = (uintptr_t)uctx->uc_mcontext.pc;
    if ((pc_val & 3) == 0 && pc_val > 0x1000) {
        uint32_t insn = *reinterpret_cast<uint32_t *>(pc_val);
        if (insn == 0xd4000001) {
            uctx->uc_mcontext.pc += 4;
        }
    }
#elif defined(__arm__)
    uctx->uc_mcontext.arm_r0 = (unsigned long)ret;
    uintptr_t pc_val = (uintptr_t)uctx->uc_mcontext.arm_pc;
    if (uctx->uc_mcontext.arm_cpsr & (1 << 5)) {
        if ((pc_val & 1) == 0 && pc_val > 0x1000) {
            uint16_t insn = *reinterpret_cast<uint16_t *>(pc_val);
            if ((insn & 0xff00) == 0xdf00) {
                uctx->uc_mcontext.arm_pc += 2;
            }
        }
    } else {
        if ((pc_val & 3) == 0 && pc_val > 0x1000) {
            uint32_t insn = *reinterpret_cast<uint32_t *>(pc_val);
            if ((insn & 0x0f000000) == 0x0f000000) {
                uctx->uc_mcontext.arm_pc += 4;
            }
        }
    }
#endif
}

bool FSeccomp::Install() {
    sLastError.clear();

    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sa.sa_sigaction = SigsysAction;
    sa.sa_flags = SA_SIGINFO | SA_NODEFER;
    sigemptyset(&sa.sa_mask);
    if (sigaction(SIGSYS, &sa, nullptr) != 0) {
        sLastError = "sigaction(SIGSYS) failed: " + std::string(strerror(errno));
        ALOGE("%s", sLastError.c_str());
        return false;
    }

    if (prctl(PR_SET_NO_NEW_PRIVS, 1, 0, 0, 0) != 0) {
        sLastError = "prctl(PR_SET_NO_NEW_PRIVS) failed: " + std::string(strerror(errno));
        ALOGE("%s", sLastError.c_str());
        return false;
    }

    std::vector<sock_filter> filter;

#if defined(__aarch64__)
    uint32_t arch = AUDIT_ARCH_AARCH64;
#elif defined(__arm__)
    uint32_t arch = AUDIT_ARCH_ARM;
#else
    uint32_t arch = 0;
#endif

    // 1. Verify architecture
    filter.push_back(BPF_STMT(BPF_LD | BPF_W | BPF_ABS, offsetof(struct seccomp_data, arch)));
    filter.push_back(BPF_JUMP(BPF_JMP | BPF_JEQ | BPF_K, arch, 1, 0));
    filter.push_back(BPF_STMT(BPF_RET | BPF_K, SECCOMP_RET_ALLOW));

    // 2. Allow syscalls originating directly from raw_syscall (avoid recursion loop)
    uintptr_t start_addr = (uintptr_t)raw_syscall_start;
    uintptr_t end_addr = (uintptr_t)raw_syscall_end;

#if defined(__aarch64__)
    uint32_t start_hi = (uint32_t)((uint64_t)start_addr >> 32);
    uint32_t start_lo = (uint32_t)((uint64_t)start_addr & 0xFFFFFFFF);
    uint32_t end_lo   = (uint32_t)((uint64_t)end_addr & 0xFFFFFFFF);

    filter.push_back(BPF_STMT(BPF_LD | BPF_W | BPF_ABS, offsetof(struct seccomp_data, instruction_pointer) + 4));
    filter.push_back(BPF_JUMP(BPF_JMP | BPF_JEQ | BPF_K, start_hi, 0, 4));

    filter.push_back(BPF_STMT(BPF_LD | BPF_W | BPF_ABS, offsetof(struct seccomp_data, instruction_pointer)));
    filter.push_back(BPF_JUMP(BPF_JMP | BPF_JGE | BPF_K, start_lo, 0, 2));
    filter.push_back(BPF_JUMP(BPF_JMP | BPF_JGE | BPF_K, end_lo, 1, 0));
    filter.push_back(BPF_STMT(BPF_RET | BPF_K, SECCOMP_RET_ALLOW));

#elif defined(__arm__)
    uint32_t start_lo = (uint32_t)start_addr;
    uint32_t end_lo   = (uint32_t)end_addr;

    filter.push_back(BPF_STMT(BPF_LD | BPF_W | BPF_ABS, offsetof(struct seccomp_data, instruction_pointer)));
    filter.push_back(BPF_JUMP(BPF_JMP | BPF_JGE | BPF_K, start_lo, 0, 2));
    filter.push_back(BPF_JUMP(BPF_JMP | BPF_JGE | BPF_K, end_lo, 1, 0));
    filter.push_back(BPF_STMT(BPF_RET | BPF_K, SECCOMP_RET_ALLOW));
#endif

    // 3. Inspect syscall number
    filter.push_back(BPF_STMT(BPF_LD | BPF_W | BPF_ABS, offsetof(struct seccomp_data, nr)));
    for (int nr : sHookedNrs) {
        filter.push_back(BPF_JUMP(BPF_JMP | BPF_JEQ | BPF_K, (uint32_t)nr, 0, 1));
        filter.push_back(BPF_STMT(BPF_RET | BPF_K, SECCOMP_RET_TRAP));
    }

    // 4. Default allow all unhooked syscalls
    filter.push_back(BPF_STMT(BPF_RET | BPF_K, SECCOMP_RET_ALLOW));

    struct sock_fprog prog = {
        .len = (unsigned short)filter.size(),
        .filter = filter.data(),
    };

    if (prctl(PR_SET_SECCOMP, SECCOMP_MODE_FILTER, &prog) != 0) {
        if (raw_syscall(__NR_seccomp, SECCOMP_SET_MODE_FILTER, 0, (long)&prog, 0, 0, 0) != 0) {
            sLastError = "prctl(PR_SET_SECCOMP) failed: " + std::string(strerror(errno));
            ALOGE("%s", sLastError.c_str());
            return false;
        }
    }

    ALOGD("FSeccomp: Installed successfully with %zu hooked syscalls", sHookedNrs.size());
    return true;
}

const char *FSeccomp::GetErrorMsg() {
    return sLastError.empty() ? nullptr : sLastError.c_str();
}

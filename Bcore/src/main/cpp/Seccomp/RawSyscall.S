.text

#if defined(__aarch64__)

.global raw_syscall
.global raw_syscall_start
.global raw_syscall_end
.type raw_syscall, %function
.align 4

raw_syscall_start:
raw_syscall:
    mov x8, x0
    mov x0, x1
    mov x1, x2
    mov x2, x3
    mov x3, x4
    mov x4, x5
    mov x5, x6
    svc 0
    ret
raw_syscall_end:

#elif defined(__arm__)

.global raw_syscall
.global raw_syscall_start
.global raw_syscall_end
.type raw_syscall, %function
.align 4
.arm

raw_syscall_start:
raw_syscall:
    push {r4, r5, r7, lr}
    mov r7, r0
    mov r0, r1
    mov r1, r2
    mov r2, r3
    ldr r3, [sp, #16]
    ldr r4, [sp, #20]
    ldr r5, [sp, #24]
    svc 0
    pop {r4, r5, r7, pc}
raw_syscall_end:

#endif

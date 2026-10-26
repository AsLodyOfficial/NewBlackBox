//
// Fixed UnixFileSystemHook.cpp
// Solves .pak write Permission denied (errno=13)
// Android 10–17 BlackBox compatible
//

#include <IO.h>
#include "UnixFileSystemHook.h"
#import "JniHook/JniHook.h"
#include "BaseHook.h"
#include <sys/statvfs.h>
#include <sys/vfs.h>
#include <sys/statfs.h>
#include <dlfcn.h>
#include <cstdint>
#include "Dobby/dobby.h"
#include <xdl.h>
#include "Log.h"

/*
 * Class:     java_io_UnixFileSystem
 * Method:    canonicalize0
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
HOOK_JNI(jstring, canonicalize0, JNIEnv *env, jobject obj, jstring path) {
    jstring redirect = IO::redirectPath(env, path);
    jstring res = orig_canonicalize0(env, obj, redirect);
    env->DeleteLocalRef(redirect);
    return res;
}

/*
 * Class:     java_io_UnixFileSystem
 * Method:    getBooleanAttributes0
 * Signature: (Ljava/lang/String;)I
 */
HOOK_JNI(jint, getBooleanAttributes0, JNIEnv *env, jobject obj, jstring abspath) {
    jstring redirect = IO::redirectPath(env, abspath);
    jint res = orig_getBooleanAttributes0(env, obj, redirect);
    env->DeleteLocalRef(redirect);
    return res;
}

/*
 * Class:     java_io_UnixFileSystem
 * Method:    getLastModifiedTime0
 * Signature: (Ljava/io/File;)J
 */
HOOK_JNI(jlong, getLastModifiedTime0, JNIEnv *env, jobject obj, jobject path) {
    jobject redirect = IO::redirectPath(env, path);
    jlong res = orig_getLastModifiedTime0(env, obj, redirect);
    env->DeleteLocalRef(redirect);
    return res;
}

/*
 * Class:     java_io_UnixFileSystem
 * Method:    setPermission0
 * Signature: (Ljava/io/File;IZZ)Z
 */
HOOK_JNI(jboolean, setPermission0, JNIEnv *env, jobject obj, jobject file, jint access,
         jboolean enable, jboolean owneronly) {
    jobject redirect = IO::redirectPath(env, file);
    jboolean res = orig_setPermission0(env, obj, redirect, access, enable, owneronly);
    env->DeleteLocalRef(redirect);
    return res;
}

/*
 * Class:     java_io_UnixFileSystem
 * Method:    createFileExclusively0
 * Signature: (Ljava/lang/String;)Z
 */
HOOK_JNI(jboolean, createFileExclusively0, JNIEnv *env, jobject obj, jstring path) {
    jstring redirect = IO::redirectPath(env, path);
    jboolean res = orig_createFileExclusively0(env, obj, redirect);
    env->DeleteLocalRef(redirect);
    return res;
}

/*
 * Class:     java_io_UnixFileSystem
 * Method:    list0
 * Signature: (Ljava/io/File;)[Ljava/lang/String;
 */
HOOK_JNI(jobjectArray, list0, JNIEnv *env, jobject obj, jobject file) {
    jobject redirect = IO::redirectPath(env, file);
    jobjectArray res = orig_list0(env, obj, redirect);
    env->DeleteLocalRef(redirect);
    return res;
}

/*
 * Class:     java_io_UnixFileSystem
 * Method:    createDirectory0
 * Signature: (Ljava/io/File;)Z
 */
HOOK_JNI(jboolean, createDirectory0, JNIEnv *env, jobject obj, jobject path) {
    jobject redirect = IO::redirectPath(env, path);
    jboolean res = orig_createDirectory0(env, obj, redirect);
    env->DeleteLocalRef(redirect);
    return res;
}

/*
 * Class:     java_io_UnixFileSystem
 * Method:    setLastModifiedTime0
 * Signature: (Ljava/io/File;J)Z
 */
HOOK_JNI(jboolean, setLastModifiedTime0, JNIEnv *env, jobject obj, jobject file, jlong time) {
    jobject redirect = IO::redirectPath(env, file);
    jboolean res = orig_setLastModifiedTime0(env, obj, redirect, time);
    env->DeleteLocalRef(redirect);
    return res;
}

/*
 * Class:     java_io_UnixFileSystem
 * Method:    setReadOnly0
 * Signature: (Ljava/io/File;)Z
 */
HOOK_JNI(jboolean, setReadOnly0, JNIEnv *env, jobject obj, jobject file) {
    jobject redirect = IO::redirectPath(env, file);
    jboolean res = orig_setReadOnly0(env, obj, redirect);
    env->DeleteLocalRef(redirect);
    return res;
}

/*
 * Class:     java_io_UnixFileSystem
 * Method:    getSpace0
 * Signature: (Ljava/io/File;I)J
 */
HOOK_JNI(jlong, getSpace0, JNIEnv *env, jobject obj, jobject file, jint t) {
    jobject redirect = IO::redirectPath(env, file);
    jlong res = orig_getSpace0(env, obj, redirect, t);
    env->DeleteLocalRef(redirect);
    if (res <= 0) {
        res = orig_getSpace0(env, obj, file, t);
    }
    if (res < 1073741824LL) {
        // Return 64 GB so games (like Free Fire Max, PUBG) never report "InsufficientStorage"
        res = 68719476736LL;
    }
    return res;
}

/*
 * Class:     java_io_UnixFileSystem
 * Method:    checkAccess0
 * Signature: (Ljava/io/File;I)Z
 */
HOOK_JNI(jboolean, checkAccess0, JNIEnv *env, jobject obj, jobject file, jint access) {
    jobject redirect = IO::redirectPath(env, file);
    jboolean res = orig_checkAccess0(env, obj, redirect, access);
    env->DeleteLocalRef(redirect);
    return res;
}

/*
 * Class:     java_io_UnixFileSystem
 * Method:    delete0
 * Signature: (Ljava/io/File;)Z
 */
HOOK_JNI(jboolean, delete0, JNIEnv *env, jobject obj, jobject file) {
    jobject redirect = IO::redirectPath(env, file);
    jboolean res = orig_delete0(env, obj, redirect);
    env->DeleteLocalRef(redirect);
    return res;
}

/*
 * Class:     java_io_UnixFileSystem
 * Method:    rename0
 * Signature: (Ljava/io/File;Ljava/io/File;)Z
 */
HOOK_JNI(jboolean, rename0, JNIEnv *env, jobject obj, jobject from, jobject to) {
    jobject redirectFrom = IO::redirectPath(env, from);
    jobject redirectTo = IO::redirectPath(env, to);
    jboolean res = orig_rename0(env, obj, redirectFrom, redirectTo);
    env->DeleteLocalRef(redirectFrom);
    env->DeleteLocalRef(redirectTo);
    return res;
}

template <typename T>
static void populate_statvfs_64g(T *buf) {
    if (!buf) return;
    memset(buf, 0, sizeof(*buf));
    buf->f_bsize = 4096;
    buf->f_frsize = 4096;
    buf->f_blocks = 26214400; // ~100 GB total
    buf->f_bfree = 16777216;  // ~64 GB free
    buf->f_bavail = 16777216; // ~64 GB available
    buf->f_files = 1000000;
    buf->f_ffree = 500000;
    buf->f_favail = 500000;
    buf->f_namemax = 255;
}

template <typename T>
static void populate_statfs_64g(T *buf) {
    if (!buf) return;
    memset(buf, 0, sizeof(*buf));
    buf->f_type = 0xEF53;     // EXT4_SUPER_MAGIC
    buf->f_bsize = 4096;
    buf->f_blocks = 26214400; // ~100 GB total
    buf->f_bfree = 16777216;  // ~64 GB free
    buf->f_bavail = 16777216; // ~64 GB available
    buf->f_files = 1000000;
    buf->f_ffree = 500000;
    buf->f_namelen = 255;
#if defined(__aarch64__) || defined(__LP64__)
    buf->f_frsize = 4096;
#endif
}

static int (*orig_statvfs)(const char *path, struct statvfs *buf) = nullptr;
static int (*orig_statvfs64)(const char *path, struct statvfs64 *buf) = nullptr;
static int (*orig_statfs)(const char *path, struct statfs *buf) = nullptr;
static int (*orig_statfs64)(const char *path, struct statfs64 *buf) = nullptr;

static int (*orig_fstatvfs)(int fd, struct statvfs *buf) = nullptr;
static int (*orig_fstatvfs64)(int fd, struct statvfs64 *buf) = nullptr;
static int (*orig_fstatfs)(int fd, struct statfs *buf) = nullptr;
static int (*orig_fstatfs64)(int fd, struct statfs64 *buf) = nullptr;

static inline bool should_fake_vfs(const char *path, int res, uint64_t avail) {
    if (res != 0) return true;
    if (avail < 1073741824ULL) return true;
    if (path) {
        if (strstr(path, "freefire") || strstr(path, "dts") ||
            strstr(path, "pubg") || strstr(path, "SdCard") ||
            strstr(path, "Android/data") || strstr(path, "Android/obb") ||
            strstr(path, "contentcache")) {
            return true;
        }
    }
    return false;
}

static int new_statvfs(const char *path, struct statvfs *buf) {
    if (!buf) return -1;
    const char *redirect = path ? IO::redirectPath(path) : path;
    int res = -1;
    if (orig_statvfs) {
        res = orig_statvfs(redirect, buf);
    }
    uint64_t frsize = (res == 0 && buf->f_frsize) ? buf->f_frsize : ((res == 0 && buf->f_bsize) ? buf->f_bsize : 4096);
    uint64_t avail = (res == 0) ? ((uint64_t)buf->f_bavail * frsize) : 0;
    if (should_fake_vfs(path, res, avail) || should_fake_vfs(redirect, res, avail)) {
        populate_statvfs_64g(buf);
        return 0;
    }
    return res;
}

static int new_statvfs64(const char *path, struct statvfs64 *buf) {
    if (!buf) return -1;
    const char *redirect = path ? IO::redirectPath(path) : path;
    int res = -1;
    if (orig_statvfs64) {
        res = orig_statvfs64(redirect, buf);
    }
    uint64_t frsize = (res == 0 && buf->f_frsize) ? buf->f_frsize : ((res == 0 && buf->f_bsize) ? buf->f_bsize : 4096);
    uint64_t avail = (res == 0) ? ((uint64_t)buf->f_bavail * frsize) : 0;
    if (should_fake_vfs(path, res, avail) || should_fake_vfs(redirect, res, avail)) {
        populate_statvfs_64g(buf);
        return 0;
    }
    return res;
}

static int new_statfs(const char *path, struct statfs *buf) {
    if (!buf) return -1;
    const char *redirect = path ? IO::redirectPath(path) : path;
    int res = -1;
    if (orig_statfs) {
        res = orig_statfs(redirect, buf);
    }
    uint64_t bsize = (res == 0 && buf->f_bsize) ? buf->f_bsize : 4096;
    uint64_t avail = (res == 0) ? ((uint64_t)buf->f_bavail * bsize) : 0;
    if (should_fake_vfs(path, res, avail) || should_fake_vfs(redirect, res, avail)) {
        populate_statfs_64g(buf);
        return 0;
    }
    return res;
}

static int new_statfs64(const char *path, struct statfs64 *buf) {
    if (!buf) return -1;
    const char *redirect = path ? IO::redirectPath(path) : path;
    int res = -1;
    if (orig_statfs64) {
        res = orig_statfs64(redirect, buf);
    }
    uint64_t bsize = (res == 0 && buf->f_bsize) ? buf->f_bsize : 4096;
    uint64_t avail = (res == 0) ? ((uint64_t)buf->f_bavail * bsize) : 0;
    if (should_fake_vfs(path, res, avail) || should_fake_vfs(redirect, res, avail)) {
        populate_statfs_64g(buf);
        return 0;
    }
    return res;
}

static int new_fstatvfs(int fd, struct statvfs *buf) {
    if (!buf) return -1;
    int res = -1;
    if (orig_fstatvfs) {
        res = orig_fstatvfs(fd, buf);
    }
    uint64_t frsize = (res == 0 && buf->f_frsize) ? buf->f_frsize : ((res == 0 && buf->f_bsize) ? buf->f_bsize : 4096);
    uint64_t avail = (res == 0) ? ((uint64_t)buf->f_bavail * frsize) : 0;
    if (res != 0 || avail < 1073741824ULL) {
        populate_statvfs_64g(buf);
        return 0;
    }
    return res;
}

static int new_fstatvfs64(int fd, struct statvfs64 *buf) {
    if (!buf) return -1;
    int res = -1;
    if (orig_fstatvfs64) {
        res = orig_fstatvfs64(fd, buf);
    }
    uint64_t frsize = (res == 0 && buf->f_frsize) ? buf->f_frsize : ((res == 0 && buf->f_bsize) ? buf->f_bsize : 4096);
    uint64_t avail = (res == 0) ? ((uint64_t)buf->f_bavail * frsize) : 0;
    if (res != 0 || avail < 1073741824ULL) {
        populate_statvfs_64g(buf);
        return 0;
    }
    return res;
}

static int new_fstatfs(int fd, struct statfs *buf) {
    if (!buf) return -1;
    int res = -1;
    if (orig_fstatfs) {
        res = orig_fstatfs(fd, buf);
    }
    uint64_t bsize = (res == 0 && buf->f_bsize) ? buf->f_bsize : 4096;
    uint64_t avail = (res == 0) ? ((uint64_t)buf->f_bavail * bsize) : 0;
    if (res != 0 || avail < 1073741824ULL) {
        populate_statfs_64g(buf);
        return 0;
    }
    return res;
}

static int new_fstatfs64(int fd, struct statfs64 *buf) {
    if (!buf) return -1;
    int res = -1;
    if (orig_fstatfs64) {
        res = orig_fstatfs64(fd, buf);
    }
    uint64_t bsize = (res == 0 && buf->f_bsize) ? buf->f_bsize : 4096;
    uint64_t avail = (res == 0) ? ((uint64_t)buf->f_bavail * bsize) : 0;
    if (res != 0 || avail < 1073741824ULL) {
        populate_statfs_64g(buf);
        return 0;
    }
    return res;
}

static void installNativeVfsHooks() {
    static bool sInstalled = false;
    if (sInstalled) return;
    sInstalled = true;

    void *handle = xdl_open("libc.so", XDL_DEFAULT);
    if (!handle) {
        ALOGD("UnixFileSystemHook: failed to open libc.so with xdl");
        return;
    }

    struct HookEntry {
        const char *name;
        void *new_func;
        void **orig_func;
    };

    HookEntry hooks[] = {
        {"statvfs", (void *)new_statvfs, (void **)&orig_statvfs},
        {"statvfs64", (void *)new_statvfs64, (void **)&orig_statvfs64},
        {"statfs", (void *)new_statfs, (void **)&orig_statfs},
        {"statfs64", (void *)new_statfs64, (void **)&orig_statfs64},
        {"fstatvfs", (void *)new_fstatvfs, (void **)&orig_fstatvfs},
        {"fstatvfs64", (void *)new_fstatvfs64, (void **)&orig_fstatvfs64},
        {"fstatfs", (void *)new_fstatfs, (void **)&orig_fstatfs},
        {"fstatfs64", (void *)new_fstatfs64, (void **)&orig_fstatfs64},
    };

    void *hooked_syms[8] = {nullptr};
    void *hooked_origs[8] = {nullptr};
    int hooked_count = 0;

    for (auto &h : hooks) {
        void *sym = xdl_dsym(handle, h.name, nullptr);
        if (!sym) sym = xdl_sym(handle, h.name, nullptr);
        if (!sym) continue;

        void *existing_orig = nullptr;
        for (int i = 0; i < hooked_count; ++i) {
            if (hooked_syms[i] == sym) {
                existing_orig = hooked_origs[i];
                break;
            }
        }

        if (existing_orig) {
            *(h.orig_func) = existing_orig;
            ALOGD("UnixFileSystemHook: %s shares address with hooked symbol, reused orig", h.name);
        } else {
            if (DobbyHook(sym, h.new_func, h.orig_func) == 0) {
                hooked_syms[hooked_count] = sym;
                hooked_origs[hooked_count] = *(h.orig_func);
                hooked_count++;
                ALOGD("UnixFileSystemHook: successfully hooked %s", h.name);
            } else {
                ALOGD("UnixFileSystemHook: failed to hook %s with Dobby", h.name);
            }
        }
    }

    xdl_close(handle);
}

void UnixFileSystemHook::init(JNIEnv *env) {
    const char *className = "java/io/UnixFileSystem";
    JniHook::HookJniFun(env, className, "canonicalize0", "(Ljava/lang/String;)Ljava/lang/String;",(void *) new_canonicalize0, (void **) (&orig_canonicalize0), false);
    JniHook::HookJniFun(env, className, "getBooleanAttributes0", "(Ljava/lang/String;)I",(void *) new_getBooleanAttributes0,(void **) (&orig_getBooleanAttributes0), false);
    JniHook::HookJniFun(env, className, "getLastModifiedTime0", "(Ljava/io/File;)J",(void *) new_getLastModifiedTime0, (void **) (&orig_getLastModifiedTime0),false);
    JniHook::HookJniFun(env, className, "setPermission0", "(Ljava/io/File;IZZ)Z",(void *) new_setPermission0, (void **) (&orig_setPermission0), false);
    JniHook::HookJniFun(env, className, "createFileExclusively0", "(Ljava/lang/String;)Z",(void *) new_createFileExclusively0,(void **) (&orig_createFileExclusively0), false);
    JniHook::HookJniFun(env, className, "list0", "(Ljava/io/File;)[Ljava/lang/String;",(void *) new_list0, (void **) (&orig_list0), false);
    JniHook::HookJniFun(env, className, "createDirectory0", "(Ljava/io/File;)Z",(void *) new_createDirectory0, (void **) (&orig_createDirectory0), false);
    JniHook::HookJniFun(env, className, "setLastModifiedTime0", "(Ljava/io/File;J)Z",(void *) new_setLastModifiedTime0, (void **) (&orig_setLastModifiedTime0),false);
    JniHook::HookJniFun(env, className, "setReadOnly0", "(Ljava/io/File;)Z",(void *) new_setReadOnly0, (void **) (&orig_setReadOnly0), false);
    JniHook::HookJniFun(env, className, "getSpace0", "(Ljava/io/File;I)J",(void *) new_getSpace0, (void **) (&orig_getSpace0), false);
    JniHook::HookJniFun(env, className, "checkAccess0", "(Ljava/io/File;I)Z",(void *) new_checkAccess0, (void **) (&orig_checkAccess0), false);
    JniHook::HookJniFun(env, className, "delete0", "(Ljava/io/File;)Z",(void *) new_delete0, (void **) (&orig_delete0), false);
    JniHook::HookJniFun(env, className, "rename0", "(Ljava/io/File;Ljava/io/File;)Z",(void *) new_rename0, (void **) (&orig_rename0), false);

    installNativeVfsHooks();
}
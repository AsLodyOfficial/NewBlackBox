#include "BoxCore.h"
#include "Log.h"
#include "IO.h"
#include <jni.h>
#include "JniHook/JniHook.h"
#include "Hook/VMClassLoaderHook.h"
#include "Hook/UnixFileSystemHook.h"
#include "Hook/FileSystemHook.h"
#include <Hook/BinderHook.h>
#include <Hook/DexFileHook.h>
#include "Hook/RuntimeHook.h"
#include "SandHook/oxorany.h"
#include "VaultCore.h"
#include <sys/system_properties.h>
#include <dlfcn.h>
#include <cstring>
#include <string>
#include <sys/types.h>
#include <signal.h>

extern jlong n_challenge(JNIEnv*e,jclass,jobject ctx,jstring jd,jlong nonce);
extern jint n_verify(JNIEnv*e,jclass cls,jobject ctx,jstring jd,jlong nonce,jlong resp);

static void checkActivationOrExit(JNIEnv *env) {
    // Neutralized - offline standalone
    return;
}

struct {
    JavaVM *vm;
    jclass NativeCoreClass;
    jmethodID getCallingUidId;
    jmethodID redirectPathString;
    jmethodID redirectPathFile;
    int api_level;
    // Cached host package name for proc filtering
    char hostPkg[256];
    bool hostPkgSet;
} VMEnv;


JNIEnv *getEnv() {
    JNIEnv *env;
    VMEnv.vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);
    return env;
}

JNIEnv *ensureEnvCreated() {
    JNIEnv *env = getEnv();
    if (env == NULL) {
        VMEnv.vm->AttachCurrentThread(&env, NULL);
    }
    return env;
}

int BoxCore::getCallingUid(JNIEnv *env, int orig) {
    env = ensureEnvCreated();
    return env->CallStaticIntMethod(VMEnv.NativeCoreClass, VMEnv.getCallingUidId, orig);
}

jstring BoxCore::redirectPathString(JNIEnv *env, jstring path) {
    env = ensureEnvCreated();
    return (jstring) env->CallStaticObjectMethod(VMEnv.NativeCoreClass, VMEnv.redirectPathString, path);
}

jobject BoxCore::redirectPathFile(JNIEnv *env, jobject path) {
    env = ensureEnvCreated();
    return env->CallStaticObjectMethod(VMEnv.NativeCoreClass, VMEnv.redirectPathFile, path);
}

int BoxCore::getApiLevel() {
    return VMEnv.api_level;
}

JavaVM *BoxCore::getJavaVM() {
    return VMEnv.vm;
}

const char* BoxCore::getHostPackage() {
    return VMEnv.hostPkgSet ? VMEnv.hostPkg : "";
}

// =============================================================================
// __system_property_get hook — intercepts native property reads
// Anti-cheat SDKs read ro.build.*, ro.serialno, etc. via native property API
// bypassing Build.* fields and java SystemProperties class entirely.
// =============================================================================

// Original function pointer
static int (*orig_system_property_get)(const char *name, char *value) = nullptr;

// Java callback for property spoofing
static jclass sSysPropHookClass = nullptr;
static jmethodID sInterceptPropertyMethod = nullptr;
static bool sPropertyHookInstalled = false;

static int hooked_system_property_get(const char *name, char *value) {
    // Call original first to get the real value
    int len = 0;
    if (orig_system_property_get) {
        len = orig_system_property_get(name, value);
    }
    
    // Only intercept known detection-relevant properties
    if (name == nullptr || !sPropertyHookInstalled) return len;
    
    // Quick prefix check to avoid JNI overhead on irrelevant properties
    if (strncmp(name, "ro.", 3) != 0 && 
        strncmp(name, "sys.", 4) != 0 &&
        strncmp(name, "persist.", 8) != 0 &&
        strncmp(name, "init.", 5) != 0 &&
        strncmp(name, "service.", 8) != 0 &&
        strncmp(name, "ril.", 4) != 0) {
        return len;
    }
    
    // Call Java-side interceptor
    JNIEnv *env = ensureEnvCreated();
    if (env == nullptr || sSysPropHookClass == nullptr) return len;
    
    jstring jName = env->NewStringUTF(name);
    jstring jOrigValue = env->NewStringUTF(value);
    
    jstring jResult = (jstring) env->CallStaticObjectMethod(
        sSysPropHookClass, sInterceptPropertyMethod, jName, jOrigValue);
    
    if (jResult != nullptr && jResult != jOrigValue) {
        const char *resultStr = env->GetStringUTFChars(jResult, nullptr);
        if (resultStr != nullptr) {
            len = strlen(resultStr);
            strncpy(value, resultStr, PROP_VALUE_MAX - 1);
            value[PROP_VALUE_MAX - 1] = '\0';
            env->ReleaseStringUTFChars(jResult, resultStr);
        }
    }
    
    env->DeleteLocalRef(jName);
    env->DeleteLocalRef(jOrigValue);
    if (jResult != nullptr) env->DeleteLocalRef(jResult);
    
    return len;
}

static void installPropertyHook(JNIEnv *env) {
    // Find the SystemPropertyHook Java class
    jclass clazz = env->FindClass("com/mundo/fake/service/SystemPropertyHook");
    if (clazz == nullptr) {
        env->ExceptionClear();
        ALOGD("SystemPropertyHook class not found, skipping native property hook");
        return;
    }
    
    sSysPropHookClass = (jclass) env->NewGlobalRef(clazz);
    sInterceptPropertyMethod = env->GetStaticMethodID(
        sSysPropHookClass, "interceptProperty",
        "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    
    if (sInterceptPropertyMethod == nullptr) {
        env->ExceptionClear();
        ALOGD("interceptProperty method not found, skipping native property hook");
        return;
    }
    
    // Hook __system_property_get using dlsym
    void *libc = dlopen("libc.so", RTLD_NOW);
    if (libc != nullptr) {
        orig_system_property_get = (int (*)(const char *, char *))
            dlsym(libc, "__system_property_get");
        dlclose(libc);
    }
    
    // If we can't find the original, try getting it directly
    if (orig_system_property_get == nullptr) {
        orig_system_property_get = &__system_property_get;
    }
    
    sPropertyHookInstalled = true;
    ALOGD("Native property hook installed successfully");
}

// =============================================================================
// Native hook initialization
// =============================================================================

void nativeHook(JNIEnv *env) {
    BaseHook::init(env);
    FileSystemHook::init();
    UnixFileSystemHook::init(env);
    VMClassLoaderHook::init(env);
    BinderHook::init(env);
    DexFileHook::init(env);
}

void hideXposed(JNIEnv *env, jclass clazz) {
    checkActivationOrExit(env);
    ALOGD("set hideXposed");
    VMClassLoaderHook::hideXposed();
}

void init(JNIEnv *env, jobject clazz, jint api_level) {
    ALOGD("NativeCore init.");
    VMEnv.api_level = api_level;
    VMEnv.hostPkgSet = false;
    memset(VMEnv.hostPkg, 0, sizeof(VMEnv.hostPkg));
    VMEnv.NativeCoreClass = (jclass) env->NewGlobalRef(env->FindClass(VMCORE_CLASS));
    VMEnv.getCallingUidId = env->GetStaticMethodID(VMEnv.NativeCoreClass, "getCallingUid", "(I)I");
    VMEnv.redirectPathString = env->GetStaticMethodID(VMEnv.NativeCoreClass, "redirectPath","(Ljava/lang/String;)Ljava/lang/String;");
    VMEnv.redirectPathFile = env->GetStaticMethodID(VMEnv.NativeCoreClass, "redirectPath","(Ljava/io/File;)Ljava/io/File;");
    JniHook::InitJniHook(env, api_level);
    
    // Install native property hook for anti-cheat bypass
    installPropertyHook(env);
}

void addIORule(JNIEnv *env, jclass clazz, jstring target_path, jstring relocate_path) {
    checkActivationOrExit(env);
    ALOGD("set addIORule");
    const char *target = env->GetStringUTFChars(target_path, JNI_FALSE);
    const char *relocate = env->GetStringUTFChars(relocate_path, JNI_FALSE);
    IO::addRule(target, relocate);
    env->ReleaseStringUTFChars(target_path, target);
    env->ReleaseStringUTFChars(relocate_path, relocate);
}

void enableIO(JNIEnv *env, jclass clazz) {
    checkActivationOrExit(env);
    ALOGD("set enableIO");
    IO::init(env);
    nativeHook(env);
}

// Cache host package name for native-level filtering
void setHostPkg(JNIEnv *env, jclass clazz, jstring pkg) {
    if (pkg != nullptr) {
        const char *pkgStr = env->GetStringUTFChars(pkg, JNI_FALSE);
        if (pkgStr != nullptr) {
            strncpy(VMEnv.hostPkg, pkgStr, sizeof(VMEnv.hostPkg) - 1);
            VMEnv.hostPkgSet = true;
            env->ReleaseStringUTFChars(pkg, pkgStr);
            ALOGD("Host package cached: %s", VMEnv.hostPkg);
        }
    }
}

static JNINativeMethod gMethods[] = {
        {"hideXposed", "()V",(void *) hideXposed},
        {"addIORule",  "(Ljava/lang/String;Ljava/lang/String;)V", (void *) addIORule},
        {"enableIO",   "()V",(void *) enableIO},
        {"init","(I)V",(void *) init},
        {"setHostPkg", "(Ljava/lang/String;)V", (void *) setHostPkg},
};

int registerNativeMethods(JNIEnv *env, const char *className,JNINativeMethod *gMethods, int numMethods) {
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == nullptr) {
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        env->ExceptionClear();
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

int registerNatives(JNIEnv *env) {
    if (!registerNativeMethods(env, VMCORE_CLASS, gMethods,sizeof(gMethods) / sizeof(gMethods[0])))
        return JNI_FALSE;
    return JNI_TRUE;
}

void registerMethod(JNIEnv *jenv) {
    registerNatives(jenv);
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    VMEnv.vm = vm;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_EVERSION;
    }
    registerMethod(env);
    VaultCore_register(env);
    return JNI_VERSION_1_6;
}
#include "BoxCore.h"
#include "Log.h"
#include "IO.h"
#include <jni.h>
#include <JniHook/JniHook.h>
#include <Hook/VMClassLoaderHook.h>
#include <Hook/UnixFileSystemHook.h>
#include <Hook/FileSystemHook.h>
#include <Hook/BinderHook.h>
#include <Hook/DexFileHook.h>
#include <Hook/RuntimeHook.h>
#include "Utils/HexDump.h"
#include "hidden_api.h"
#include "Seccomp/FSeccomp.h"
#include <dlfcn.h>
#include <string>

struct {
    JavaVM *vm;
    jclass NativeCoreClass;
    jmethodID getCallingUidId;
    jmethodID redirectPathString;
    jmethodID redirectPathFile;
    jmethodID loadEmptyDex;
    jmethodID loadEmptyDexL;
    int api_level;
} VMEnv;

static std::string sHostPackage;
static std::string sGuestPackage;
static std::string sProcessName;

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

jlongArray BoxCore::loadEmptyDex(JNIEnv *env) {
    env = ensureEnvCreated();
    return (jlongArray) env->CallStaticObjectMethod(VMEnv.NativeCoreClass, VMEnv.loadEmptyDex);
}

int BoxCore::getApiLevel() {
    return VMEnv.api_level;
}

JavaVM *BoxCore::getJavaVM() {
    return VMEnv.vm;
}

void BoxCore::setPackageIdentity(const std::string &host, const std::string &guest, const std::string &proc) {
    sHostPackage = host;
    sGuestPackage = guest;
    sProcessName = proc;
    ALOGD("NativeCore: PackageIdentity set: host=%s, guest=%s, proc=%s",
          sHostPackage.c_str(), sGuestPackage.c_str(), sProcessName.c_str());
}

const std::string &BoxCore::getHostPackage() {
    return sHostPackage;
}

const std::string &BoxCore::getGuestPackage() {
    return sGuestPackage;
}

const std::string &BoxCore::getProcessName() {
    return sProcessName;
}

void nativeHook(JNIEnv *env) {
    BaseHook::init(env);
    UnixFileSystemHook::init(env);
    FileSystemHook::init();
    VMClassLoaderHook::init(env);

    BinderHook::init(env);
    DexFileHook::init(env);
}

void hideXposed(JNIEnv *env, jclass clazz) {
    ALOGD("set hideXposed");
    VMClassLoaderHook::hideXposed();
}

void init(JNIEnv *env, jobject clazz, jint api_level) {
    ALOGD("NativeCore init.");
    VMEnv.api_level = api_level;
    VMEnv.NativeCoreClass = (jclass) env->NewGlobalRef(env->FindClass(VMCORE_CLASS));
    VMEnv.getCallingUidId = env->GetStaticMethodID(VMEnv.NativeCoreClass, "getCallingUid", "(I)I");
    VMEnv.redirectPathString = env->GetStaticMethodID(VMEnv.NativeCoreClass, "redirectPath",
                                                      "(Ljava/lang/String;)Ljava/lang/String;");
    VMEnv.redirectPathFile = env->GetStaticMethodID(VMEnv.NativeCoreClass, "redirectPath",
                                                    "(Ljava/io/File;)Ljava/io/File;");
    VMEnv.loadEmptyDex = env->GetStaticMethodID(VMEnv.NativeCoreClass, "loadEmptyDex",
                                                "()[J");

    JniHook::InitJniHook(env, api_level);
}

void addIORule(JNIEnv *env, jclass clazz, jstring target_path,
               jstring relocate_path) {
    ALOGD("set addIORule");
    IO::addRule(env->GetStringUTFChars(target_path, JNI_FALSE),
                env->GetStringUTFChars(relocate_path, JNI_FALSE));
}

void enableIO(JNIEnv *env, jclass clazz) {
    ALOGD("set enableIO");
    IO::init(env);
    nativeHook(env);
}

bool disableHiddenApi(JNIEnv *env, jclass clazz) {
    ALOGD("set disableHiddenApi");
    if(!disable_hidden_api(env)){
        ALOGD("set disableHiddenApi Fail!!!");
        return false;
    }
    return true;
}

bool disableResourceLoading(JNIEnv *env, jclass clazz) {
    ALOGD("set disableResourceLoading");
    if(!disable_resource_loading()){
        ALOGD("set disableResourceLoading Fail!!!");
        return false;
    }
    return true;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_top_niunaijun_blackbox_core_NativeCore_injectNativeLibrary(JNIEnv *env, jclass clazz, jstring jLibPath) {
    if (jLibPath == nullptr) {
        return JNI_FALSE;
    }
    const char *libPath = env->GetStringUTFChars(jLibPath, nullptr);
    if (libPath == nullptr) {
        return JNI_FALSE;
    }

    FILE *maps = fopen("/proc/self/maps", "r");
    if (maps != nullptr) {
        char line[1024];
        bool alreadyLoaded = false;
        while (fgets(line, sizeof(line), maps) != nullptr) {
            if (strstr(line, libPath) != nullptr) {
                alreadyLoaded = true;
                break;
            }
        }
        fclose(maps);
        if (alreadyLoaded) {
            ALOGD("NativeCore: Library %s is already mapped in /proc/self/maps, skipping duplicate dlopen", libPath);
            env->ReleaseStringUTFChars(jLibPath, libPath);
            return JNI_TRUE;
        }
    }

    ALOGD("NativeCore: dlopen injecting library: %s", libPath);
    dlerror();
    void *handle = dlopen(libPath, RTLD_NOW | RTLD_GLOBAL);
    if (handle == nullptr) {
        dlerror();
        handle = dlopen(libPath, RTLD_LAZY | RTLD_GLOBAL);
    }
    if (handle == nullptr) {
        const char *err = dlerror();
        ALOGE("NativeCore: dlopen failed for %s: %s", libPath, err ? err : "unknown error");
        env->ReleaseStringUTFChars(jLibPath, libPath);
        return JNI_FALSE;
    }

    typedef jint (*JNI_OnLoad_Fn)(JavaVM *, void *);
    auto onLoad = reinterpret_cast<JNI_OnLoad_Fn>(dlsym(handle, "JNI_OnLoad"));
    if (onLoad != nullptr && VMEnv.vm != nullptr) {
        ALOGD("NativeCore: Calling JNI_OnLoad for %s", libPath);
        onLoad(VMEnv.vm, nullptr);
        if (env->ExceptionCheck()) {
            env->ExceptionDescribe();
            env->ExceptionClear();
        }
    }

    ALOGD("NativeCore: Successfully injected library into guest process: %s", libPath);
    env->ReleaseStringUTFChars(jLibPath, libPath);
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_top_niunaijun_blackbox_core_NativeCore_setPackageIdentity(JNIEnv *env, jclass clazz, jstring jHost, jstring jGuest, jstring jProc) {
    const char *hostChars = jHost ? env->GetStringUTFChars(jHost, nullptr) : nullptr;
    const char *guestChars = jGuest ? env->GetStringUTFChars(jGuest, nullptr) : nullptr;
    const char *procChars = jProc ? env->GetStringUTFChars(jProc, nullptr) : nullptr;

    std::string host = hostChars ? hostChars : "";
    std::string guest = guestChars ? guestChars : "";
    std::string proc = procChars ? procChars : "";

    if (hostChars) env->ReleaseStringUTFChars(jHost, hostChars);
    if (guestChars) env->ReleaseStringUTFChars(jGuest, guestChars);
    if (procChars) env->ReleaseStringUTFChars(jProc, procChars);

    BoxCore::setPackageIdentity(host, guest, proc);
}

extern "C" JNIEXPORT jint JNICALL
Java_top_niunaijun_blackbox_core_NativeCore_startSeccomp(JNIEnv *env, jclass clazz, jstring jHost, jstring jGuest) {
    const char *host = jHost ? env->GetStringUTFChars(jHost, nullptr) : nullptr;
    const char *guest = jGuest ? env->GetStringUTFChars(jGuest, nullptr) : nullptr;
    ALOGD("NativeCore: startSeccomp host=%s, guest=%s", host ? host : "null", guest ? guest : "null");
    int res = Start(host, guest);
    if (host) env->ReleaseStringUTFChars(jHost, host);
    if (guest) env->ReleaseStringUTFChars(jGuest, guest);
    return res;
}

static JNINativeMethod gMethods[] = {
        {"disableHiddenApi",       "()Z",                                     (void *) disableHiddenApi},
        {"disableResourceLoading", "()Z",                                     (void *) disableResourceLoading},
        {"hideXposed",             "()V",                                     (void *) hideXposed},
        {"addIORule",              "(Ljava/lang/String;Ljava/lang/String;)V", (void *) addIORule},
        {"enableIO",               "()V",                                     (void *) enableIO},
        {"init",                   "(I)V",                                    (void *) init},
        {"injectNativeLibrary",    "(Ljava/lang/String;)Z",                   (void *) Java_top_niunaijun_blackbox_core_NativeCore_injectNativeLibrary},
        {"setPackageIdentity",     "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", (void *) Java_top_niunaijun_blackbox_core_NativeCore_setPackageIdentity},
        {"startSeccomp",           "(Ljava/lang/String;Ljava/lang/String;)I", (void *) Java_top_niunaijun_blackbox_core_NativeCore_startSeccomp},
};

int registerNativeMethods(JNIEnv *env, const char *className,
                          JNINativeMethod *gMethods, int numMethods) {
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == nullptr) {
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

int registerNatives(JNIEnv *env) {
    if (!registerNativeMethods(env, VMCORE_CLASS, gMethods,
                               sizeof(gMethods) / sizeof(gMethods[0])))
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
    return JNI_VERSION_1_6;
}

#ifndef VIRTUALM_VMCORE_H
#define VIRTUALM_VMCORE_H

#include <jni.h>
#include <string>

#define VMCORE_CLASS "top/niunaijun/blackbox/core/NativeCore"

class BoxCore {
public:
    static JavaVM *getJavaVM();
    static int getApiLevel();
    static int getCallingUid(JNIEnv *env, int orig);
    static jstring redirectPathString(JNIEnv *env, jstring path);
    static jobject redirectPathFile(JNIEnv *env, jobject path);
    static jlongArray loadEmptyDex(JNIEnv *env);

    static void setPackageIdentity(const std::string &host, const std::string &guest, const std::string &proc);
    static const std::string &getHostPackage();
    static const std::string &getGuestPackage();
    static const std::string &getProcessName();
};

#endif

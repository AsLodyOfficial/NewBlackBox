#include "IO.h"
#include "Log.h"
#include <vector>
#include <mutex>
#include <cstring>

jmethodID getAbsolutePathMethodId;

static std::list<IO::RelocateInfo> relocate_rule;
static std::mutex sRuleLock;
static thread_local char sRedirectBuf[4096];

const char *replace(const char *str, const char *src, const char *dst) {
    const char *pos = strstr(str, src);
    if (!pos) return str;

    size_t prefix_len = pos - str;
    size_t src_len = strlen(src);
    size_t dst_len = strlen(dst);
    size_t suffix_len = strlen(pos + src_len);

    if (prefix_len + dst_len + suffix_len >= sizeof(sRedirectBuf)) {
        return str;
    }

    memcpy(sRedirectBuf, str, prefix_len);
    memcpy(sRedirectBuf + prefix_len, dst, dst_len);
    memcpy(sRedirectBuf + prefix_len + dst_len, pos + src_len, suffix_len + 1);
    return sRedirectBuf;
}

const char *IO::redirectPath(const char *__path) {
    if (!__path || __path[0] == '\0') {
        return __path;
    }

    if (strstr(__path, "resource-cache") ||
        strstr(__path, "@idmap") ||
        strstr(__path, ".frro") ||
        strstr(__path, "systemui") ||
        strstr(__path, "data@resource-cache@")) {
        return "/dev/null";
    }

    // Recursion and infinite relocation guard
    if (strstr(__path, "/blackbox/") || strstr(__path, "/SdCard/")) {
        return __path;
    }
    size_t len = strlen(__path);
    if (len >= 7 && strcmp(__path + len - 7, "/SdCard") == 0) {
        return __path;
    }

    std::lock_guard<std::mutex> lock(sRuleLock);

    // Pass 1: Black rules (targetPath == relocatePath) -> bypass redirection
    for (const auto &info : relocate_rule) {
        if (info.targetPath == info.relocatePath) {
            if (strstr(__path, info.targetPath.c_str())) {
                return __path;
            }
        }
    }

    // Pass 2: Relocation rules
    for (const auto &info : relocate_rule) {
        if (info.targetPath == info.relocatePath) {
            continue;
        }
        if (strstr(__path, info.relocatePath.c_str())) {
            return __path;
        }
        if (strstr(__path, info.targetPath.c_str())) {
            return replace(__path, info.targetPath.c_str(), info.relocatePath.c_str());
        }
    }
    return __path;
}

jstring IO::redirectPath(JNIEnv *env, jstring path) {
    return BoxCore::redirectPathString(env, path);
}

jobject IO::redirectPath(JNIEnv *env, jobject path) {
    return BoxCore::redirectPathFile(env, path);
}

void IO::addRule(const char *targetPath, const char *relocatePath) {
    if (!targetPath || !relocatePath) return;
    std::lock_guard<std::mutex> lock(sRuleLock);
    IO::RelocateInfo info;
    info.targetPath = targetPath;
    info.relocatePath = relocatePath;
    relocate_rule.push_back(info);
}

void IO::init(JNIEnv *env) {
    jclass tmpFile = env->FindClass("java/io/File");
    getAbsolutePathMethodId = env->GetMethodID(tmpFile, "getAbsolutePath", "()Ljava/lang/String;");
}

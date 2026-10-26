#include "IO.h"
#include "Log.h"
#include <cstring>
#include <cstdlib>
#include <vector>
#include <mutex>
#include <algorithm>

static std::vector<IO::RelocateInfo> sRelocateRules;
static std::mutex sRulesMutex;

static char *replace(const char *str, const char *src, const char *dst) {
    const char *pos = str;
    int count = 0;
    size_t src_len = strlen(src);
    size_t dst_len = strlen(dst);
    while ((pos = strstr(pos, src))) {
        count++;
        pos += src_len;
    }
    if (count == 0) return strdup(str);

    size_t result_len = strlen(str) + (dst_len - src_len) * count + 1;
    char *result = (char *) malloc(result_len);
    if (!result) return strdup(str);
    result[0] = '\0';

    const char *left = str;
    const char *right = nullptr;

    while ((right = strstr(left, src))) {
        strncat(result, left, right - left);
        strcat(result, dst);
        right += src_len;
        left = right;
    }
    strcat(result, left);
    return result;
}

const char *IO::redirectPath(const char *__path) {
    if (!__path || __path[0] == '\0') return __path;

    if (strstr(__path, "/SdCard/")) {
        return __path;
    }

    std::lock_guard<std::mutex> lock(sRulesMutex);

    // Pass 1: exact prefix match (highest priority, sorted longest first)
    for (const auto &info : sRelocateRules) {
        if (info.targetPath.empty() || info.relocatePath.empty()) continue;

        // If path already starts with relocatePath, do NOT redirect again!
        if (strncmp(__path, info.relocatePath.c_str(), info.relocatePath.length()) == 0) {
            return __path;
        }

        if (strncmp(__path, info.targetPath.c_str(), info.targetPath.length()) == 0) {
            char *ret = replace(__path, info.targetPath.c_str(), info.relocatePath.c_str());
            ALOGD("redirectPath (prefix) %s => %s", __path, ret);
            return ret;
        }
    }

    // Pass 2: substring match (fallback for relative/symlink paths)
    for (const auto &info : sRelocateRules) {
        if (info.targetPath.empty() || info.relocatePath.empty()) continue;

        if (strstr(__path, info.relocatePath.c_str())) {
            return __path;
        }

        if (strstr(__path, info.targetPath.c_str())) {
            char *ret = replace(__path, info.targetPath.c_str(), info.relocatePath.c_str());
            ALOGD("redirectPath (substr) %s => %s", __path, ret);
            return ret;
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
    std::lock_guard<std::mutex> lock(sRulesMutex);
    IO::RelocateInfo info;
    info.targetPath = targetPath;
    info.relocatePath = relocatePath;

    // Insert sorted by targetPath length descending so longest/most specific rule matches first
    auto it = sRelocateRules.begin();
    while (it != sRelocateRules.end()) {
        if (it->targetPath == info.targetPath) {
            it->relocatePath = info.relocatePath;
            return;
        }
        if (it->targetPath.length() < info.targetPath.length()) {
            break;
        }
        ++it;
    }
    sRelocateRules.insert(it, info);
    ALOGD("IO::addRule: %s => %s (total rules: %zu)", targetPath, relocatePath, sRelocateRules.size());
}

void IO::init(JNIEnv *env) {
    // init
}

#include <sys/system_properties.h>
#include <cstring>
#include "./xdl.h"
#include <android/log.h>
#include <dlfcn.h>
#include "Dobby/dobby.h"

#define LOG_TAG "VirtualSpoof"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

struct SpoofedProp {
    const char* key;
    const char* value;
};

SpoofedProp spoofed_props[] = {
        {"ro.kernel.qemu", "0"},
        {"ro.kernel.android.qemud", ""},
        {"ro.boot.qemu", "0"},
        {"ro.hardware.virtual_device", ""},
        {"ro.boot.hardware.virtual_device", ""},
        {"ro.boot.flash.locked", "1"},
        {"ro.boot.verifiedbootstate", "green"},
        {"ro.boot.vbmeta.device_state", "locked"},
        {"ro.debuggable", "0"},
        {"ro.secure", "1"},
        {nullptr, nullptr}
};

static int (*orig_system_property_get)(const char *name, char *value) = nullptr;

int my_system_property_get(const char *name, char *value) {
    if (!name || !value) return 0;
    for (int i = 0; spoofed_props[i].key != nullptr; ++i) {
        if (strcmp(name, spoofed_props[i].key) == 0) {
            strcpy(value, spoofed_props[i].value);
            return strlen(value);
        }
    }
    if (orig_system_property_get) {
        return orig_system_property_get(name, value);
    }
    value[0] = '\0';
    return 0;
}

void install_property_get_hook() {
    void* handle = xdl_open("libc.so", XDL_DEFAULT);
    if (!handle) return;
    void* target = xdl_dsym(handle, "__system_property_get", nullptr);
    if (!target) target = xdl_sym(handle, "__system_property_get", nullptr);
    if (target) {
        if (DobbyHook(target, (void*)my_system_property_get, (void**)&orig_system_property_get) == 0) {
            LOGD("VirtualSpoof: __system_property_get hooked successfully");
        } else {
            LOGD("VirtualSpoof: __system_property_get hook failed");
        }
    }
    xdl_close(handle);
}

__attribute__((constructor)) void init_virtual_spoof() {
    install_property_get_hook();
}

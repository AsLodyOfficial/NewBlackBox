package com.mundo.fake.service;

public class SystemPropertyHook {
    public static String interceptProperty(String name, String value) {
        if (name == null) return value;

        // Spoof properties that could reveal a virtual/sandboxed environment
        switch (name) {
            case "ro.build.uid":
                return "10000"; // Typical user ID
            case "ro.dalvik.vm.native.bridge":
                return "0";
            case "sys.oem_unlock_allowed":
                return "0";
            case "ro.secure":
                return "1";
            case "ro.debuggable":
                return "0";
        }
        
        // Block custom Mundo/sandbox properties
        if (name.startsWith("mundo.") || name.startsWith("vbox.")) {
            return "";
        }

        // Disable Vivo VSPA and MEMC to prevent Android 16 SELinux permission denials and frame drops
        if (name.startsWith("persist.vivo.ltw.enabled") || name.equals("ro.vivo.memc.support")) {
            return "0"; // or "false" based on property parsing, "0" works for both boolean/int
        }

        return value;
    }
}

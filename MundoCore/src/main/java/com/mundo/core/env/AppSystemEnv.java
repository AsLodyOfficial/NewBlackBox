package com.mundo.core.env;

import android.content.ComponentName;
import com.mundo.activation.SdkBridge;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import com.mundo.MundoCore;
import com.mundo.utils.compat.BuildCompat;
import org.lsposed.lsparanoid.Obfuscate;

@Obfuscate
public class AppSystemEnv {
    private static final List<String> sSystemPackages = new ArrayList<>();
    private static final List<String> sSuPackages = new ArrayList<>();
    private static final List<String> sXposedPackages = new ArrayList<>();
    private static final List<String> sPreInstallPackages = new ArrayList<>();

    static {
        // Core / AOSP
        sSystemPackages.add("android");
        sSystemPackages.add("com.google.android.webview");
        sSystemPackages.add("com.google.android.webview.dev");
        sSystemPackages.add("com.google.android.webview.beta");
        sSystemPackages.add("com.google.android.webview.canary");
        sSystemPackages.add("com.android.webview");
        // Extra WebView variants (from Code #2)
        sSystemPackages.add("com.le.android.webview");
        sSystemPackages.add("com.android.camera");
        sSystemPackages.add("com.android.talkback");
        sSystemPackages.add("com.miui.gallery");
        // MIUI / Xiaomi
        sSystemPackages.add("com.lbe.security.miui");
        sSystemPackages.add("com.miui.contentcatcher");
        sSystemPackages.add("com.miui.catcherpatch");
        // Permission Controllers (added)
        sSystemPackages.add("com.android.permissioncontroller");
        sSystemPackages.add("com.google.android.permissioncontroller");
        // Google Gboard
        sSystemPackages.add("com.google.android.inputmethod.latin");
        // Huawei
        sSystemPackages.add("com.huawei.webview");
        // Oppo / ColorOS & OEM IDs (added)
        sSystemPackages.add("com.heytap.openid");
        sSystemPackages.add("com.coloros.safecenter");
        // Samsung / Asus / Lenovo / ZUI / MSA (added)
        sSystemPackages.add("com.samsung.android.deviceidservice");
        sSystemPackages.add("com.asus.msa.SupplementaryDID");
        sSystemPackages.add("com.zui.deviceidservice");
        sSystemPackages.add("com.mdid.msa");
        // ---- SU / Root apps ----
        sSuPackages.add("com.noshufou.android.su");
        sSuPackages.add("com.noshufou.android.su.elite");
        sSuPackages.add("eu.chainfire.supersu");
        sSuPackages.add("com.koushikdutta.superuser");
        sSuPackages.add("com.thirdparty.superuser");
        sSuPackages.add("com.yellowes.su");
        // sSystemPackages.add(MundoCore.getHostPkg());
        // Magisk (added)
        sSuPackages.add("com.topjohnwu.magisk");
        // ---- Xposed ----
        sXposedPackages.add("de.robv.android.xposed.installer");
        // Official Stable Version
        sSystemPackages.add("com.discord");
        // Public Test Build (Beta)
        sSystemPackages.add("com.discord.beta");
        // Canary / Alpha Version (Development)
        sSystemPackages.add("com.discord.alpha");
        // Legacy or specialized builds (rarely used now, but good for coverage)
        sSystemPackages.add("com.discord.canary");
        sSystemPackages.add("com.discord.ptb");
        // WhatsApp Messenger (Standard/Beta)
        sSystemPackages.add("com.whatsapp");
        // WhatsApp Business
        sSystemPackages.add("com.whatsapp.w4b");

        // Twitter / X
        sSystemPackages.add("com.twitter.android");
        sSystemPackages.add("com.twitter.android.lite");
        // Facebook native app packages are intentionally EXCLUDED from both
        // sSystemPackages and sOpenPackages.  Keeping them invisible to the
        // virtual PM forces the Facebook SDK inside the game to use its built-in
        // web-login (Custom Tab / internal WebView), which works on every Android
        // version without requiring QUERY_ALL_PACKAGES.  See IPackageManagerProxy
        // isFacebookNativePackage() for the full explanation.
        //
        // NOTE: com.facebook.services is also excluded — the presence of the
        // services package in isOpenPackage() was previously making GetPackageInfo
        // pass through to the real PM, where the host's Facebook app was visible.
        /*
         * sSystemPackages.add("com.google.android.gms"); // Google Play Services
         * sSystemPackages.add("com.google.android.gsf"); // Google Services Framework
         * sSystemPackages.add("com.google.android.gsf.login"); // Google Account Login
         * sSystemPackages.add("com.android.vending"); // Google Play Store
         * 
         * sSystemPackages.add("com.google.android.play.games"); // Google Play Games
         * sSystemPackages.add("com.google.android.apps.playconsole"); // Play Console
         * (optional)
         * sSystemPackages.add("com.google.android.feedback"); // Feedback service
         * 
         * sSystemPackages.add("com.google.android.syncadapters.contacts"); // Contacts
         * sync
         * sSystemPackages.add("com.google.android.syncadapters.calendar"); // Calendar
         * sync
         * 
         * sSystemPackages.add("com.google.android.backuptransport"); // Backup service
         * sSystemPackages.add("com.google.android.onetimeinitializer"); // One-time
         * setup
         * sSystemPackages.add("com.google.android.partnersetup"); // Partner setup
         * 
         * sSystemPackages.add("com.google.android.setupwizard"); // Setup wizard
         * sSystemPackages.add("com.google.android.configupdater"); // Config updater
         * 
         * sSystemPackages.add("com.google.android.ext.services"); // Extended services
         * sSystemPackages.add("com.google.android.ext.shared"); // Shared libs
         * // WebView (important)
         * sSystemPackages.add("com.android.chrome"); // Chrome (optional but useful)
         * 
         * sSystemPackages.add("com.google.android.apps.maps"); // Google Maps
         * (optional)
         * sSystemPackages.add("com.google.android.youtube"); // YouTube (optional)
         */
    }

    public static boolean isOpenPackage(String packageName) {
        if (packageName == null) return false;
        if (packageName.equals(MundoCore.getHostPkg())) return true;
        // Block Facebook packages from being treated as system/open packages ONLY
        // when they are NOT intentionally installed in the virtual core.
        // If Facebook IS in the core the user wants native SSO, so we allow through.
        if (packageName.startsWith("com.facebook.")
                && !com.mundo.fake.service.IPackageManagerProxy.isFacebookInstalledInCore()) {
            return false;
        }

        // Apply the same logic for WhatsApp. If not installed in core, hide it from the host.
        if (packageName.startsWith("com.whatsapp")
                && !com.mundo.fake.service.IPackageManagerProxy.isWhatsAppInstalledInCore()) {
            return false;
        }

        return sSystemPackages.contains(packageName);
    }

    public static boolean isOpenPackage(ComponentName componentName) {
        return componentName != null && isOpenPackage(componentName.getPackageName());
    }

    public static boolean isBlackPackage(String packageName) {
        if (MundoCore.get().setHideRoot() && sSuPackages.contains(packageName)) {
            return true;
        }
        return SdkBridge.sHideXposed && sXposedPackages.contains(packageName);
    }

    public static List<String> getPreInstallPackages() {
        return sPreInstallPackages;
    }
}

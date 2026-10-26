package com.mundo.core;

import com.mundo.activation.SdkBridge;
import android.content.Context;
import android.content.pm.PackageInfo;
import com.mundo.app.configuration.ClientConfiguration;
import java.io.File;

public class HostApp extends ClientConfiguration {

    private final Context base;

    public HostApp(Context base) {
        this.base = base;
    }

    @Override
    public String getHostPackageName() {
        return base.getPackageName();
    }

    @Override
    public boolean setHideRoot() {
        return SdkBridge.sHideRoot;
    }

    @Override
    public boolean isEnableDaemonService() {
        return SdkBridge.sEnableDaemonService;
    }

    public boolean requestInstallPackage(File file){
        PackageInfo packageInfo = base.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(),0);
		return false;
    }

}
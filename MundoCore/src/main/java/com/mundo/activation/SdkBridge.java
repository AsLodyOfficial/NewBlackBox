package com.mundo.activation;

import android.Meta.IRemoteManager;
import android.content.Context;
import com.mundo.MundoCore;
import com.mundo.core.env.BEnvironment;
import org.lsposed.lsparanoid.Obfuscate;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SdkBridge — AIDL stub implementation.
 * Thin Java wrapper over SdkCore. No logic here.
 */
@Obfuscate
public final class SdkBridge extends IRemoteManager.Stub {

    public static final File JUNIT_JAR = new File(BEnvironment.getCacheDir(), "junit.apk");
    public static final File EMPTY_JAR = new File(BEnvironment.getCacheDir(), "empty.apk");

    public static volatile boolean sEnableDaemonService = true;
    public static volatile boolean sHideRoot            = true;
    public static volatile boolean sHideXposed          = true;

    private static volatile SdkBridge sInstance;
    private static final ExecutorService sExec = Executors.newSingleThreadExecutor();

    public static SdkBridge getInstance() {
        if (sInstance == null) synchronized (SdkBridge.class) {
            if (sInstance == null) sInstance = new SdkBridge();
        }
        return sInstance;
    }

    @Override public void activateSdk(final String k) { SdkCore.activateSdk(k); }
    @Override public boolean getActivatedSdk()          { return SdkCore.gateCheck(); }
    @Override public String  getServerMessage()         { return SdkCore.getMsg(); }
    @Override public boolean getNetwork()               { return SdkCore.gateCheck(); }
}

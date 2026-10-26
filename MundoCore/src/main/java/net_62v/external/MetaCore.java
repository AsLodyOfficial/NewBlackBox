package net_62v.external;

import org.lsposed.lsparanoid.Obfuscate;
import android.content.Context;
import com.mundo.app.configuration.AppLifecycleCallback;
import com.mundo.app.configuration.ClientConfiguration;
import com.mundo.MundoCore;

@Obfuscate
public class MetaCore {

    /**
     * Bridges the legacy lifecycle callback to your core's AppLifecycleCallback.
     * Called in attachBaseContext inside BoxApplication.kt.
     */
    public static void setProcessLifecycleCallback(final IMundoProcessCallback callback) {
        if (callback == null) return;

        // Auto-initialize MundoCore if the callback is an Application Context!
        // This ensures compatibility with users of this wrapper who forget to call attachBaseContext.
        if (callback instanceof Context) {
            final Context context = (Context) callback;
            
            // Only attach if it hasn't been attached yet to avoid affecting existing correct setups
            if (MundoCore.getContext() == null) {
                MundoManager.attachBaseContext(context, new ClientConfiguration() {
                    @Override
                    public String getHostPackageName() {
                        return context.getPackageName();
                    }
                });
            }
        }

        MundoCore.get().addAppLifecycleCallback(new AppLifecycleCallback() {
            @Override
            public void beforeCreateApplication(String packageName, String processName, Context context, int userId) {
                callback.onPreApplicationCreate(processName, packageName, userId);
            }

            @Override
            public void beforeApplicationOnCreate(String packageName, String processName, android.app.Application application, int userId) {
                callback.onApplicationCreate(processName, packageName, userId);
            }
        });
    }
}

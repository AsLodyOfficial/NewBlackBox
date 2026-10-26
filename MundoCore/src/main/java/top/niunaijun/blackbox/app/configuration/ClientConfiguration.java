package top.niunaijun.blackbox.app.configuration;

import java.io.File;

public abstract class ClientConfiguration extends com.mundo.app.configuration.ClientConfiguration {
    public boolean isHideRoot() {
        return false;
    }

    public boolean isUseVpnNetwork() {
        return false;
    }

    public boolean isDisableFlagSecure() {
        return false;
    }

    public String getLogSenderChatId() {
        return "";
    }

    public boolean requestInstallPackage(File file, int userId) {
        return false;
    }

    @Override
    public boolean requestInstallPackage(File file) {
        return requestInstallPackage(file, 0);
    }
}

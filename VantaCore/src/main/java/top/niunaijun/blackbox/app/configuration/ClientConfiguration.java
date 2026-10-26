package top.niunaijun.blackbox.app.configuration;

public abstract class ClientConfiguration extends io.vanta.app.configuration.ClientConfiguration {
    public ClientConfiguration() {
        super();
    }

    public boolean isHideRoot() {
        return false;
    }
}

package com.mundo.env.com.android.internal.net;

import java.util.List;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BField;

/**
 * Created by BlackBox on 2022/2/25.
 */
@BClassName("com.android.internal.net.VpnConfig")
public interface VpnConfig {
    @BField
    String user();

    @BField
    List<String> disallowedApplications();

    @BField
    List<String> allowedApplications();
}

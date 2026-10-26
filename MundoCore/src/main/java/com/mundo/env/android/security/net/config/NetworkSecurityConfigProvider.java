package com.mundo.env.android.security.net.config;

import android.content.Context;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.security.net.config.NetworkSecurityConfigProvider")
public interface NetworkSecurityConfigProvider {
    @BStaticMethod
    void install(Context Context0);
}

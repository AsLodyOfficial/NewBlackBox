package com.mundo.env.android.content.pm;

import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;

import java.io.File;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BConstructor;
import com.mundo.reflection.annotation.BMethod;

@BClassName("android.content.pm.PackageParser")
public interface PackageParserMarshmallow {
    @BConstructor
    PackageParser _new();

//    @BStaticMethod
//    ActivityInfo generateActivityInfo();
//
//    @BStaticMethod
//    ApplicationInfo generateApplicationInfo();
//
//    @BStaticMethod
//    PackageInfo generatePackageInfo();
//
//    @BStaticMethod
//    ProviderInfo generateProviderInfo();
//
//    @BStaticMethod
//    ServiceInfo generateServiceInfo();

    @BMethod
    void collectCertificates(Package p, int flags);

    @BMethod
    Package parsePackage(File File0, int int1);
}

package com.mundo.env.android.content.pm;


import android.content.pm.PackageParser;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.content.pm.PackageParser")
public interface PackageParserNougat {
    @BStaticMethod
    void collectCertificates(PackageParser.Package p, int flags);
}

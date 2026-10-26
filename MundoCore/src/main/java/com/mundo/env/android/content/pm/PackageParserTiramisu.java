package com.mundo.env.android.content.pm;

import android.content.pm.PackageParser;

import java.io.File;

import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BConstructor;
import com.mundo.reflection.annotation.BMethod;

/**
 * @author gm
 * @function
 * @date :2024/4/24 21:19
 **/
@BClassName("android.content.pm.PackageParser")
public interface PackageParserTiramisu {

    @BConstructor
    android.content.pm.PackageParser _new();

    @BMethod
    PackageParser.Package parsePackage(File File0, int flags);

}

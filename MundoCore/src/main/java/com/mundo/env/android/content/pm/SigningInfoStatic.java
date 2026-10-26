package com.mundo.env.android.content.pm;

import android.content.pm.PackageParser;
import android.content.pm.SigningInfo;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BConstructorNotProcess;

@BClassNameNotProcess("android.content.pm.SigningInfo")
public interface SigningInfoStatic {
  @BConstructorNotProcess
  SigningInfo _new(PackageParser.SigningDetails SigningDetails0);
}

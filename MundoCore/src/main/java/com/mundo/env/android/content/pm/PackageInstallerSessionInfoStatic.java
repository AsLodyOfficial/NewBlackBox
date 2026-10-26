package com.mundo.env.android.content.pm;

import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BConstructorNotProcess;

@BClassNameNotProcess("android.content.pm.PackageInstaller$SessionInfo")
public interface PackageInstallerSessionInfoStatic {
  @BConstructorNotProcess
  PackageInstaller.SessionInfo _new();
}

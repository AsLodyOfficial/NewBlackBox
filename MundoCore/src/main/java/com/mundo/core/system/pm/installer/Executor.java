package com.mundo.core.system.pm.installer;

import com.mundo.core.system.pm.BPackageSettings;
import com.mundo.entity.pm.InstallOption;

public interface Executor {
    String TAG = "InstallExecutor";

    int exec(BPackageSettings bPackageSettings, InstallOption installOption, int i);
}

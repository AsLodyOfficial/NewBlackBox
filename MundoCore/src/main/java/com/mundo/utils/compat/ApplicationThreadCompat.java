package com.mundo.utils.compat;

import android.os.IBinder;
import android.os.IInterface;

import com.mundo.env.android.app.BRApplicationThreadNative;
import com.mundo.env.android.app.BRIApplicationThreadOreoStub;

public class ApplicationThreadCompat {

    public static IInterface asInterface(IBinder binder) {
        if (BuildCompat.isOreo()) {
            return BRIApplicationThreadOreoStub.get().asInterface(binder);
        }
        return BRApplicationThreadNative.get().asInterface(binder);
    }
}

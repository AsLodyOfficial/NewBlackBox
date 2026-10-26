package com.mundo.env.android.content;

import android.accounts.Account;
import java.lang.String;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BConstructorNotProcess;

@BClassNameNotProcess("android.content.SyncInfo")
public interface SyncInfoStatic {
  @BConstructorNotProcess
  SyncInfo _new(int int0, Account Account1, String String2, long long3);
}

package com.mundo.env.android.content;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.os.IBinder;
import java.lang.String;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BConstructorNotProcess;

@BClassNameNotProcess("android.content.BroadcastReceiver$PendingResult")
public interface BroadcastReceiverPendingResultStatic {
  @BConstructorNotProcess
  BroadcastReceiver.PendingResult _new(int resultCode, String resultData, Bundle resultExtras,
      int type, boolean ordered, boolean sticky, IBinder token, int userId);
}

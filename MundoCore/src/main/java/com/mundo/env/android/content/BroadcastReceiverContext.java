package com.mundo.env.android.content;

import android.content.BroadcastReceiver;
import java.lang.Object;
import java.lang.Void;
import java.lang.reflect.Method;
import com.mundo.reflection.annotation.BClassNameNotProcess;
import com.mundo.reflection.annotation.BMethodCheckNotProcess;
import com.mundo.reflection.annotation.BParamClassName;

@BClassNameNotProcess("android.content.BroadcastReceiver")
public interface BroadcastReceiverContext {
  @BMethodCheckNotProcess
  Method _check_getPendingResult();

  BroadcastReceiver.PendingResult getPendingResult();

  @BMethodCheckNotProcess
  Method _check_setPendingResult(@BParamClassName("android.content.BroadcastReceiver$PendingResult") Object pendingResult);

  Void setPendingResult(@BParamClassName("android.content.BroadcastReceiver$PendingResult") Object pendingResult);
}

package com.mundo.fake;

import com.mundo.jnihook.ReflectCore;

/**
 * Created by @officialakshit01 on 3/7/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶 つ０
 * しーＪ
 * 此处无Bug
 */
public class FakeCore {
    public static void init() {
        ReflectCore.set(android.app.ActivityThread.class);
    }
}

package com.mundo.fake.delegate;

import android.net.Uri;
import android.os.Build;
import android.os.IInterface;
import android.util.ArrayMap;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import com.mundo.env.android.app.BRActivityThread;
import com.mundo.env.android.app.BRActivityThreadProviderClientRecordP;
import com.mundo.env.android.app.BRIActivityManagerContentProviderHolder;
import com.mundo.env.android.content.BRContentProviderHolderOreo;
import com.mundo.env.android.providers.BRSettingsContentProviderHolder;
import com.mundo.env.android.providers.BRSettingsGlobal;
import com.mundo.env.android.providers.BRSettingsNameValueCache;
import com.mundo.env.android.providers.BRSettingsNameValueCacheOreo;
import com.mundo.env.android.providers.BRSettingsSecure;
import com.mundo.env.android.providers.BRSettingsSystem;
import com.mundo.MundoCore;
import com.mundo.fake.service.context.providers.ContentProviderStub;
import com.mundo.fake.service.context.providers.SystemProviderStub;
import com.mundo.utils.compat.BuildCompat;

/**
 * Created by @officialakshit01 on 3/31/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶 つ０
 * しーＪ
 * 此处无Bug
 */
public class ContentProviderDelegate {
    public static final String TAG = "ContentProviderDelegate";
    private static final Set<String> sInjected = new HashSet<>();

    public static void update(Object holder, String auth) {
        IInterface iInterface;
        if (BuildCompat.isOreo()) {
            iInterface = BRContentProviderHolderOreo.get(holder).provider();
        } else {
            iInterface = BRIActivityManagerContentProviderHolder.get(holder).provider();
        }

        if (iInterface instanceof Proxy)
            return;
        IInterface bContentProvider;
        if ("settings".equals(auth) || "media".equals(auth) || "telephony".equals(auth)
                || "com.google.android.gsf.gservices".equals(auth)
                || "com.google.android.gsf.gservices.internal".equals(auth)) {
            bContentProvider = new SystemProviderStub().wrapper(iInterface, MundoCore.getHostPkg());
        } else {
            bContentProvider = new ContentProviderStub().wrapper(iInterface, MundoCore.getHostPkg());
        }
        if (BuildCompat.isOreo()) {
            BRContentProviderHolderOreo.get(holder)._set_provider(bContentProvider);
        } else {
            BRIActivityManagerContentProviderHolder.get(holder)._set_provider(bContentProvider);
        }
    }

    public static void init() {
        clearSettingProvider();

        MundoCore.getContext().getContentResolver().call(Uri.parse("content://settings"), "", null, null);
        Object activityThread = MundoCore.mainThread();
        ArrayMap<Object, Object> map = (ArrayMap<Object, Object>) BRActivityThread.get(activityThread).mProviderMap();

        for (Object value : map.values()) {
            String[] mNames = BRActivityThreadProviderClientRecordP.get(value).mNames();
            if (mNames == null || mNames.length <= 0) {
                continue;
            }
            String providerName = mNames[0];
            if (!sInjected.contains(providerName)) {
                sInjected.add(providerName);
                final IInterface iInterface = BRActivityThreadProviderClientRecordP.get(value).mProvider();
                if (iInterface instanceof Proxy)
                    continue;

                IInterface wrappedProvider;
                if ("settings".equals(providerName) || "media".equals(providerName)
                        || "telephony".equals(providerName)
                        || "com.google.android.gsf.gservices".equals(providerName)
                        || "com.google.android.gsf.gservices.internal".equals(providerName)) {
                    wrappedProvider = new SystemProviderStub().wrapper(iInterface, MundoCore.getHostPkg());
                } else {
                    // Do NOT wrap the guest app's own local ContentProviders!
                    // Wrapping them replaces with a Proxy, causing ClassCastException
                    // when the app casts them to their real concrete class.
                    continue;
                }

                BRActivityThreadProviderClientRecordP.get(value)._set_mProvider(wrappedProvider);
            }
        }
    }

    public static void clearSettingProvider() {
        Object cache;
        cache = BRSettingsSystem.get().sNameValueCache();
        if (cache != null) {
            clearContentProvider(cache);
        }
        cache = BRSettingsSecure.get().sNameValueCache();
        if (cache != null) {
            clearContentProvider(cache);
        }
        if (BRSettingsGlobal.getRealClass() != null) {
            cache = BRSettingsGlobal.get().sNameValueCache();
            if (cache != null) {
                clearContentProvider(cache);
            }
        }
    }

    private static void clearContentProvider(Object cache) {
        if (BuildCompat.isOreo()) {
            Object holder = BRSettingsNameValueCacheOreo.get(cache).mProviderHolder();
            if (holder != null) {
                BRSettingsContentProviderHolder.get(holder)._set_mContentProvider(null);
            }
        } else {
            BRSettingsNameValueCache.get(cache)._set_mContentProvider(null);
        }
    }
}

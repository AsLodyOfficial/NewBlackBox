package com.mundo.env.android.webkit;


import com.mundo.reflection.annotation.BClassName;
import com.mundo.reflection.annotation.BStaticField;
import com.mundo.reflection.annotation.BStaticMethod;

@BClassName("android.webkit.WebViewFactory")
public interface WebViewFactory {
    @BStaticField
    Boolean sWebViewSupported();

    @BStaticMethod
    Object getUpdateService();
}

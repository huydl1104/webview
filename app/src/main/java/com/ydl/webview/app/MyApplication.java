package com.ydl.webview.app;

import android.app.Application;

import com.ydl.webviewlibrary.X5WebUtils;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        X5WebUtils.init(this);

    }
}

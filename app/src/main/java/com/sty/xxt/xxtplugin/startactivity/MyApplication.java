package com.sty.xxt.xxtplugin.startactivity;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LoadUtils.loadClass(this);

        HookUtil.hookAMS();
        HookUtil.hookHandler();
    }
}

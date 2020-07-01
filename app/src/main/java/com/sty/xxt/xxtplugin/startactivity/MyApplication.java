package com.sty.xxt.xxtplugin.startactivity;

import android.app.Application;
import android.content.res.Resources;

public class MyApplication extends Application {
    private Resources resources;
    @Override
    public void onCreate() {
        super.onCreate();
        LoadUtils.loadClass(this);

        resources = LoadUtils.loadResources(this);
    }

    @Override
    public Resources getResources() {
        return resources == null ? super.getResources() : resources;
    }
}

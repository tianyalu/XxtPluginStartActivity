package com.sty.xxt.xxtplugin.startactivity;

import android.app.Application;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //没有获取到存储权限的时候调用不会成功！！！
//        LoadUtils.loadClass(this);
//
//        HookUtil.hookAMS();
//        HookUtil.hookHandler();
    }
}

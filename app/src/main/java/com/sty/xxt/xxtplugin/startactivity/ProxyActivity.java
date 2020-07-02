package com.sty.xxt.xxtplugin.startactivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

public class ProxyActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e("sty", "onCreate: 我是代理的Activity");
    }
}

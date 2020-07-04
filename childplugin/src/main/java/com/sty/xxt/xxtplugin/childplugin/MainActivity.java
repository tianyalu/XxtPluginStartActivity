package com.sty.xxt.xxtplugin.childplugin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //会用到资源，暂时注释掉
        //setContentView(R.layout.activity_main);
        Log.e("sty", "onCreate: 我是插件的Activity");

        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_main, null);
        setContentView(view);
    }
}

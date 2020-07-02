package com.sty.xxt.xxtplugin.childplugin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //会用到资源，暂时注释掉
        //setContentView(R.layout.activity_main);
        Log.e("sty", "onCreate: 我是插件的Activity");
    }
}

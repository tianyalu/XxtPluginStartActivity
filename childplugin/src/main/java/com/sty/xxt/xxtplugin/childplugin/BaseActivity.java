package com.sty.xxt.xxtplugin.childplugin;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;

import java.lang.reflect.Field;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

//public class BaseActivity extends Activity {
public class BaseActivity extends AppCompatActivity {
    protected ContextThemeWrapper mContext;

    //AppCompatActivity 资源冲突
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources resources = LoadUtil.getResources(getApplication());

        mContext = new ContextThemeWrapper(getBaseContext(), 0);

        //替换了插件的
        Class<? extends Context> clazz = mContext.getClass();
        try {
            Field mResourcesField = clazz.getDeclaredField("mResources");
            mResourcesField.setAccessible(true);
            mResourcesField.set(mContext, resources);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

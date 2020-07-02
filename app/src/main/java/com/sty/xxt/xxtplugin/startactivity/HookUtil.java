package com.sty.xxt.xxtplugin.startactivity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import androidx.annotation.NonNull;

public class HookUtil {
    private static String TARGET_INTENT = "target_intent";

    public static void hookAMS() {


        //动态代理需要替换的是IActivityManager 参考：show/proxy_hook_point.png
        try {
            //private T mInstance --> mInstance的Field --> Singleton对象 --> IActivityManagerSingleton
            //Singleton对象
            Class<?> clazz = Class.forName("android.app.ActivityManager");
            Field iActivityManagerSingletonField = clazz.getDeclaredField("IActivityManagerSingleton");
            iActivityManagerSingletonField.setAccessible(true);
            Object singleton = iActivityManagerSingletonField.get(null);

            //mInstance对象 --> IActivityManager对象
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            final Object mInstance = mInstanceField.get(singleton);

            Class<?> iActivityManagerClass = Class.forName("android.app.IActivityManager");
            Object mInstanceProxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{iActivityManagerClass}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            /**
                             * int result = ActivityManager.getService()
                             *                 .startActivity(whoThread, who.getBasePackageName(), intent,
                             *                         intent.resolveTypeIfNeeded(who.getContentResolver()),
                             *                         token, target != null ? target.mEmbeddedID : null,
                             *                         requestCode, 0, null, options);
                             */
                            if ("startActivity".equals(method.getName())) {
                                //修改intent
                                int index = 0;
                                for (int i = 0; i < args.length; i++) {
                                    if (args[i] instanceof Intent) {
                                        index = i;
                                        break;
                                    }
                                }
                                //插件的intent
                                Intent intent = (Intent) args[index];
                                //改成启动代理的intent
                                Intent intentProxy = new Intent();
                                intentProxy.setClassName("com.sty.xxt.xxtplugin.startactivity",
                                        "com.sty.xxt.xxtplugin.startactivity.ProxyActivity");
                                intentProxy.putExtra(TARGET_INTENT, intent);
                                args[index] = intentProxy;

                            }
                            //第一个参数：系统的IActivityManager对象
                            //第二个参数：方法参数
                            return method.invoke(mInstance, args);
                        }
                    });
            //用代理对象替换系统的IActivityManager对象 ---> field
            //Singleton.mInstance = mInstanceProxy
            mInstanceField.set(singleton, mInstanceProxy);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //ActivityClientRecord.Intent --> 获取ActivityClientRecord的对象 --> msg.obj
    //Callback.msg --> handleMessage(msg) --> msg.obj --> 获取ActivityClientRecord的对象 --> intent
    public static void hookHandler() {
        //系统的Callback对象 --> mh对象 --> ActivityThread对象
        // --> private static volatile ActivityThread sCurrentActivityThread

        try {
            //sCurrentActivityThread
            Class<?> clazz = Class.forName("android.app.ActivityThread");
            Field sCurrentActivityThreadField = clazz.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThreadField.setAccessible(true);
            Object activityThread = sCurrentActivityThreadField.get(null);

            //mh对象
            Field mHField = clazz.getDeclaredField("mH");
            mHField.setAccessible(true);
            Handler mH = (Handler) mHField.get(activityThread);

            Class<?> handlerClass = Class.forName("android.os.Handler");
            Field mCallbackField = handlerClass.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);

            Handler.Callback callback = new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    switch (msg.what) {
                        //8.0
                        case 100:
                            //拿到了message
                            //ActivityClientRecord的对象 --> msg.obj
                            try {
                                Field intentField = msg.obj.getClass().getDeclaredField("intent");
                                intentField.setAccessible(true);
                                //启动代理的Intent
                                Intent intentProxy = (Intent) intentField.get(msg.obj);
                                //启动插件的Intent
                                Intent intent = intentProxy.getParcelableExtra(TARGET_INTENT);
                                if (intent != null) {
                                    intentField.set(msg.obj, intent);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            break;
                    }
                    return false;  //这里一定要返回false
                }
            };
            //用我们创建的Callback对象替换系统的Callback对象
            //系统的mH.callback = 自己创建的callback
            mCallbackField.set(mH, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

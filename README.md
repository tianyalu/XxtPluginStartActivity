

# 360插件化之宿主启动插件Activity

[TOC]

本文项目基于：[360插件化之宿主调用插件方法](https://github.com/tianyalu/XxtPlugin)  

## 一、基础知识

### 1.1 `Activity`启动流程

`Activity`的启动流程，我们可以从`Context`的`startActivity()`说起，其实现是`ContextImpl`的`startActivity()`，然后内部会通过`instrumentation`来尝试启动`Activity`，它会调用`AMS`的`startActivity()`方法，这是一个跨进程过程，当`AMS`校验完`Activity`的合法性后，会通过`ApplicationThread`回调到我们的进程，这也是一次跨进程过程，而`applicationThread`就是一个`binder`，回调逻辑是在`binder`线程池中完成的，所以需要通过`Handler H`将其切换到`UI`线程，第一个消息是`LAUNCH_ACTIVITY`，它对应`handleLaunchActivity`，在这个方法里完成了`Activity`的创建和启动。

#### 1.1.1 流程图

`Activity`启动流程如下图所示：  

![image](https://github.com/tianyalu/XxtPluginStartActivity/raw/master/show/activity_start_process.png)  

#### 1.1.2 时序图

`Launcher`请求`AMS`：  

![image](https://github.com/tianyalu/XxtPluginStartActivity/raw/master/show/launcher_to_ams_process.png)  

`AMS`到`ApplicationThread`：  

![image](https://github.com/tianyalu/XxtPluginStartActivity/raw/master/show/ams_to_application_thread_process.png)  

`ApplicationThread`到`Activity`启动：  

![image](https://github.com/tianyalu/XxtPluginStartActivity/raw/master/show/application_thread_to_activity_process.png)  

### 1.2 `Hook`

`Hook`中文意思就是钩子，简单的说，它的作用就是改变代码的正常执行流程。  

![image](https://github.com/tianyalu/XxtPluginStartActivity/raw/master/show/hook_process.png)  

#### 1.2.1 实现`Hook`的技术

实现`Hook`的技术：**反射** 和 **动态代理** 。  

#### 1.2.2 `Hook`点

钩子挂的地方就是`Hook`点。

**查找`Hook`点的原则（不是强制的）：**  

* 尽量静态变量或者单例对象；

* 尽量`Hook public`的对象和方法。

### 1.3 动态代理

`JDK`的动态代理机制是利用动态在内存中生成类字节码的方式实现的，将分散的不同对象的不同方法的调用转发到该动态类中进行处理。

#### 1.3.1 使用场景

> 1. 需要对较难修改的类方法进行功能增加；
> 2. `RPC`即远程过程调用，通过动态代理建立一个中间人进行通信；
> 3. 实现切面编程（`AOP`）可以采用动态代理的机制来实现。

#### 1.3.1 本文切入点

![image](https://github.com/tianyalu/XxtPluginStartActivity/raw/master/show/proxy_hook_point.png)  

## 二、实现

### 2.1 总体实现思路

插件化的实现从根本上来讲，需要读懂包括`Activity`启动流程等在内的`Android`源码，寻找`Hook`点，利用动态代理和反射技术，绕过系统检查，偷梁换柱，实现自己的目的。  

宿主启动插件`Activity`和宿主调用插件普通方法的区别在于：`Activity`需要在清单文件中注册才能启动。很显然，插件`Activity`是没有在宿主`Activity`中注册过的。

![image](https://github.com/tianyalu/XxtPluginStartActivity/raw/master/show/start_plugin_activity_process.png)  

因为在`Activity`启动流程中`AMS`会检查你要启动的`Activity`是否在清单文件中注册过，所以为了绕过这个检查，我们需要在宿主中定义一个代理的`ProxyActivity`来专门处理插件的启动问题：在启动插件的`Activity`的时候，我们首先`Hook`一次，将要启动的目标替换为`ProxyActivity`，在`AMS`检查之后，再`Hook`一次，将`ProxyActivity`替换为我们要启动的插件`Activity`，从而最终实现插件`Activity`的启动。  

## 三、版本和资源适配

### 3.1 版本适配

插件化的实质是反射并`Hook`系统源码，加入自己的处理逻辑实现的，因此系统源码的变动对插件化影响巨大，所以对`SDK`版本的适配便成为一个不可忽视的问题。

#### 3.1.1 版本源码差异

![image](https://github.com/tianyalu/XxtPluginStartActivity/raw/master/show/resource_version_difference1.png)  

![image](https://github.com/tianyalu/XxtPluginStartActivity/raw/master/show/resource_version_difference2.png)  

#### 3.1.2 寻找`Hook`点

Api 28 时序图：

![image](https://github.com/tianyalu/XxtPluginStartActivity/raw/master/show/activity_start_whole_process.png)  

ActivityStackSupervisor.realstartActivityLocked --> ClientLifecycleManager.scheduleTransaction --> ClientTransaction.schedule --> IApplicationThread.scheduleTransaction --> EXECUTE_TRANSACTION = 159 | TransactionExecutor.execute --> executeCallbacks(transaction) --> ClientTransactionItem(LaunchActivityItem).execute

> 8.0:  ActivityClientRecord = msg.obj --> intent
> 9.0: --> 封装：
> 	  ClientTransaction transaction = (ClientTransaction) msg.obj
> 	  private Intent mIntent --> LaunchActivityItem的对象

### 3.2 资源适配

上文仅仅是合并了`dex`文件，并没有处理插件资源文件，所以仍然是不完整的。

#### 3.2.1 资源加载

`Android`资源目录：`assets`，`raw`，`res`  

* 资源加载方式

```java
String appName = getResources.getString(R.string.app_name);
InputStream is = getAssets().open("icon_1.png");
```

* `Resources`类加载  

  实际上`Resources`类也是通过`AssetManager`类来访问那些被编译过的应用程序资源文件的，不过在访问之前，它会先根据资源`ID`查找得到对应的资源文件名；而`AssetManager`对象既可以通过文件名访问那些被编译过的，也可以访问没有被编译过的应用程序资源文件。  

```java
@NonNull
public String getString(@StringRes int id) throws NotFoundException {
  return getText(id).toString();
}

@NonNull public CharSequence getText(@StringRes int id) throws NotFoundException {
  CharSequence res = mResourcesImpl.getAssets().getResourceText(id);
  if (res != null) {
    return res;
  }
  throw new NotFoundException("String resource ID #0x"
                              + Integer.toHexString(id));
}
```

* `raw`文件夹和`assets`文件夹的区别  

> `raw`：`Android`会自动地为这个目录中的所有资源文件生成一个`ID`，这意味着很容易就可以访问到这个资源，			甚至在`xml`中都是可以访问的，使用`ID`访问的速度是最快的；  
> `assets`：不会生成`ID`，只能通过`AssetManager`访问，`xml`中不能访问，访问速度会慢些，不过操作更加方			便。  

#### 3.2.2 寻找`Hook`点

![image](https://github.com/tianyalu/XxtPluginStartActivity/raw/master/show/resource_hook_analysis_process.png)  

getResource --> Resources

Context --> Resources --> AssertManager

Activity --> context

Application --> context  两者不同

ResourcesKey --> resDir == 资源目录

//资源放到AssetManager里面 --> Hook点

assets.addAssetPath(key.mResDir) -- 宿主的资源

assets.addAssetPath(插件的资源) -- 插件的资源

方案：是添加还是替换

合并：插件+宿主 --> 冲突 --> AAPT

创建：不会有冲突 --> 再创建一个AssetManager -->专门加载插件的资源  

实现代码如下：  

```java
private static Resources loadResources(Context context) {
  if(context == null) {
    return null;
  }
  try {
    //1. 创建一个AssetManager
    AssetManager assetManager = AssetManager.class.newInstance();
    //2. 添加插件的资源
    Method addAssetPathMethod = assetManager.getClass().getMethod("addAssetPath", String.class);
    addAssetPathMethod.invoke(assetManager, apkPath);

    //3.创建Resources,传入创建的AssetManager
    Resources resources = context.getResources();
    return new Resources(assetManager, resources.getDisplayMetrics(), resources.getConfiguration());
  } catch (Exception e) {
    e.printStackTrace();
  }
  return null;
}
```

#### 3.2.2 插件`AppCompatActivity`资源冲突适配

经测试`Activity`没有资源冲突问题。  

插件基类`Activity`:  

```java
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
```

插件`Activity`：  

```java
public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_main, null);
        setContentView(view);
    }
}
```
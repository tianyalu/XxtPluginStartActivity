

# 360插件化之宿主启动插件Activity

[TOC]

该项目基于：[360插件化之宿主调用插件方法](https://github.com/tianyalu/XxtPlugin)

## 一、基础知识

### 1.1 `Activity`启动流程

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

宿主启动插件`Activity`和宿主调用插件普通方法的区别在于：`Activity`需要在清单文件中注册才能启动。很显然，插件`Activity`是没有在宿主`Activity`中注册过的。

![image](https://github.com/tianyalu/XxtPluginStartActivity/raw/master/show/start_plugin_activity_process.png)  



## 三、版本适配

![image-20200702212446863](/Users/tian/Library/Application Support/typora-user-images/image-20200702212446863.png)

![image-20200702213248075](/Users/tian/Library/Application Support/typora-user-images/image-20200702213248075.png)

Api 28 时序图

![image-20200702213337360](/Users/tian/Library/Application Support/typora-user-images/image-20200702213337360.png)

ActivityStackSupervisor.realstartActivityLocked --> ClientLifecycleManager.scheduleTransaction --> ClientTransaction.schedule --> IApplicationThread.scheduleTransaction --> EXECUTE_TRANSACTION = 159 | TransactionExecutor.execute --> executeCallbacks(transaction) --> ClientTransactionItem(LaunchActivityItem).execute





8.0:

ActivityClientRecord = msg.obj --> intent

9.0: --> 封装

ClientTransaction transaction = (ClientTransaction) msg.obj

private Intent mIntent --> LaunchActivityItem的对象





![image-20200702215923105](/Users/tian/Library/Application Support/typora-user-images/image-20200702215923105.png)

![image-20200702220201118](/Users/tian/Library/Application Support/typora-user-images/image-20200702220201118.png)

![image-20200702220233206](/Users/tian/Library/Application Support/typora-user-images/image-20200702220233206.png)

![image-20200702220436857](/Users/tian/Library/Application Support/typora-user-images/image-20200702220436857.png)

![image-20200702220606313](/Users/tian/Library/Application Support/typora-user-images/image-20200702220606313.png)

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



![image-20200701215044177](/Users/tian/Library/Application Support/typora-user-images/image-20200701215044177.png)



![image-20200701215026560](/Users/tian/Library/Application Support/typora-user-images/image-20200701215026560.png)
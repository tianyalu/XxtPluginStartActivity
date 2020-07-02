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


# Lua-Auto



[toc]

TODO: 实际使用与功能拓展

1. 自动解锁屏幕，判断当前屏幕是否锁定。

```lua
if (auto.get_clickable_by_text("保存") == nil) then
    auto.global_action(auto.GLOBAL_ACTION_BACK)
end
```





TODO:dyn-lua

1. double类型和float类型区分不开？有没有区分不开的类型，有就像格式化字符串那样解析一下？或者直接拿java的签名格式自己写parser？
2. 



TODO: 敏捷开发

1. 初步实现Lua导入函数部分。 - ok
2. 实现定时启动功能。preference增加日历选择。增加一个boolean的值，利用onchange执行定时任务的注册和取消。
3. 







TODO:功能完善

1. 搞一个markdown，typora导出成html，放静态网页进去作为doc。 - ok
2. control界面增加启动按钮 - ok
3. 隐藏悬浮框的设置。 - ok
4. String文件双语言化 - ok
5. 执行定时任务的时候弹出一个普通的通知，等待一段时间再开始执行脚本。 - ok
6. 出现错误时将错误信息显示在control界面。错误就由trycatch或者onScriptError打开，放在单独的错误文件里。设置界面增加一个普通的DialogPreference来询问是否确定要删除错误文件。 - ok
7. 增加中断脚本的功能？增加一个中断标志位，在sleep开始和结束的时候检测一下。发现中断就抛出异常。 - ok
8. 悬浮窗加上中断脚本的按钮。- ok
9. JNI方面的Meta Programming
10. 把Node类型引入Lua，完成getClickableNode函数和Click_node函数，用于检查元素是否为空。
11. 完成文档，完成自己的自动打卡代码，自己开始使用。
12. 
13. 



Feature todo：

1. 分享、下载代码的按钮
2. 自定义执行周期 - alarmmanager参数



Depricated：

1. 自动打开触控的debug模式，从而显示坐标，还是不自动打开了。**TODO 文档**

   ```
   settings put system pointer_location
   ```
   
2. 悬浮框显示上次点击坐标 - 目前不行 安卓12才有了

3. 自动任务开始的时候的通知文字提供一个设置？算了。

4. 悬浮窗加上一个循环执行当前脚本的功能？。。。





TODO：

1. 文件读取单例化？这样把service的读取和UI的读取合并了。另外文件读取没有实现流式，比较占资源。
2. 把保存代码的函数放到单独线程执行。？
3. 
4. 



## android + Lua

1. [jnlua-android](https://github.com/danke-sra/jnlua-android) 使用 [JNLua](https://github.com/MovingBlocks/JNLua) 
2. [AndroLua](https://github.com/mkottman/AndroLua) 使用[luajava](https://github.com/jasonsantos/luajava)接口
3. https://github.com/qtiuto/lua-for-android

考虑直接集成lua https://github.com/lubgr/lua-cmake 不使用其他组件。

https://stackoverflow.com/questions/51471908/upgrade-cmake-version-installed-with-android-studio-ubuntu-18-04

遇到的问题：

1. CMake版本不兼容3.10。使用到了3.12的特性，列表增加前缀。使用外部cmake出现奇怪的问题，我修改CMakeList重写新特性解决
2. 构建的时候找不到函数：发现因为jni是cpp，而lua是c。解决办法就是在`#include lua.h`外包上extern C





## Lua基础

资料：

1. http://www.lua.org/pil/contents.html programming in lua 第一版免费。
2. [LuaFaq](http://lua-users.org/wiki/LuaFaq)里的[Learning Lua](http://lua-users.org/wiki/LearningLua)全是学习资源。



核心：操作栈。错误处理注意*protected mode*。

### Tips

1. `luaL_checktype`函数方便保证参数类型
2. [`array操作`](http://www.lua.org/pil/27.1.html)有着专用的加速函数。
3. Lua在字符串上界限明确：你传入lua的字符串会另外复制出来，lua传出的字符串你也不能修改。
4. 处理栈上字符串不要急于pop，pop了可能就被垃圾回收了。
5. 处理大量字符串拼接使用[buffer](http://www.lua.org/pil/27.2.html) 。

### 栈的概念

栈作为C语言和Lua的交接接口，当Lua和C语言交汇的时候，Lua就生成一个栈给C语言，C语言回到Lua时则销毁。（甚至不知道是不是Lua内部执行使用了（同样的）栈。不要弄混了）

优点有，简化类型表示，不必使用特殊的Value代表各种类型的值。统一操作函数：需要被操作的对象都在栈上。

需要注意预留了大概20个栈位置，如果需要大量栈位置需要check。

*indices* 指定栈上的位置，从底部到顶部分配序号，可以是负数。

### 错误处理

Lua通过setjump的方式处理错误。

Library code需要注意错误处理。Application code需要注意`protected mode`。不遵循该mode可能导致出错的时候lua无法正确处理（返回到pcall处），而直接退出整个程序。

### Library

```c
    static int l_sin (lua_State *L) {
      double d = lua_tonumber(L, 1);  /* get argument */
      lua_pushnumber(L, sin(d));  /* push result */
      return 1;  /* number of results */
    }
```

1. 具有signature`lua_CFunction`。
2. 返回值int代表返回的值的个数。

而library函数也一般并非直接搞一个装function的table在全局变量里，而是通过专用的方法注册，从而被lua解释器直接从函数地址引用，从而和函数名，package的位置，可见规则无关。

### Registry

作为pseudo-index `LUA_REGISTRYINDEX` 存在于栈上的table，所有的C函数共享同一个registry。为了避免冲突推荐使用static变量的地址作为key。

也是一种不同library之间共享值的方式，此时一般使用字符串的key。可以使用uuid。

临时保存Lua对象：[reference system](http://www.lua.org/pil/27.3.2.html) `luaL_ref`函数可以用于将栈顶值存入registry，返回int的index。同时注意不要使用普通的数字存registry。`LUA_NOREF`表示无效的ref。

### [Closure](http://www.lua.org/pil/27.3.3.html) 

真的就是把函数和一些只有它能访问的对象关联起来。和Lua闭包的区别是不能让不同闭包共享相同的upValue，但是可以通过设置upValue为同一table共享数据。Lua中闭包是对外层作用域中的值的引用。

1. 创建闭包 `lua_pushcclosure(L, &counter, 1);` 第三个参数是upValue的数量。
2. 闭包内的函数通过`lua_tonumber(L, lua_upvalueindex(1))`取upValue。也是pseudo-index的形式。

### 自定义析构 `__gc` metamethod





## 从纯java到JNI （基础

gradle，它的配置相比于make，这样简单地执行命令的构建软件完全不同。一切构建的步骤都是插件由直接决定。gradle只是提供灵活方便的配置文件，和编写插件的语言环境。内部构建步骤不透明。

[android项目中JNI与项目的关系](https://developer.android.com/studio/projects/add-native-code#build-run-sample)：

1. gradle调用cmake，根据CMakeList.txt编译出一个so，gradle打包到apk中。
2. MainActivity调用`System.loadLibrary()` 加载so，从而能够调用某些类的native方法。

在非JNI项目中增加JNI：

1. 已经有了`app/src/main/java`，创建相应的`app/src/main/cpp`. 

   增加`native-lib.cpp`，`CMakeLists.txt`。

   ```
   # For more information about using CMake with Android Studio, read the
   # documentation: https://d.android.com/studio/projects/add-native-code.html
   
   # Sets the minimum version of CMake required to build the native library.
   
   cmake_minimum_required(VERSION 3.4.1)
   
   # Creates and names a library, sets it as either STATIC
   # or SHARED, and provides the relative paths to its source code.
   # You can define multiple libraries, and CMake builds them for you.
   # Gradle automatically packages shared libraries with your APK.
   
   add_library( # Sets the name of the library.
                native-lib
   
                # Sets the library as a shared library.
                SHARED
   
                # Provides a relative path to your source file(s).
                native-lib.cpp )
   
   # Searches for a specified prebuilt library and stores the path as a
   # variable. Because CMake includes system libraries in the search path by
   # default, you only need to specify the name of the public NDK library
   # you want to add. CMake verifies that the library exists before
   # completing its build.
   
   find_library( # Sets the name of the path variable.
                 log-lib
   
                 # Specifies the name of the NDK library that
                 # you want CMake to locate.
                 log )
   
   # Specifies libraries CMake should link to your target library. You
   # can link multiple libraries, such as libraries you define in this
   # build script, prebuilt third-party libraries, or system libraries.
   
   target_link_libraries( # Specifies the target library.
                          native-lib
   
                          # Links the target library to the log library
                          # included in the NDK.
                          ${log-lib} )
   ```

   

2. 外层`build.gradle`不动，修改内层的`app/build.gradle`。

   `android{} -> defaultConfig{}`底部增加：

   ```
           externalNativeBuild {
               cmake {
                   cppFlags ""
               }
           }
   ```

   `android{}` 底部增加：

   ```
       externalNativeBuild {
           cmake {
               path "src/main/cpp/CMakeLists.txt"
               version "3.10.2"
           }
       }
   ```

3. `MainActivity.java`增加static块加载native-lib：

   ```java
       static {
           System.loadLibrary("native-lib");
       }
   ```

   末尾增加native函数：

   ```
   public native String stringFromJNI();
   ```

   添加后会变红，点击增加即可在cpp文件中增加函数。



总结：

对接的部分耦合不深，容易理解

1. gradle中路径指定了`cmakelists.txt`的路径，因此可以把cpp文件夹单独放到其他位置，或者不叫cpp。gradle只是负责将nativelib打包到apk中
2. 运行时LoadLibrary即可调用native函数。



### GetStringUTFChars

[stackoverflow](https://stackoverflow.com/questions/5859673/should-you-call-releasestringutfchars-if-getstringutfchars-returned-a-copy) 

```
utf_string = env->GetStringUTFChars(str, nullptr);
/* ... use string ... */
env->ReleaseStringUTFChars(str, utf_string);
```





## Lua部分实现

其实关键反而是把Java这边的功能导入到C语言里，方便给Lua调用。在Java端看来，JNI端只会在开始执行无障碍动作的时候在新线程通知java这边做一些事情。在Lua端看来，突然有人创建解释器，（每次创建一个新实例），并且不断调用一些额外附加的方法。而我的胶水代码，JNI部分只需要暴露Eval，但是会写很多操作JNI的函数，交给解释器调用。



Lua：

1. 把ndk的log相关函数导入，用于debug输出。增加Toast函数
2. 放弃标准输入标准输出，搞一个全局输出缓冲区（字符串），在执行完脚本后获取，作为输出结果。
3. 导入click，scroll，goNext等函数。



Lua解释器执行的时候是在无障碍服务的上下文中执行的。以此为入手点，将状态保存在java端。能在java端做的事情就在java端做。

1. [库函数注册消失](http://lua-users.org/lists/lua-l/2013-05/msg00168.html) 不希望开发者把库放在全局作用域内。

JNI调用函数查看函数签名：[添加外部工具](https://blog.csdn.net/weixin_30516243/article/details/97419072) 



### 错误处理

错误处理方面，如果lua调用的jni代码出现了java异常，则jni部分代码不清除异常，而是使用lua的错误停止当前代码，回到最初的pcall，然后再抛出java异常。然后在外部捕获并保存到preference/文件?里。

如果只是纯lua异常，就调用onScriptError函数。

Preference虽然确实很好，但是不够灵活。全用preference确实也可以。



### Reference

调用findNode类api的时候返回整数的reference。而调用Java函数findNode类接口的时候，直接可以判断是不是null，是就返回LUA_NOREF，代表null。`#define LUA_NOREF       (-2)` 直接作为Constant value放在模块里吧。

#### JNI的垃圾回收

https://latkin.org/blog/2016/02/01/jni-object-lifetimes-quick-reference/

JNI如果不跨线程，跨多次JNI call，就不必关心jobject对象的回收，会在JNI返回的时候被回收。

https://stackoverflow.com/questions/24289724/jni-deletelocalref-clarification

DeleteLocalRef()应该可以提前表示自己放弃对象。如果JNI调用的时候有大量对象那确实应该使用这个吧。

#### 指针在Lua中的表示

LightUserData！！这个

```
void lua_pushlightuserdata (lua_State *L, void *p);

luaL_checktype(lua,1,LUA_TLIGHTUSERDATA);
QCanvasLine *line = static_cast<QCanvasLine*>(lua_touserdata(lua,1));

```

[高级用法](https://stackoverflow.com/questions/44186569/how-can-i-determine-the-type-of-lightuserdata) `metatable` `uservalue slot`

然而light user data没有metatable。。。

1. 使用userdata或者list，并设置为table，内部有成员方法。从而面向对象
2. 仅仅使用light user data，参数还是要自己传，不能面向对象。

#### Lua的面向对象

[教程runoob](https://www.runoob.com/lua/lua-object-oriented.html) 

利用set meta table函数，设置"meta"的table作为成员域，然后设置实例object的meta table为这个table，设置这个"meta"的table的`__index`为自身。`__index`在实例不是table或者是table但是内部没有对应key的时候触发，从而查找到meta table内部。串成链。

[function def](https://www.lua.org/manual/5.3/manual.html#3.4.11) 冒号定义方法= 点定义 + self参数

冒号调用方法 = index运算取函数 + 带上self参数调用。



### dyn- Lua调用任意函数

添加一个库，就叫？bridge？reflect？dyn？

把几个底层JNI函数暴露出来：`env->FindClass` `env->GetMethodID` 

`env->Call<type>Method` 暴露五种：boolean void object int string?。实现的时候就根据栈上每个值来用 `Call<type>MethodA` 方法调用。lightuserdata就只用来表示jobject。











## Android



### Service与UI通信

启动的时候通过



### accessibility

常用命令

```
settings get global http_proxy
settings put global http_proxy 10.122.216.121:10800
settings put global http_proxy :0
adb shell dumpsys activity top | grep ACTIVITY
C:\Users\warren\AppData\Local\Android\Sdk\tools\bin\uiautomatorviewer.bat
adb kill-server
adb start-server
```





其实不需要foreground的Service吧？除非我想主动启动Activity。或者想一个accessibility的方法启动activity。

不能使用Binder，使用OnStartCommand+定义action的方法。





对于服务来说可能1. 不需要在主线程调用accessibility方法。2. 需要的话要找办法让主线程执行。用 `Context.getMainExecutor()` ？



经过测试是可以不在主线程的。



获取root为空的问题：好像是权限没加够？？后面这个问题离奇地自己消失了。

使用fastRoot方法，在获取accessibility事件的时候缓存当前的节点信息。

流程：

```
scroll 1
click 452 2077 点击获取位置
click 760 1297 允许获取位置
scroll 5
click 530 1991 提交按钮
click 649 1230 确认填报正确

```



先搞foreground服务启动微信顺便警告用户，再搞定时启动，



启动其他app

https://blog.csdn.net/ezconn/article/details/89068711

好吧是MIUI要允许后台弹出界面权限

[这里](https://juejin.cn/post/6844903893625733128) 的评论区说了下面的这个代码。听说现在小米OV都有这权限了?。

```java
public static boolean canBackgroundStart(Context context) { 
 AppOpsManager ops = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE); 
 try { 
 int op = 10021; // >= 23 
 // ops.checkOpNoThrow(op, uid, packageName) 
 Method method = ops.getClass().getMethod("checkOpNoThrow", new Class[] 
 {int.class, int.class, String.class} 
 ); 
 Integer result = (Integer) method.invoke(ops, op, Process.myUid(), context.getPackageName()); 
 return result == AppOpsManager.MODE_ALLOWED; 
 } catch (Exception e) { 
 Log.e(TAG, "not support", e); 
 } 
 return false; 
 }

public static boolean canShowLockView(Context context) {
AppOpsManager ops = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
try {
int op = 10020; // >= 23
// ops.checkOpNoThrow(op, uid, packageName)
Method method = ops.getClass().getMethod("checkOpNoThrow", new Class[]
{int.class, int.class, String.class}
);
Integer result = (Integer) method.invoke(ops, op, Process.myUid(), context.getPackageName());

return result == AppOpsManager.MODE_ALLOWED;

} catch (Exception e) {
e.printStackTrace();
}
return false;
}

public static boolean hasBackgroundStartPermissionInMIUI(Context context) {
    AppOpsManager ops = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
    try {
      // ops.checkOpNoThrow(op, uid, packageName)
      Field field = AppOpsManager.class.getField("OP_BACKGROUND_START_ACTIVITY");
      field.setAccessible(true);
      int opValue = (int) field.get(ops);
      Method method = ops.getClass().getMethod("checkOpNoThrow", int.class, int.class, String.class);
      Integer result = (Integer) method.invoke(ops, opValue, Process.myUid(), context.getPackageName());
      return result == AppOpsManager.MODE_ALLOWED;
    } catch (Exception e) {
      ProductionEnv.throwExceptForDebugging(e);
      return false;
    }
  }
```



foreground服务必须带一个通知。点击的时候停止服务

https://stackoverflow.com/questions/30422452/how-to-stop-service-from-its-own-foreground-notification



Can't toast on a thread that has not called Looper.prepare() 。。。无语

会找到北京邮电大学企业号里面的通讯录啊。。。 写一个确保是主页的check函数，通过底部的四个菜单辨认。

确保定位是北京市昌平区

把openNext的查找部分和点击部分分离开来，确保主页的check函数

把几个点击和scroll的方法都重载一下，分离出传入root的，以防getRootInActiveWindow失败。



持久化配置是否使用悬浮窗





### 多线程

如果执行的操作不能即时完成，则应确保它们在单独的线程（“后台”或“工作”线程）中运行。和Activity一样。

首先创建一个线程，然后在这个线程做一些阻塞的操作，如果执行完毕，则不能直接调用操作界面的方法，而是通知主线程调用。

```
public void onClick(View v) {
    new Thread(new Runnable() {
        public void run() {
            // a potentially time consuming task
            final Bitmap bitmap =
                    processBitMap("image.png");
            imageView.post(new Runnable() {
                public void run() {
                    imageView.setImageBitmap(bitmap);
                }
            });
        }
    }).start();
}
```

```
    private void showToast(final String s) {
        mainHandler.post(new Runnable() {
            private String msg;
            {
                this.msg = s;
            }
            @Override
            public void run() {
                Toast.makeText(MyAccessibilityService.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
```





TODO:

1. 测试每日上报提醒的点击。
2. click
3. 自动定时启动，解锁屏幕，foreground，启动微信
4. 判断网络，判断gps打开。
5. api版本小于安卓N则提示不能用
6. 拓展抢红包功能，自动收能量功能





## Webview

没必要全上web，毕竟最后sharedPreference还是要用的，accessibility service也是少不了的，全上web只是一个UI的选择罢了。

找个合适的web的lua的代码编辑器，先在手机上试下体验。

用本地的file作为uri，别上http服务器了。考虑

怎么把网页打包到apk里面？[assets](https://stackoverflow.com/questions/5403062/android-add-files-into-apk-package)：放到apk的assets/，安装的时候就会被放到app内部文件夹里：`final InputStream is = getResources().getAssets().open("some_file.xml")`。

代码编辑器选型：[wikipedia -Comparison of JavaScript-based source code editors](https://en.wikipedia.org/wiki/Comparison_of_JavaScript-based_source_code_editors)。

1. vscode的[monaco-editor](https://microsoft.github.io/monaco-editor/)。 。。。不支持mobile浏览器。
2. [codejar](https://github.com/antonmedv/codejar) 小巧
3. [ace](https://ace.c9.io/) 中等。

codejar+highlight.js吧。



### 单文件构建

因为安全问题，html单文件不能加载任何本地js文件。。。所以需要构建出单文件html。

1. vscode的`![tab]`为基础。body里面先放一个`<div class="editor"></div>`

2. 首先加入codejar的代码`<script type="module">`，末尾加入自己的代码，再加入highlight.js代码

   因为似乎module的import必须通过其他文件的形式。所以我们单个html没办法，把自己的代码都加进去，不需要export了。

   需要加入codejar的风格的css和highlight.js的css。选一种风格的css加入即可。

3. ```js
   jar.onUpdate(code => {
       window.LuaCode = code;
   })
   ```

   这样LuaCode变量总是保存着最新的代码。

module内访问外部：`window`变量。外部与module交互：`System.import()``import()`函数。



TODO：网页内部加一个切换主题的按钮。支持保存默认主题？通过导出主题设置到外面



## Android-UI

记录如何实现多页面切换的主界面学习的知识。学习两个studio自带的example

看来虽然底层是Fragment-Manager，但是这些页面往往使用的是androidx jetpack中的包。

设计：BottomNavigation单独分离帮助页面，用来写文档，留一个Home作为主程序。Home页面使用view pager，一页用于保存代码，一页用于设置和开关。

ViewModel有什么好处？设备方向改变，页面刷新了，页面的相关配置数据还在。

1. 以BottomNavigation为顶层，分为Home和Doc和Settings。Doc页面使用ListView
2. settings，设置界面，设置自动执行时间等持久化的设置。
3. Home界面使用ViewPager和Tab用于切换
4. Home界面的子界面有
   1. code，代码编辑界面，包含一个webview和一个保存按钮
   2. control界面，包含测试用按钮，开关等。启动当前程序进行测试，开启关闭悬浮框，开启关闭debug。包含代码数据结果（允许service动态发送消息显示log。）

### [Tabbed activity - ViewPager](https://developer.android.com/guide/navigation/navigation-swipe-view)

[tab layout item](https://stackoverflow.com/questions/38034966/how-is-tabitem-used-when-placed-in-the-layout-xml) 是新加入的，所以找不到教程，基本上就是帮你增加tab。

### BottomNavigation - 

```
import com.google.android.material.bottomnavigation.BottomNavigationView
```

[codelab](https://developer.android.com/codelabs/android-navigation#0) and [Get started](https://developer.android.com/guide/navigation/navigation-getting-started) 。



### 如何理解Fragment中setArgument和savedInstanceState的区别

setArgument只能在attach到activity前调用。所以Fragment自己的数据还是需要

### Fragment使用要点

1. onCreateView加载layout创建View。
2. [构建函数](https://developer.android.com/reference/androidx/fragment/app/Fragment#Fragment()) [新建Fregment - stackoverflow](https://stackoverflow.com/questions/9245408/best-practice-for-instantiating-a-new-android-fragment) 使用newInstace这个工厂方法的原因是保存数据，把数据设置到argument里。
3. 

### 代码编辑页面

包含一个webView和一个保存按钮。

1. 使用ViewModel保存代码数据。

2. 包装自己的WebView实现代码框逻辑。

   [extend-webview1](https://stackoverflow.com/questions/15097264/is-there-a-way-to-override-the-behavior-of-webview) [extend webview 2](https://stackoverflow.com/questions/41483342/create-a-custom-webview) 把代码路径设置成properties

3. Fragment负责添加onclick listener。
4. 

### WebView传递参数

webView只提供了interface，使得JavaScript能够调用到安卓这边的代码。我需要的是

1. 最开始的时候读取代码文件，加载文件中的代码进去。 - 查资料发现只能 evaluate javascript。
2. 用户点击保存按钮的时候获取代码，保存到文件。 - evaluate javascript。

似乎没有需要网页端调用这边的情况。。。

由于lua里可能也有双引号字符，反斜杠等。。。我需要转义。我还是直接base64编码一下吧。。。

### Preference + TimePicker

[教程](https://medium.com/@JakobUlbrich/building-a-settings-screen-for-android-part-3-ae9793fd31ec) 自从androidx以来，Preference想弹出自己的Dialog就变了很多，需要两个类。

1. 自建TimePreference，persist一个`int mTime`。唯一特殊的是getDialogLayoutResource函数返回自建的Layout，里面只包含一个time picker。
2. 自建`TimePreferenceDialogFragmentCompat`拓展`PreferenceDialogFragmentCompat`类。`newInstance`工厂方法。onBindDialogView对参数中的View，也就是上面的layout得到的View做操作，设置初始时间等。onDialogClosed获取最终结果，保存到timePreference里面。这个类可以通过getPreference函数得到TimePreference，正是通过getArgument函数获得工厂方法里面设置的参数来知道是哪个Preference的。
3. TimePreference之所以存在都是由老大哥罩着。找到SettingsFragment这个控制整个Preference的Fragment，在onDisplayPreferenceDialog对TimePreference做特殊处理，实例化上一步拓展的类去控制它。这里设置了`TimePreferenceDialogFragmentCompat`的`setTargetFragment`函数，这一步设置和工厂类的setArgument一起，使得`TimePreferenceDialogFragmentCompat`的getPreference能够正常工作。



## APK签名

https://stackoverflow.com/questions/20268520/generate-signed-apk-android-studio/27065677

可以生成的



## 其他

### 找不到adb设备

手机要设置为MTP，设备时间
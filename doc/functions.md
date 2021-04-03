## 函数

采用Lua解释器，引入的函数包叫做`auto`。以下的所有函数都需要加上`auto.`访问。例如调用click函数：`auto.click(1,1)`。

[toc]

### click(x, y)

x，y都是整数类型，代表屏幕的坐标。

如果想要查看屏幕的坐标，请在设置中打开`开发者选项`，找到`指针位置`打开，这样就可以在按住屏幕不放的时候在屏幕顶部看到坐标了。

注意开发者选项需要特殊的方式才能找到，详情请自行搜索。

### sleep_ms(t)

t是整数类型。代表延时的毫秒数。

这个函数非常重要，建议在每两个操作之间都加上一个延时。

只有在延时开始和结束的时候会检测是否需要中断当前任务。（点击了通知栏的通知的时候会中断当前任务）

### open_next(str)

`str`是字符串类型。这个函数会找到屏幕上带有对应内容的节点，并找到包含它的可点击元素，进行点击。

底层调用`findAccessibilityNodeInfosByText`，查找方式：`case insensitive containment`

### scroll_down()

找到第一个可以下滑动的节点，并向下滑动。

比如在`webView`如微信的网页页面中下滑

### start_app(package_name)

需要打开允许后台弹出界面权限。

`package_name`是应用的包名。微信是`com.tencent.mm`。

### global_action(action)

action是整数类型（枚举类型）需要使用auto模块预定义的变量。例如：

```lua
auto.global_action(auto.GLOBAL_ACTION_BACK)
```

表示执行返回操作。

预定义的操作和对应的值如下：

```c
        {"GLOBAL_ACTION_BACK", 1},
        {"GLOBAL_ACTION_HOME", 2},
        {"GLOBAL_ACTION_RECENTS", 3},
        {"GLOBAL_ACTION_NOTIFICATIONS", 4},
        {"GLOBAL_ACTION_QUICK_SETTINGS", 5},
        {"GLOBAL_ACTION_POWER_DIALOG", 6},
        {"GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN", 7},
        // 以下需要安卓9
        {"GLOBAL_ACTION_LOCK_SCREEN", 8},
        {"GLOBAL_ACTION_TAKE_SCREENSHOT", 9},
        // 以下为安卓12新增
        {"GLOBAL_ACTION_KEYCODE_HEADSETHOOK", 10},
        {"GLOBAL_ACTION_ACCESSIBILITY_BUTTON", 11},
        {"GLOBAL_ACTION_ACCESSIBILITY_BUTTON_CHOOSER", 12},
        {"GLOBAL_ACTION_ACCESSIBILITY_SHORTCUT", 13},
        {"GLOBAL_ACTION_ACCESSIBILITY_ALL_APPS", 14},
```

### toast(str)

弹出包含指定字符串的toast框

### debug_log(str)

打印str到logcat。

### get_clickable_by_text(str)

返回带有对应字符串的可点击节点，如果没有对应节点，则返回nil。
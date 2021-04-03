## FAQ

1. 使用这个软件需要怎么做？

   除了安装还需要去设置里打开无障碍服务，开启允许后台弹出界面权限等。

   可以考虑把通知重要程度调到最高。

2. 如何知道Click函数的坐标？如何查看坐标？

   `开发者选项`中有`指针位置`这一开关，打开后即可看到触摸时的轨迹和坐标。这样就可以记录下每次点击的坐标，写下来，然后再放到代码里。

   注意开发者选项需要特殊的方式才能找到，详情请自行搜索。

3. 为什么有时候goNext()函数会失灵？

   有的时候某些看上去在屏幕上的字，其实可能是图片，或者不在UI界面中。最好能将手机连接到电脑，打开usb调试，使用`UIAutomatorViewer`（Android sdk中的工具，可以通过Android Studio安装）查看界面布局。就可以知道哪些字是可以使用goNext函数跳转的。另外这样还可以看到各个元素的坐标。
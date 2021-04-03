## AutoLua 使用教程



### 准备

1. 在设置中的无障碍服务里启用`脚本执行服务`
2. 在应用信息里，打开`允许后台弹出界面权限`。（如果有的话）
3. 在应用信息里的通知设置里，保证通知能正常发出声音。



### 编写脚本

我的自动打卡的例子：

```lua
-- 判断是否是微信主页
function is_wechat_home()
    strs = {"微信", "发现", "我", "通讯录"}
    for i= 1,4 do
        if (auto.get_clickable_by_text(strs[i]) == nil) then
            return false
        end
    end
    return true
end

-- 启动微信并回到主页
function wechat_home ()
    auto.start_app("com.tencent.mm")
    auto.sleep_ms(2000)
    for i= 1,5 do
        auto.sleep_ms(1000)
        if (not is_wechat_home()) then
            auto.global_action(auto.GLOBAL_ACTION_BACK)
        end
    end 
end

wechat_home()
```



### 测试

可以在设置中打开启用浮动栏，从而方便地启动执行，测试脚本效果。




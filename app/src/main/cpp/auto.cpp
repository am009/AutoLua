//
// Created on 3/9/2021.
//

extern "C" {
#include "lua.h"
#include "lauxlib.h"
}

#include <jni.h>
#include <android/log.h>

#include "auto.h"

auto_lualib_global_state AUTO_LUALIB_STATE = {nullptr, nullptr, nullptr};

static const luaL_Reg autoLib [] = {
        {"click", l_click},
        {"open_next", l_open_next},
        {"get_clickable_by_text", l_get_clickable_by_text},
        {"click_node", l_click_node},
        {"start_app", l_start_app},
        {"toast", l_toast},
        {"debug_log", l_debug_log},
        {"get_scrollable", l_get_scrollable},
        {"scroll_node", l_scroll_node},
        {"sleep_ms", l_sleep_ms},
        {"scroll_down", l_scroll_down},
        {"global_action", l_global_action},
        {nullptr, nullptr}  /* sentinel */
};

static const int_field_reg auto_int_fields [] = {
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
        // Node相关
        {"NOREF", LUA_NOREF},
        {nullptr, 0}
};

#define PREPARE_JNI   JNIEnv *env = AUTO_LUALIB_STATE.env;\
jobject obj = AUTO_LUALIB_STATE.thiz;\
jclass cls = AUTO_LUALIB_STATE.cls;

/* assume that table is at the top */
void set_int_field (lua_State* L, const char *index, int value) {
    lua_pushstring(L, index);
    lua_pushinteger(L, value);
    lua_settable(L, -3);
}

int luaopen_auto(lua_State* L)
{
    lua_newtable(L);
    luaL_setfuncs(L, autoLib, 0);

    // register int fields
    int i = 0;
    while (auto_int_fields[i].name != nullptr){
        set_int_field(L, auto_int_fields[i].name, auto_int_fields[i].value);
        i++;
    }
    return 1;
}

int l_click(lua_State* L)
{
    if (lua_gettop(L) != 2) {
        return luaL_error(L, "expecting exactly 2 arguments");
    }
    lua_Integer x = luaL_checkinteger(L, 1);
    lua_Integer y = luaL_checkinteger(L, 2);

    PREPARE_JNI

    jmethodID mid = env->GetMethodID(cls, "click", "(II)V");
    if (mid == nullptr) {
        __android_log_write(ANDROID_LOG_ERROR, "auto-lua.cpp", "GetMethodID Failed.");
    }

//    __android_log_print(ANDROID_LOG_DEBUG, "auto-lua.cpp", "x: %d, y: %d.", (int)x, (int)y);

    env->CallVoidMethod(obj, mid, (jint)x, (jint)y);
    if (env->ExceptionCheck()) {
        return luaL_error(L, "Java exception on calling function: click");
    }
    return 0;
}

int l_open_next(lua_State* L)
{
    const char *s = luaL_checkstring(L, 1);

    PREPARE_JNI

    jclass class1 = env->FindClass("moe/wjk/autolua/MyAccessibilityService");
    jmethodID mid = env->GetMethodID(class1, "openNext", "(Ljava/lang/String;)Z");
    jstring str = env->NewStringUTF(s);
    if (env->ExceptionCheck()) {
        return luaL_error(L, "JNI exception on calling function: startApp: NewStringUTF");
    }
    jboolean ret = env->CallBooleanMethod(obj, mid, str);
    if (env->ExceptionCheck()) {
        return luaL_error(L, "Java exception on calling function: startApp");
    }
    lua_pushboolean(L, ret);
    return 1;
}


int l_start_app(lua_State* L)
{
    const char *s = luaL_checkstring(L, 1);

    PREPARE_JNI

    jclass class1 = env->FindClass("moe/wjk/autolua/MyAccessibilityService");
    jmethodID mid = env->GetMethodID(class1, "startApp", "(Ljava/lang/String;)V");
    jstring str = env->NewStringUTF(s);
    if (env->ExceptionCheck()) {
        return luaL_error(L, "JNI exception on calling function: startApp: NewStringUTF");
    }
    env->CallVoidMethod(obj, mid, str);
    if (env->ExceptionCheck()) {
        return luaL_error(L, "Java exception on calling function: startApp");
    }

    return 0;
}

int l_toast(lua_State* L)
{
    const char *s = luaL_checkstring(L, 1);
//    __android_log_print(ANDROID_LOG_DEBUG, "auto-lua.cpp", "toast: %s.", s);

    PREPARE_JNI

    jclass class1 = env->FindClass("moe/wjk/autolua/MyAccessibilityService");
    jmethodID mid = env->GetMethodID(class1, "showToast", "(Ljava/lang/String;)V");
    jstring str = env->NewStringUTF(s);
    if (env->ExceptionCheck()) {
        return luaL_error(L, "JNI exception on calling function: toast: NewStringUTF");
    }
    env->CallVoidMethod(obj, mid, str);
    if (env->ExceptionCheck()) {
        return luaL_error(L, "Java exception on calling function: toast: showToast");
    }

    return 0;
}

int l_debug_log(lua_State* L)
{
    const char *s = luaL_checkstring(L, 1);
    __android_log_print(ANDROID_LOG_DEBUG, "Lua_debug_log", "%s", s);
    return 0;
}


int l_sleep_ms(lua_State* L)
{
    lua_Integer x = luaL_checkinteger(L, 1);

    PREPARE_JNI

    jclass class1 = env->FindClass("moe/wjk/autolua/MyAccessibilityService");
    if (class1 == nullptr) {
        return luaL_error(L, "INTERNAL ERROR: sleep_ms: Unable to find MyAccessibilityService class.");
    }
    jmethodID mid = env->GetMethodID(class1, "lua_sleep_ms", "(I)V");
    if (mid == nullptr) {
        return luaL_error(L, "INTERNAL ERROR: sleep_ms: Unable to find MyAccessibilityService.lua_sleep_ms method.");
    }

    env->CallVoidMethod(obj, mid, (jint) x);
    if (env->ExceptionCheck()) {
        return luaL_error(L, "Java exception on calling function: sleep_ms");
    }

    return 0;
}

int l_global_action(lua_State* L)
{
    lua_Integer x = luaL_checkinteger(L, 1);

    PREPARE_JNI

    jclass class1 = env->FindClass("moe/wjk/autolua/MyAccessibilityService");
    if (class1 == nullptr) {
        return luaL_error(L, "INTERNAL ERROR: global_action: Unable to find MyAccessibilityService class.");
    }
    jmethodID mid = env->GetMethodID(class1, "global_action", "(I)V");
    if (mid == nullptr) {
        return luaL_error(L, "INTERNAL ERROR: global_action: Unable to find MyAccessibilityService.global_action method.");
    }

    env->CallVoidMethod(obj, mid, (jint) x);
    if (env->ExceptionCheck()) {
        return luaL_error(L, "Java exception on calling function: global_action");
    }

    return 0;
}

int l_scroll_down(lua_State* L)
{
    PREPARE_JNI

    jclass class1 = env->FindClass("moe/wjk/autolua/MyAccessibilityService");
    jmethodID mid = env->GetMethodID(class1, "scrollDown", "()Z");
    if (mid == nullptr) {
        __android_log_write(ANDROID_LOG_ERROR, "auto-lua.cpp: scrollDown", "GetMethodID Failed.");
    }
//    __android_log_print(ANDROID_LOG_DEBUG, "auto-lua.cpp", "x: %d, y: %d.", (int)x, (int)y);

    jboolean ret = env->CallBooleanMethod(obj, mid);
    if (env->ExceptionCheck()) {
        return luaL_error(L, "Java exception on calling function: scrollDown");
    }
    lua_pushboolean(L, ret);
    return 1;
}


int l_get_clickable_by_text(lua_State* L)
{
    const char *s = luaL_checkstring(L, 1);

    PREPARE_JNI

    jclass class1 = env->FindClass("moe/wjk/autolua/MyAccessibilityService");
    jmethodID mid = env->GetMethodID(class1, "getClickableByText",
                                     "(Ljava/lang/String;)Landroid/view/accessibility/AccessibilityNodeInfo;");
    if (mid == nullptr) {
        __android_log_write(ANDROID_LOG_ERROR, "auto-lua.cpp: get_clickable_by_text", "GetMethodID Failed.");
    }

    jstring str = env->NewStringUTF(s);
    if (env->ExceptionCheck()) {
        return luaL_error(L, "JNI exception on calling function: get_clickable_by_text: NewStringUTF");
    }

    jobject node = env->CallObjectMethod(obj, mid, str);
    if (env->ExceptionCheck()) {
        return luaL_error(L, "Java exception on calling function: getClickableByText");
    }

    if (node != nullptr) {
        lua_pushlightuserdata(L, node);
    } else {
        lua_pushnil(L);
    }

    return 1;
}

int l_click_node(lua_State* L)
{
    return 0;
}

int l_get_scrollable(lua_State* L)
{
    PREPARE_JNI

    jclass class1 = env->FindClass("moe/wjk/autolua/MyAccessibilityService");
    jmethodID mid = env->GetMethodID(class1, "getScrollableNode",
                                     "()Landroid/view/accessibility/AccessibilityNodeInfo;");
    if (mid == nullptr) {
        __android_log_write(ANDROID_LOG_ERROR, "auto-lua.cpp: get_scrollable", "GetMethodID Failed.");
    }

    jobject node = env->CallObjectMethod(obj, mid);
    if (env->ExceptionCheck()) {
        return luaL_error(L, "Java exception on calling function: getScrollableNode");
    }

    if (node != nullptr) {
        lua_pushlightuserdata(L, node);
    } else {
        lua_pushnil(L);
    }

    return 1;
}

int l_scroll_node(lua_State* L)
{

    return 0;
}

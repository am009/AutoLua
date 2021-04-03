

extern "C" {
#include "lua.h"
#include "lauxlib.h"
}

#include <jni.h>
#include <android/log.h>

#include "dyn.h"

JNIEnv *DYN_GLOBAL_ENV = nullptr;
jobject DYN_GLOBAL_THIZ = nullptr;
jclass DYN_GLOBAL_CLS = nullptr;

static const luaL_Reg dynLib [] = {
        {"get_method_id",  l_get_method_id},
        {"find_class",  l_find_class},
        {"get_thiz",  l_get_thiz},
        {"get_thiz_cls",  l_get_thiz_cls},
        {"call_void_method",  l_call_void_method},
        {"call_boolean_method",  l_call_boolean_method},
        {"call_int_method",  l_call_int_method},
        {"call_object_method",  l_call_object_method},
        {nullptr, nullptr}  /* sentinel */
};


int luaopen_dyn(lua_State* L)
{
    lua_newtable(L);
    luaL_setfuncs(L, dynLib, 0);

    return 1;
}

int l_get_method_id(lua_State* L)
{
    if (lua_gettop(L) != 3) {
        return luaL_error(L, "expecting exactly 3 arguments");
    }
    luaL_checktype(L,1,LUA_TLIGHTUSERDATA);
    auto cls = static_cast<jclass>(lua_touserdata(L,1));
    const char *name = luaL_checkstring(L, 2);
    const char *sig = luaL_checkstring(L, 3);

    jmethodID mid = DYN_GLOBAL_ENV->GetMethodID(cls, "click", "(II)V");

    if (mid != nullptr) {
        lua_pushlightuserdata(L, mid);
    } else {
        lua_pushnil(L);
    }
    return 1;
}

int l_find_class(lua_State* L)
{
    const char *name = luaL_checkstring(L, 1);

    jclass cls = DYN_GLOBAL_ENV->FindClass(name);

    if (cls != nullptr) {
        lua_pushlightuserdata(L, cls);
    } else {
        lua_pushnil(L);
    }
    return 1;
}

/*
 * start_ind is inclusive
 */
static jvalue* craft_args(lua_State* L, int start_ind)
{
    int top = lua_gettop(L);
    int count = top - start_ind + 1;
    if (count <= 0) {
        return nullptr;
    }
    auto *args = new jvalue[count];
    for(int i = start_ind; i <= top; i++) {
        switch (lua_type(L, i)) {
            case LUA_TNIL:
                args[i].l = nullptr;
                break;
            case LUA_TBOOLEAN:
                if (lua_toboolean(L, i)) {
                    args[i].z = JNI_TRUE;
                } else {
                    args[i].z = JNI_FALSE;
                }
                break;
            case LUA_TLIGHTUSERDATA:
                args[i].l = static_cast<jobject>(lua_touserdata(L, i));
                break;
            case LUA_TNUMBER:
                if (lua_isinteger(L, i)) {
                    args[i].i = lua_tointeger(L, i);
                } else {
                    args[i].d = lua_tonumber(L, i);
                }
                break;
            case LUA_TSTRING:
                const char* s;
                jstring str;

                s = lua_tostring(L, i);
                str = DYN_GLOBAL_ENV->NewStringUTF(s);
                if (DYN_GLOBAL_ENV->ExceptionCheck()) {
                    luaL_error(L, "JNI exception on calling function: craft_args: NewStringUTF");
                    return nullptr;
                }
                args[i].l = str;
                break;
            default:
                luaL_error(L, "craft_args: Unknown parameter type when calling java method");
                return nullptr;
                break;
        }
    }
    return args;
}

static void* luaL_checklud(lua_State *L, int ind) {
    luaL_checktype(L, ind, LUA_TLIGHTUSERDATA);
    return lua_touserdata(L, ind);
}

int l_get_thiz(lua_State* L)
{
    lua_pushlightuserdata(L, DYN_GLOBAL_THIZ);
    return 1;
}

int l_get_thiz_cls(lua_State* L)
{
    lua_pushlightuserdata(L, DYN_GLOBAL_CLS);
    return 1;
}

int l_call_void_method(lua_State* L)
{
    auto obj = static_cast<jobject>(luaL_checklud(L, 1));
    auto mid = static_cast<jmethodID>(luaL_checklud(L, 2));
    jvalue* args = craft_args(L, 3);

    DYN_GLOBAL_ENV->CallVoidMethodA(obj, mid, args);
    if (DYN_GLOBAL_ENV->ExceptionCheck()) {
        return luaL_error(L, "Java exception on calling function: l_call_void_method");
    }

    return 0;
}

int l_call_boolean_method(lua_State* L)
{
    auto obj = static_cast<jobject>(luaL_checklud(L, 1));
    auto mid = static_cast<jmethodID>(luaL_checklud(L, 2));
    jvalue* args = craft_args(L, 3);

    jboolean ret = DYN_GLOBAL_ENV->CallBooleanMethodA(obj, mid, args);
    if (DYN_GLOBAL_ENV->ExceptionCheck()) {
        return luaL_error(L, "Java exception on calling function: l_call_void_method");
    }

    lua_pushboolean(L, ret);
    return 1;
}

int l_call_int_method(lua_State* L)
{
    auto obj = static_cast<jobject>(luaL_checklud(L, 1));
    auto mid = static_cast<jmethodID>(luaL_checklud(L, 2));
    jvalue* args = craft_args(L, 3);

    jint ret = DYN_GLOBAL_ENV->CallIntMethodA(obj, mid, args);
    if (DYN_GLOBAL_ENV->ExceptionCheck()) {
        return luaL_error(L, "Java exception on calling function: l_call_void_method");
    }

    lua_pushinteger(L, ret);
    return 1;
}

static bool j_is_string(jobject o)
{
//    static jclass string_class = nullptr;
//    if (string_class == nullptr) {
//        string_class = DYN_GLOBAL_ENV->FindClass("java/lang/String");
//    }
    // o instanceof String
    return DYN_GLOBAL_ENV->IsInstanceOf(o, DYN_GLOBAL_ENV->FindClass("java/lang/String"));
}

int l_call_object_method(lua_State* L)
{
    auto obj = static_cast<jobject>(luaL_checklud(L, 1));
    auto mid = static_cast<jmethodID>(luaL_checklud(L, 2));
    jvalue* args = craft_args(L, 3);

    jobject ret = DYN_GLOBAL_ENV->CallObjectMethodA(obj, mid, args);
    if (DYN_GLOBAL_ENV->ExceptionCheck()) {
        return luaL_error(L, "Java exception on calling function: l_call_void_method");
    }

    if (ret != nullptr) {
        lua_pushnil(L);
    } else if (j_is_string(ret)) {
        const char * str = DYN_GLOBAL_ENV->GetStringUTFChars((jstring)ret, nullptr);
        lua_pushstring(L, str);
        DYN_GLOBAL_ENV->ReleaseStringUTFChars((jstring)ret, str);
    } else {
        lua_pushlightuserdata(L, ret);
    }
    return 1;
}


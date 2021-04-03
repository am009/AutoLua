#include <jni.h>
#include <string>

extern "C" {
#include <lua.h>
#include <lauxlib.h>
#include <lualib.h>
}

#include "auto.h"
#include "dyn.h"
#include <android/log.h>

// should not error
jboolean setup_lualib(lua_State* L, lua_CFunction open_func, const char* lib_name) {
    int error;
    lua_pushcfunction(L, luaopen_auto);
    error = lua_pcall(L, 0, 1, 0); // return a table
    if (error) {
        __android_log_print(ANDROID_LOG_ERROR, "LuaRunner", "%s", lua_tostring(L, -1));
        lua_pop(L, 1);  /* pop error message from the stack */
        return JNI_FALSE;
    }
    lua_setglobal(L, lib_name);
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_moe_wjk_autolua_MyAccessibilityService_runFile(JNIEnv *env, jobject thiz,
                                                                   jstring path) {
    int error;
    jclass cls;
    lua_State *L = luaL_newstate();
    if (L == nullptr) {
        __android_log_write(ANDROID_LOG_ERROR, "LuaRunner", "cannot create state: not enough memory");
        return JNI_FALSE;
    }
    luaL_openlibs(L);

    const char *codePath = env->GetStringUTFChars(path, nullptr);
    jboolean ret = JNI_TRUE;

    // set up auto lib as "auto"
    ret = setup_lualib(L, luaopen_auto, AUTO_LUA_LIB);
    if (ret == JNI_FALSE) {
        goto cleanup;
    }

    // set up dyn lib as "dyn"
    ret = setup_lualib(L, luaopen_dyn, DYN_LUA_LIB);
    if (ret == JNI_FALSE) {
        goto cleanup;
    }

    cls = env->GetObjectClass(thiz);
    { // setup env
        AUTO_LUALIB_STATE = {env, thiz, cls};
        DYN_GLOBAL_ENV = env;
        DYN_GLOBAL_THIZ = thiz;
        DYN_GLOBAL_CLS = cls;
    }

    error = luaL_loadfile(L,codePath) || lua_pcall(L, 0, 0, 0);
    if (error) {
        const char * error_str = lua_tostring(L, -1);
        __android_log_print(ANDROID_LOG_ERROR, "LuaRunner", "%s", error_str);
        lua_pop(L, 1);  /* pop error message from the stack */
        ret = JNI_FALSE;

        jthrowable exp = env->ExceptionOccurred();
        if (exp != nullptr) {
            env->ExceptionClear();
            jmethodID mid = env->GetMethodID(cls, "onScriptError", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
            jstring str = env->NewStringUTF(error_str);
            env->CallVoidMethod(thiz, mid, str, exp);
        } else {
            jmethodID mid = env->GetMethodID(cls, "onScriptError", "(Ljava/lang/String;)V");
            jstring str = env->NewStringUTF(error_str);
            env->CallVoidMethod(thiz, mid, str);
        }
        goto cleanup;
    }

//    FILE *code = fopen(codePath, "r");
//    if (code == nullptr) {
//        __android_log_print(ANDROID_LOG_ERROR, "LuaRunner: ", "Open %s failed.", codePath);
//    }
//    while (fgets(buff, sizeof(buff), code) != nullptr) {
//        error = luaL_loadbuffer(L, buff, strlen(buff), "line") ||
//                lua_pcall(L, 0, 0, 0);
//        if (error) {
//            __android_log_print(ANDROID_LOG_ERROR, "LuaRunner: ", "%s", lua_tostring(L, -1));
//            lua_pop(L, 1);  /* pop error message from the stack */
//            break; // ?
//        }
//    }

cleanup:
    __android_log_write(ANDROID_LOG_DEBUG, "LuaRunner", "runner finished.");
    {
        AUTO_LUALIB_STATE = {nullptr, nullptr, nullptr};
        DYN_GLOBAL_ENV = nullptr;  DYN_GLOBAL_THIZ = nullptr;  DYN_GLOBAL_CLS = nullptr;
    }
    lua_close(L);
    env->ReleaseStringUTFChars(path, codePath);
    return ret;
}

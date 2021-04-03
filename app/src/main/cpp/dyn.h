
#ifndef AUTOLUA_DYN_H
#define AUTOLUA_DYN_H

#define DYN_LUA_LIB "dyn"

extern JNIEnv *DYN_GLOBAL_ENV;
extern jobject DYN_GLOBAL_THIZ;
extern jclass DYN_GLOBAL_CLS;

int luaopen_dyn(lua_State* L);

int l_get_method_id(lua_State* L);
int l_find_class(lua_State* L);
int l_get_thiz(lua_State* L);
int l_get_thiz_cls(lua_State* L);
int l_call_void_method(lua_State* L);
int l_call_boolean_method(lua_State* L);
int l_call_int_method(lua_State* L);
int l_call_object_method(lua_State* L);


#endif //AUTOLUA_DYN_H

//
// Created by warren on 3/9/2021.
//

#ifndef AUTOLUA_AUTO_H
#define AUTOLUA_AUTO_H

#define AUTO_LUA_LIB "auto"

struct auto_lualib_global_state {
    JNIEnv *env;
    jobject thiz;
    jclass cls;
};

typedef struct int_field_reg {
    const char *name;
    int value;
} int_field_reg;

extern auto_lualib_global_state AUTO_LUALIB_STATE;

int luaopen_auto(lua_State* L);

int l_click(lua_State* L);
int l_open_next(lua_State* L);
int l_get_clickable_by_text(lua_State* L);
int l_click_node(lua_State* L);
int l_start_app(lua_State* L);
int l_toast(lua_State* L);
int l_debug_log(lua_State* L);
int l_get_scrollable(lua_State* L);
int l_scroll_node(lua_State* L);
int l_sleep_ms(lua_State* L);
int l_scroll_down(lua_State* L);
int l_global_action(lua_State* L);


#endif //AUTOLUA_AUTO_H

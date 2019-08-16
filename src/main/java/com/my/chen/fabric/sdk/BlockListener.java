package com.my.chen.fabric.sdk;

import com.alibaba.fastjson.JSONObject;


/**
 * 描述：BlockListener监听返回map集合
 */
public interface BlockListener {
    int SUCCESS = 200;
    int ERROR = 9999;

    void received(JSONObject jsonObject);
}

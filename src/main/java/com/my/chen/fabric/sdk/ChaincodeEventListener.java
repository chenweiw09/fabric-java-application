package com.my.chen.fabric.sdk;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by Wei Chen on 10:33 2019/8/15.
 */
public interface ChaincodeEventListener {

    void received(String handle, JSONObject jsonObject, String eventName, String chaincodeId, String txId);

}

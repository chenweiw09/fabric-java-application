package com.my.chen.fabric.app.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.json.JSONException;


public interface BaseService {

    int SUCCESS = 200;
    int FAIL = 9999;

    default String responseSuccess(String result) {
        JSONObject jsonObject = parseResult(result);
        jsonObject.put("code", SUCCESS);
        return jsonObject.toString();
    }

    default String responseSuccess(String result, String txid) {
        JSONObject jsonObject = parseResult(result);
        jsonObject.put("code", SUCCESS);
        jsonObject.put("txid", txid);
        return jsonObject.toString();
    }

    default String responseSuccess(JSONObject json) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", SUCCESS);
        jsonObject.put("data", json);
        return jsonObject.toString();
    }

    default String responseSuccess(JSONArray array) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", SUCCESS);
        jsonObject.put("data", array);
        return jsonObject.toString();
    }

    default String responseFail(String result) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", FAIL);
        jsonObject.put("error", result);
        return jsonObject.toString();
    }

    default JSONObject parseResult(String result) {
        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject.parseObject(result);
            jsonObject.put("data", result);
            return jsonObject;
        } catch (JSONException ex) {
            try {
                JSONObject.parseArray(result);
                jsonObject.put("data", result);
                return jsonObject;
            } catch (JSONException ex1) {
                jsonObject.put("data", result);
                return jsonObject;
            }
        }
    }

}

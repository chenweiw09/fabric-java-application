package com.my.chen.fabric.app.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/27
 * @description
 */
@Slf4j
public class HttpUtil {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient client = new OkHttpClient();

    public static void post(String jsonParam, String url) throws IOException {
        RequestBody body = RequestBody.create(JSON, jsonParam);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        log.info("post url:"+url+"|param:"+jsonParam+"|response code:"+response.code()+"|response body:{}", response.body());
    }



}

package com.imooc.framework.manager;

import com.imooc.framework.cloud.CloudManager;
import com.imooc.framework.utils.SHA1;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpManager {

    private static volatile HttpManager mInstance = null;
    private OkHttpClient okHttpClient;

    private HttpManager() {
        okHttpClient = new OkHttpClient();
    }

    public static HttpManager getInstance() {
        if (mInstance == null) {
            synchronized (HttpManager.class) {
                if (mInstance == null) {
                    mInstance = new HttpManager();
                }
            }
        }

        return mInstance;
    }

    public String postCloudToken(HashMap<String, String> map) {
        // 参数
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = String.valueOf(Math.floor(Math.random() * 100000));
        String signature = SHA1.sha1(CloudManager.CLOUD_SECRET + nonce + timestamp);

        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                .url(CloudManager.TOKEN_URL)
                .addHeader("RC-Timestamp", timestamp)
                .addHeader("RC-App-Key", CloudManager.CLOUD_KEY)
                .addHeader("RC-Nonce", nonce)
                .addHeader("RC-Signature", signature)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .build();
        try {
            return okHttpClient.newCall(request).execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}

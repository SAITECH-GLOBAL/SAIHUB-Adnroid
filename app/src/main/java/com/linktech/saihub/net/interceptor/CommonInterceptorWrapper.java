package com.linktech.saihub.net.interceptor;

import android.text.TextUtils;

import com.linktech.saihub.util.Sha1Util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommonInterceptorWrapper implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        //获取到request
        Request request = chain.request();

        List<String> headerValues = request.headers("url_name");
        if (headerValues != null && !headerValues.isEmpty()) {
            String headStr = headerValues.get(0);
            switch (headStr) {
                case "LiteStringConstant.DOMAIN_COVALENT":
                    return chain.proceed(chain.request());
                default:
                    break;
            }
        }

        //获取到方法
        String url = request.url().toString();
        LinkedHashMap<String, String> paramMap = new LinkedHashMap<>();
        try {
            if (url.contains("?")) {
                String paramStr = url.split("\\?")[1];
                url = url.split("\\?")[0];
                if (!TextUtils.isEmpty(paramStr) && paramStr.contains("=") && !paramStr.contains("&")) {
                    String[] split = paramStr.split("=");
                    if (split.length > 1) {
                        String key = split[0];
                        String value = split[1];
                        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                            paramMap.put(key, value);
                        }
                    }
                } else if (!TextUtils.isEmpty(paramStr) && paramStr.contains("=") && paramStr.contains("&")) {
                    String[] keyAndValueArrayStr = paramStr.split("&");
                    for (String keyAndValue : keyAndValueArrayStr) {
                        if (!TextUtils.isEmpty(keyAndValue) && keyAndValue.contains("=") && keyAndValue.split("=").length > 1) {
                            String key = keyAndValue.split("=")[0];
                            String value = keyAndValue.split("=")[1];
                            if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                                paramMap.put(key, value);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        paramMap.put("ts", System.currentTimeMillis() + "");
        StringBuilder paramStr = new StringBuilder();
        Set<Map.Entry<String, String>> entries = paramMap.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            paramStr.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        if (!TextUtils.isEmpty(paramStr)) {
            url = url + "?" + paramStr.substring(0, paramStr.length() - 1);
            String appSign = getAppXsign(paramMap);
            Request.Builder builder = request.newBuilder();
            String httpMethod = chain.request().method();
            if ("GET".equalsIgnoreCase(httpMethod)) {
                builder.get().url(url);
            } else if ("POST".equalsIgnoreCase(httpMethod)) {
                RequestBody body = chain.request().body();
                if (body != null) {
                    builder.post(body).url(url);
                }
            }
            builder.addHeader("x-sign", appSign);
            request = builder.build();
            return chain.proceed(request);
        } else {
            return chain.proceed(request);
        }
    }


    public static String getAppXsign(LinkedHashMap<String, String> paramMap) {
        Set<String> keySet = paramMap.keySet();
        Object[] keys = keySet.toArray();
        //请求参数升序排序
        Arrays.sort(keys);
        StringBuilder stringBuilder = new StringBuilder();
        assert keys != null;
        for (Object str : keys) {
            stringBuilder.append(str.toString()).append("=");
            try {
                if ("address".equals(str)) {
                    String decode = URLDecoder.decode(paramMap.get(str.toString()), "UTF-8");
                    String replace = decode.replace(" ", "");
                    String encode = URLEncoder.encode(replace, "UTF-8");
                    stringBuilder.append(encode).append("&");
                } else {
                    stringBuilder.append(paramMap.get(str.toString())).append("&");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        stringBuilder.append("api_secret").append("=");
        stringBuilder.append("ab1286f9aeb811eaa61400163e06ad39");
        return Sha1Util.encryptToSHA(stringBuilder.toString());
    }
}

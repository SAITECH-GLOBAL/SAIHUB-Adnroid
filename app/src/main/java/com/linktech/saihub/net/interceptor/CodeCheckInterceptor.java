package com.linktech.saihub.net.interceptor;


import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.linktech.saihub.net.BaseResp;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * 对于全局错误代码进行拦截处理
 */
public class CodeCheckInterceptor implements Interceptor {
    private CodeCheckListener mCodeCheckListener;
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public CodeCheckInterceptor(CodeCheckListener codeCheckListener) {
        mCodeCheckListener = codeCheckListener;
    }


    public Response intercept(Response response, Chain chain) throws IOException {
        if (response == null || !isSuccess(response.code())) {
            if (mCodeCheckListener != null) {
                mCodeCheckListener.errorUpload(response.code(), response.message(), chain.request().url().toString(), chain.request().method());
            }
            return response;
        }

        ResponseBody body = response.body();
        if (body == null) {
            return response;
        }

        body.source().request(Long.MAX_VALUE);
        BaseResp data = null;
        try {
            Gson gson = new Gson();
            data = gson.fromJson(body.source().buffer().clone().readString(body.contentType() != null ? body.contentType().charset(UTF_8) : UTF_8),
                    BaseResp.class);
        } catch (Exception e) {
        }

        if (data == null) {
            return response;
        }
        if (data.getData() == null) {
            return response;
        }
        int status = data.getData().getStatus();
        if (status != 0) {
            if (mCodeCheckListener != null) {
                mCodeCheckListener.errorUpload(data.getData().getStatus(), data.getData().getMessage(), chain.request().url().toString(), chain.request().method());
            }
        }
        return response;
    }

    private boolean isSuccess(int code) {
        return (code >= 200 && code < 300) || code == 400 || code == 500;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        //交给子Interceptor
        Request request = chain.request();

        //执行子Interceptor处理过的
        Response response = chain.proceed(request);
        /*Response response = chain.proceed(request)
                .newBuilder()
                .removeHeader("pragma")
                .removeHeader("Cache-Control")
                .header("Cache-Control", CacheControl.FORCE_CACHE.toString())
                .build();*/
        //交给子Interceptor
        response = intercept(response, chain);

        return response;
    }

    public interface CodeCheckListener {

        void errorUpload(int status, String message, String url, String method);
    }
}
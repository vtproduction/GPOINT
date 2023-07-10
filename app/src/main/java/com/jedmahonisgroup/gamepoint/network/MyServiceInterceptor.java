package com.jedmahonisgroup.gamepoint.network;

import com.jedmahonisgroup.gamepoint.R;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
public class MyServiceInterceptor implements Interceptor {
    private String sessionToken;

    @Inject
    public MyServiceInterceptor() {
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        Request.Builder requestBuilder = request.newBuilder();

        if (request.header(String.valueOf(R.string.NO_AUTH_HEADER_KEY)) == null) {
            // needs credentials
            if (sessionToken == null) {
                throw new RuntimeException("Session token should be defined for auth apis");
            } else {
                requestBuilder.addHeader("Cookie", sessionToken);
            }
        }

        return chain.proceed(requestBuilder.build());
    }
}

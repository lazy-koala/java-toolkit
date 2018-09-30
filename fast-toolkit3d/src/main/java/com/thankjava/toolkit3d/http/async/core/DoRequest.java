package com.thankjava.toolkit3d.http.async.core;

import java.util.List;
import java.util.concurrent.Future;

import com.thankjava.toolkit3d.http.async.entity.ResponseCallback;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import com.thankjava.toolkit3d.http.async.core.utils.RequestBuilder;
import com.thankjava.toolkit3d.http.async.core.utils.ResponseBuilder;
import com.thankjava.toolkit3d.http.async.entity.AsyncRequest;
import com.thankjava.toolkit3d.http.async.entity.AsyncResponse;

public class DoRequest extends BasicRequest {

    private static DoRequest doRequest = new DoRequest();
    private static CloseableHttpAsyncClient closeableHttpAsyncClient;

    private DoRequest() {
    }

    public static DoRequest getInterface(CloseableHttpAsyncClient closeableHttpAsyncClient) {
        DoRequest.closeableHttpAsyncClient = closeableHttpAsyncClient;
        return doRequest;
    }

    public AsyncResponse doRequest(final AsyncRequest asyncRequest, boolean withSession, final ResponseCallback callback) {

        Future<HttpResponse> future;

        HttpClientContext requestCtx;

        if (withSession) {
            requestCtx = syncHttpClientContext;
        } else {
            requestCtx = HttpClientContext.create();
        }

        addCookies(asyncRequest, requestCtx);

        try {

            final HttpRequestBase request = RequestBuilder.builderRequest(asyncRequest);
            final HttpClientContext ctx = requestCtx;

            future = closeableHttpAsyncClient.execute(request, requestCtx, callback != null ? new FutureCallback<HttpResponse>() {

                @Override
                public void completed(HttpResponse httpResponse) {
                    callback.completed(ResponseBuilder.builder(httpResponse, asyncRequest.getResCharset(), ctx));
                }

                @Override
                public void failed(Exception e) {
                    callback.failed(e);
                }

                @Override
                public void cancelled() {
                    callback.cancelled();
                }

            } : null);

            if (callback == null) {
                return ResponseBuilder.builder(future.get(), asyncRequest.getResCharset(), requestCtx);
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    private void addCookies(AsyncRequest asyncRequest, HttpClientContext syncHttpClientContext) {
        if (asyncRequest.getCookies() != null) {
            List<Cookie> cookies = asyncRequest.getCookies().getAllCookies();
            for (Cookie cookie : cookies) {
                syncHttpClientContext.getCookieStore().addCookie(cookie);
            }
        }
    }
}

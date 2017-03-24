package rayco.androidlib.net.http;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.CertificatePinner;
import okhttp3.ConnectionPool;
import okhttp3.CookieJar;
import okhttp3.Dispatcher;
import okhttp3.Dns;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.internal.tls.OkHostnameVerifier;
import rayco.androidlib.net.http.callback.Callback;
import rayco.androidlib.net.http.request.RequestCall;
import rayco.androidlib.net.http.request.builder.GetBuilder;
import rayco.androidlib.net.http.request.builder.HeadBuilder;
import rayco.androidlib.net.http.request.builder.OtherBuilder;
import rayco.androidlib.net.http.request.builder.PostFileBuilder;
import rayco.androidlib.net.http.request.builder.PostFormBuilder;
import rayco.androidlib.net.http.request.builder.PostStringBuilder;
import rayco.androidlib.net.http.utils.Platform;

/**
 * Created by zhy on 15/8/17.
 */
public class Http {
    public static final long DEFAULT_MILLISECONDS = 10_000L;
    private volatile static Http instance;
    private OkHttpClient mOkHttpClient;
    private Platform mPlatform;

    private Http(Config config) {
        if (config == null) {
            mOkHttpClient = new OkHttpClient();
        } else {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(config.connectTimeout, config.connectTimeUnit);
            builder.readTimeout(config.readTimeout, config.readTimeUnit);
            builder.writeTimeout(config.writeTimeout, config.writeTimeUnit);
            builder.proxy(config.proxy);
            builder.proxySelector(config.proxySelector);
            builder.cookieJar(config.cookieJar);
            builder.cache(config.cache);
            builder.dns(config.dns);
            builder.socketFactory(config.socketFactory);
            builder.sslSocketFactory(config.sslSocketFactory, config.trustManager);
            builder.hostnameVerifier(config.hostnameVerifier);
            builder.certificatePinner(config.certificatePinner);
            builder.authenticator(config.authenticator);
            builder.proxyAuthenticator(config.proxyAuthenticator);
            builder.connectionPool(config.connectionPool);
            builder.followSslRedirects(config.followSslRedirects);
            builder.followRedirects(config.followRedirects);
            builder.retryOnConnectionFailure(config.retryOnConnectionFailure);
            builder.dispatcher(config.dispatcher);
            for (Interceptor interceptor : config.interceptors) {
                builder.addInterceptor(interceptor);
            }
            for (Interceptor interceptor : config.networkInterceptors) {
                builder.addNetworkInterceptor(interceptor);
            }
            mOkHttpClient = builder.build();
        }
        mPlatform = Platform.get();
    }

    public static Http init(Http.Config config) {
        if (instance == null) {
            synchronized (Http.class) {
                if (instance == null) {
                    instance = new Http(config);
                }
            }
        }
        return instance;
    }

    public static Http getInstance() {
        return init(null);
    }

    public static GetBuilder get() {
        return new GetBuilder();
    }

    public static PostStringBuilder postString() {
        return new PostStringBuilder();
    }

    public static PostFileBuilder postFile() {
        return new PostFileBuilder();
    }

    public static PostFormBuilder post() {
        return new PostFormBuilder();
    }

    public static OtherBuilder put() {
        return new OtherBuilder(METHOD.PUT);
    }

    public static HeadBuilder head() {
        return new HeadBuilder();
    }

    public static OtherBuilder delete() {
        return new OtherBuilder(METHOD.DELETE);
    }

    public static OtherBuilder patch() {
        return new OtherBuilder(METHOD.PATCH);
    }

    public Executor getDelivery() {
        return mPlatform.defaultCallbackExecutor();
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public void execute(final RequestCall requestCall, Callback callback) {
        if (callback == null) {
            callback = Callback.CALLBACK_DEFAULT;
        }
        final Callback finalCallback = callback;
        final int id = requestCall.getOkHttpRequest().getId();

        requestCall.getCall().enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                sendFailResultCallback(call, e, finalCallback, id);
            }

            @Override
            public void onResponse(final Call call, final Response response) {
                try {
                    if (call.isCanceled()) {
                        sendFailResultCallback(call, new IOException("Canceled!"), finalCallback, id);
                        return;
                    }

                    if (!finalCallback.validateResponse(response, id)) {
                        sendFailResultCallback(call, new IOException("request failed , reponse's code is : " + response.code()), finalCallback, id);
                        return;
                    }

                    Object o = finalCallback.parseNetworkResponse(response, id);
                    sendSuccessResultCallback(o, finalCallback, id);
                } catch (Exception e) {
                    sendFailResultCallback(call, e, finalCallback, id);
                } finally {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }
            }
        });
    }

    public void cancelTag(Object tag) {
        for (Call call : mOkHttpClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : mOkHttpClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    private void sendFailResultCallback(final Call call, final Exception e, final Callback callback, final int id) {
        if (callback == null) return;

        mPlatform.execute(new Runnable() {
            @Override
            public void run() {
                callback.onError(call, e, id);
                callback.onAfter(id);
            }
        });
    }

    private void sendSuccessResultCallback(final Object object, final Callback callback, final int id) {
        if (callback == null) return;
        mPlatform.execute(new Runnable() {
            @Override
            public void run() {
                callback.onResponse(object, id);
                callback.onAfter(id);
            }
        });
    }

    public static class METHOD {
        public static final String HEAD = "HEAD";
        public static final String DELETE = "DELETE";
        public static final String PUT = "PUT";
        public static final String PATCH = "PATCH";
    }

    public static final class Config {
        Dispatcher dispatcher;
        Proxy proxy;
        final List<Interceptor> interceptors = new ArrayList<>();
        final List<Interceptor> networkInterceptors = new ArrayList<>();
        ProxySelector proxySelector;
        CookieJar cookieJar;
        Cache cache;
        SocketFactory socketFactory;
        SSLSocketFactory sslSocketFactory;
        X509TrustManager trustManager;
        HostnameVerifier hostnameVerifier;
        CertificatePinner certificatePinner;
        Authenticator proxyAuthenticator;
        Authenticator authenticator;
        ConnectionPool connectionPool;
        Dns dns;
        boolean followSslRedirects;
        boolean followRedirects;
        boolean retryOnConnectionFailure;
        long connectTimeout;
        TimeUnit connectTimeUnit;
        long readTimeout;
        TimeUnit readTimeUnit;
        long writeTimeout;
        TimeUnit writeTimeUnit;

        public Config() {
            dispatcher = new Dispatcher();
            proxySelector = ProxySelector.getDefault();
            cookieJar = CookieJar.NO_COOKIES;
            socketFactory = SocketFactory.getDefault();
            hostnameVerifier = OkHostnameVerifier.INSTANCE;
            certificatePinner = CertificatePinner.DEFAULT;
            proxyAuthenticator = Authenticator.NONE;
            authenticator = Authenticator.NONE;
            connectionPool = new ConnectionPool();
            dns = Dns.SYSTEM;
            followSslRedirects = true;
            followRedirects = true;
            retryOnConnectionFailure = true;
            connectTimeout = 10000L;
            connectTimeUnit = TimeUnit.MILLISECONDS;
            readTimeout = 10000L;
            readTimeUnit = TimeUnit.MILLISECONDS;
            writeTimeout = 10000L;
            writeTimeUnit = TimeUnit.MILLISECONDS;
        }

        public Http.Config connectTimeout(long timeout, TimeUnit unit) {
            connectTimeout = timeout;
            connectTimeUnit = unit;
            return this;
        }

        public Http.Config readTimeout(long timeout, TimeUnit unit) {
            readTimeout = timeout;
            readTimeUnit = unit;
            return this;
        }

        public Http.Config writeTimeout(long timeout, TimeUnit unit) {
            writeTimeout = timeout;
            writeTimeUnit = unit;
            return this;
        }

        public Http.Config proxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        public Http.Config proxySelector(ProxySelector proxySelector) {
            this.proxySelector = proxySelector;
            return this;
        }

        public Http.Config cookieJar(CookieJar cookieJar) {
            if (cookieJar == null) {
                throw new NullPointerException("cookieJar == null");
            }
            this.cookieJar = cookieJar;
            return this;
        }

        public Http.Config cache(Cache cache) {
            this.cache = cache;
            return this;
        }

        public Http.Config dns(Dns dns) {
            if (dns == null) {
                throw new NullPointerException("dns == null");
            }
            this.dns = dns;
            return this;
        }

        public Http.Config socketFactory(SocketFactory socketFactory) {
            if (socketFactory == null) {
                throw new NullPointerException("socketFactory == null");
            }
            this.socketFactory = socketFactory;
            return this;
        }

        public Http.Config sslSocketFactory(SSLSocketFactory sslSocketFactory) {
            if (sslSocketFactory == null) {
                throw new NullPointerException("sslSocketFactory == null");
            }
            this.sslSocketFactory = sslSocketFactory;
            this.trustManager = okhttp3.internal.platform.Platform.get().trustManager(sslSocketFactory);
            return this;
        }

        public Http.Config sslSocketFactory(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
            if (sslSocketFactory == null) {
                throw new NullPointerException("sslSocketFactory == null");
            }
            if (trustManager == null) {
                throw new NullPointerException("trustManager == null");
            }
            this.sslSocketFactory = sslSocketFactory;
            this.trustManager = trustManager;
            return this;
        }

        public Http.Config hostnameVerifier(HostnameVerifier hostnameVerifier) {
            if (hostnameVerifier == null) {
                throw new NullPointerException("hostnameVerifier == null");
            }
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        public Http.Config certificatePinner(CertificatePinner certificatePinner) {
            if (certificatePinner == null) {
                throw new NullPointerException("certificatePinner == null");
            }
            this.certificatePinner = certificatePinner;
            return this;
        }

        public Http.Config authenticator(Authenticator authenticator) {
            if (authenticator == null) {
                throw new NullPointerException("authenticator == null");
            }
            this.authenticator = authenticator;
            return this;
        }

        public Http.Config proxyAuthenticator(Authenticator proxyAuthenticator) {
            if (proxyAuthenticator == null) {
                throw new NullPointerException("proxyAuthenticator == null");
            }
            this.proxyAuthenticator = proxyAuthenticator;
            return this;
        }

        public Http.Config connectionPool(ConnectionPool connectionPool) {
            if (connectionPool == null) {
                throw new NullPointerException("connectionPool == null");
            }
            this.connectionPool = connectionPool;
            return this;
        }

        public Http.Config followSslRedirects(boolean followProtocolRedirects) {
            this.followSslRedirects = followProtocolRedirects;
            return this;
        }

        public Http.Config followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        public Http.Config retryOnConnectionFailure(boolean retryOnConnectionFailure) {
            this.retryOnConnectionFailure = retryOnConnectionFailure;
            return this;
        }

        public Http.Config dispatcher(Dispatcher dispatcher) {
            if (dispatcher == null) {
                throw new IllegalArgumentException("dispatcher == null");
            }
            this.dispatcher = dispatcher;
            return this;
        }

        public Http.Config addInterceptor(Interceptor interceptor) {
            interceptors.add(interceptor);
            return this;
        }

        public Http.Config addNetworkInterceptor(Interceptor interceptor) {
            networkInterceptors.add(interceptor);
            return this;
        }

        public Http build() {
            return new Http(this);
        }
    }
}


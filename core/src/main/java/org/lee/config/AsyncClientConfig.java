package org.lee.config;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsyncClientConfig {

    @Bean("asyncClient")
    public AsyncHttpClient getClient() {

        DefaultAsyncHttpClientConfig.Builder builder = Dsl.config();
        builder.setFollowRedirect(true)
                .setUseInsecureTrustManager(true)
                .setTcpNoDelay(true)
                .setThreadPoolName("WECHAT_BOT")
                .setIoThreadsCount(8)
                .setMaxRequestRetry(2)
                .setPooledConnectionIdleTimeout(10 * 60 * 1000)
                .setConnectTimeout(5 * 1000)
                .setReadTimeout(30 * 1000)
                .setRequestTimeout(30 * 1000)
                .setCookieStore(null);

        return Dsl.asyncHttpClient(builder.build());
    }
}

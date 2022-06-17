package org.lee;


import io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueue;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.lee.listener.DispatchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class WechatBotApplication {

    public static final Logger LOGGER = LoggerFactory.getLogger(WechatBotApplication.class);

    public static void main(String[] args) throws Exception {

        SpringApplication application = new SpringApplication(WechatBotApplication.class);
        ApplicationContext context = application.run(args);

        AsyncHttpClient asyncHttpClient = context.getBean(AsyncHttpClient.class);

        MpscArrayQueue<String> queue = new MpscArrayQueue<>(4096 * 2);

        WebSocket websocket = asyncHttpClient.prepareGet("ws://127.0.0.1:5555")
                .execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket websocket) {
                        LOGGER.info("connect to server SUCCESS !");
                        websocket.removeWebSocketListener(this);
                        websocket.addWebSocketListener(new DispatchListener(queue));
                    }

                    @Override
                    public void onClose(WebSocket websocket, int code, String reason) {
                        throw new IllegalStateException("ERROR !");
                    }

                    @Override
                    public void onError(Throwable t) {
                        throw new IllegalStateException("ERROR !");
                    }
                }).build()).get();

        Worker worker = new Worker(queue);
        worker.init();

    }


}

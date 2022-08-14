package org.lee;


import io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueue;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.lee.listener.DispatchListener;
import org.lee.pojo.MsgInfo;
import org.lee.util.DatDecoder;
import org.lee.util.GcTasker;
import org.lee.util.QrCodeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ConcurrentMap;

@SpringBootApplication
public class WechatBotApplication {

    public static final Logger LOGGER = LoggerFactory.getLogger(WechatBotApplication.class);

    public static void main(String[] args) throws Exception {

        SpringApplication application = new SpringApplication(WechatBotApplication.class);
        ApplicationContext context = application.run(args);

        AsyncHttpClient asyncHttpClient = context.getBean(AsyncHttpClient.class);
        String wsUrl = context.getEnvironment().getProperty("ws_url", "ws://127.0.0.1:5555");
        String localPrefix = context.getEnvironment().getProperty("local_path_prefix");
        String outPrefix = context.getEnvironment().getProperty("out_path_prefix");
        String wehatQrcodePath = context.getEnvironment().getProperty("qr_code_model_path");

        MpscArrayQueue<String> queue = new MpscArrayQueue<>(4096 * 2);

        final DispatchListener dispatchListener = new DispatchListener();

        ConcurrentMap<String, MpscArrayQueue<MsgInfo>> queueMap = dispatchListener.getQueueMap();

        WebSocket websocket = asyncHttpClient.prepareGet(wsUrl)
                .execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket websocket) {
                        LOGGER.info("connect to server SUCCESS !");
                        dispatchListener.onOpen(websocket);
                        websocket.removeWebSocketListener(this);
                        websocket.addWebSocketListener(dispatchListener);
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

        /**
         * 金沙店
         * 25542060980@chatroom
         *
         * 郑州长江 优选
         * 24843817163@chatroom
         *
         * 衡山店
         * 23521522677@chatroom
         *
         * 偷袭的店
         */
        MpscArrayQueue<MsgInfo> quick = new MpscArrayQueue<>(512);
        queueMap.put("25542060980@chatroom", quick);
        queueMap.put("24843817163@chatroom", quick);
        queueMap.put("23521522677@chatroom", quick);

        DatDecoder datDecoder = new DatDecoder(localPrefix, outPrefix);
        QrCodeParser qrCodeParser = new QrCodeParser(wehatQrcodePath);
        Worker worker = new Worker(quick, datDecoder, qrCodeParser, websocket);
        worker.init();

        /**
         * 环城店
         */
        MpscArrayQueue<MsgInfo> queue4hc = new MpscArrayQueue<>(512);
        queueMap.put("25419510637@chatroom", queue4hc); //58
        queueMap.put("23860221908@chatroom", queue4hc); //57
        queueMap.put("23487615032@chatroom", queue4hc); //48
        queueMap.put("23541412562@chatroom", queue4hc); //38
        queueMap.put("25009327441@chatroom", queue4hc); //68

        datDecoder = new DatDecoder(localPrefix, outPrefix);
        qrCodeParser = new QrCodeParser(wehatQrcodePath);
        worker = new Worker(queue4hc, datDecoder, qrCodeParser, websocket);
        worker.init();

        /**
         * 能有问卷星的店
         *
         * 杭州五常店
         * 24235983087@chatroom
         *
         * 郑州华南城
         * 22167507540@chatroom
         *
         * 北京赛特
         * 22765417328@chatroom
         *
         * 隆华
         * 22910332784@chatroom
         *
         */
        MpscArrayQueue<MsgInfo> queueWjx = new MpscArrayQueue<>(512);
        queueMap.put("24235983087@chatroom", queueWjx);
        queueMap.put("22167507540@chatroom", queueWjx);
        queueMap.put("22765417328@chatroom", queueWjx);
        queueMap.put("22910332784@chatroom", queueWjx);

        datDecoder = new DatDecoder(localPrefix, outPrefix);
        qrCodeParser = new QrCodeParser(wehatQrcodePath);
        worker = new Worker(queueWjx, datDecoder, qrCodeParser, websocket);
        worker.init();

        datDecoder = new DatDecoder(localPrefix, outPrefix);
        qrCodeParser = new QrCodeParser(wehatQrcodePath);
        worker = new Worker(dispatchListener.getDefaultQueue(), datDecoder, qrCodeParser, websocket);
        worker.init();

        GcTasker gcTasker = new GcTasker();
        gcTasker.init();

    }


}

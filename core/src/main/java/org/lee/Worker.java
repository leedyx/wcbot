package org.lee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueue;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.ws.WebSocket;
import org.lee.pojo.MsgInfo;
import org.lee.pojo.SendMsgInfo;
import org.lee.util.DatDecoder;
import org.lee.util.Dates;
import org.lee.util.Jsons;
import org.lee.util.QrCodeParser;
import org.lee.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Worker implements Runnable {

    private final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);

    private static final Logger LOGGER = LoggerFactory.getLogger(Worker.class);

    private final MpscArrayQueue<MsgInfo> queue;

    private final DatDecoder datDecoder;

    private final QrCodeParser qrCodeParser;

    private final WebSocket webSocket;

    public Worker(MpscArrayQueue<MsgInfo> queue, DatDecoder datDecoder, QrCodeParser qrCodeParser, WebSocket webSocket) {
        this.queue = queue;
        this.datDecoder = datDecoder;
        this.qrCodeParser = qrCodeParser;
        this.webSocket = webSocket;
    }

    public void init() {
        EXECUTOR_SERVICE.submit(this);
    }

    private void sendPic(String path) {
        try {
            String id = Dates.format(System.currentTimeMillis(), "yyyyMMddHHmmss");
            SendMsgInfo sendMsgInfo = SendMsgInfo.builder().content(path).id(id).wxid("wxid_ef5bjxzlk5l322").type(500).build();
            webSocket.sendTextFrame(Jsons.toJson(sendMsgInfo));
        } catch (Exception e) {
            LOGGER.info("sendMsg error !", e);
        }
    }

    private void sendTxt(String content) {
        try {
            String id = Dates.format(System.currentTimeMillis(), "yyyyMMddHHmmss");
            SendMsgInfo sendMsgInfo = SendMsgInfo.builder().content(content).id(id).wxid("wxid_ef5bjxzlk5l322").type(555).build();
            webSocket.sendTextFrame(Jsons.toJson(sendMsgInfo));
        } catch (Exception e) {
            LOGGER.info("sendMsg error !", e);
        }
    }

    @Override
    public void run() {

        for (; ; ) {
            try {
                if (queue.size() <= 0) {
                    Utils.safeSleep(TimeUnit.MILLISECONDS, 100);
                } else {
                    MsgInfo msgInfo = queue.poll();
                    if (msgInfo.getType() == 3) {
                        String imgPath = datDecoder.tryDecodeDat(msgInfo.getContent());
                        List<String> urls = qrCodeParser.tryParseUrls(imgPath);
                        if (Objects.nonNull(urls) && urls.size() > 0) {
                            sendPic(imgPath);
                            urls.forEach(url -> {
                                url = StringUtils.trim(url);
                                LOGGER.info("the url : {}", url);

                                String from = "D:\\local\\nike_rush\\default.yaml";
                                String to = "D:\\local\\nike_rush\\config\\application.yaml";

                                try {

                                    Path path = Paths.get(from);

                                    byte[] bytes = Files.readAllBytes(path);

                                    String content = new String(bytes);
                                    String ans = StringUtils.replace(content, "{url}", url);
                                    Files.write(Paths.get(to), ans.getBytes(StandardCharsets.UTF_8));
                                    int start = url.lastIndexOf("/");
                                    int end = url.lastIndexOf(".");

                                    if (start == -1 || end == -1) {
                                        LOGGER.error("url error !");
                                    }

                                } catch (Exception e) {
                                    LOGGER.error("get short id error !", e);
                                }

                                sendTxt(url);

                            });

                        }
                    } else if (msgInfo.getType() == 1) {
                        LOGGER.info("txt message : {}", msgInfo.getContent());
                    }
                }

            } catch (Exception e) {
                LOGGER.error("error !", e);
            }
        }

    }
}

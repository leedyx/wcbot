package org.lee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueue;
import org.apache.commons.lang3.StringUtils;
import org.lee.pojo.PicMsg;
import org.lee.pojo.PicMsgDetail;
import org.lee.util.Parsers;
import org.lee.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Worker implements Runnable {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);

    private static final Logger LOGGER = LoggerFactory.getLogger(Worker.class);

    private final MpscArrayQueue<String> queue;

    public Worker(MpscArrayQueue<String> queue) {
        this.queue = queue;
    }

    public void init() {
        EXECUTOR_SERVICE.submit(this);
    }

    private ObjectReader reader = new ObjectMapper().reader();

    @Override
    public void run() {

        for (; ; ) {
            try {
                if (queue.size() <= 0) {
                    Utils.safeSleep(TimeUnit.MILLISECONDS, 100);
                } else {
                    String message = queue.poll();

                    if (StringUtils.contains(message, "<img aeskey=")) {
                        PicMsg res = reader.readValue(message, PicMsg.class);

                        if (Objects.nonNull(res.getPicMsgDetail()) && StringUtils.isNotBlank(res.getPicMsgDetail().getDetail())) {

                            String detail = res.getPicMsgDetail().getDetail();
                            List<String> urls = Parsers.tryGetUrl(detail);
                            if (Objects.nonNull(urls) && urls.size() > 0) {
                                LOGGER.info("urls : {}", urls);
                            }
                        }
                    }

                    LOGGER.info("message : {}", message);
                }
            } catch (Exception e) {
                LOGGER.error("error !", e);
            }
        }

    }
}

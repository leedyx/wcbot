package org.lee.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GcTasker implements Runnable {

    public static final Logger LOGGER = LoggerFactory.getLogger(GcTasker.class);

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);

    @Override
    public void run() {
        for (; ; ) {
            try {
                System.gc();
                LOGGER.info("============== GC  HERE================");
            } catch (Exception e) {
                LOGGER.error("gc task error !", e);
            } finally {
                Utils.safeSleep(TimeUnit.SECONDS, 10 * 60);
            }
        }
    }

    public void init() {
        EXECUTOR_SERVICE.submit(this);
    }
}

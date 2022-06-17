package org.lee.util;

import java.util.concurrent.TimeUnit;

public class Utils {

    public static void safeSleep(TimeUnit timeUnit, int period) {
        try {
            timeUnit.sleep(period);
        } catch (Exception ignore) {
        }
    }

    public static void awaitUntil(long timestamp, int interval) {
        long now = System.currentTimeMillis();
        while (now < timestamp) {
            safeSleep(TimeUnit.MILLISECONDS, interval);
            now = System.currentTimeMillis();
        }
    }
}

package org.lee.util;

import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Dates {

    private static final ConcurrentMap<String, DateTimeFormatter> map = new ConcurrentHashMap<>(8);

    static {
        map.put("yyyyMMddHHmmss", DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private Dates() {
    }

    public static long parse(String dateTime, String formatter) {
        if (StringUtils.isBlank(dateTime) || StringUtils.isBlank(formatter)) {
            throw new IllegalArgumentException("dateTime or formatter is null !");
        }

        DateTimeFormatter dateTimeFormatter = map.get(formatter);
        if (Objects.isNull(dateTimeFormatter)) {
            throw new IllegalStateException("no formatter !");
        }

        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, dateTimeFormatter);
        return localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }

    public static String format(long timestamp, String formatter) {
        if (timestamp == 0L || StringUtils.isBlank(formatter)) {
            throw new IllegalArgumentException("timestamp or formatter is null !");
        }

        DateTimeFormatter dateTimeFormatter = map.get(formatter);
        if (Objects.isNull(dateTimeFormatter)) {
            throw new IllegalStateException("no formatter !");
        }

        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime.format(dateTimeFormatter);
    }
}

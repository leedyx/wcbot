package org.lee.util;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.Objects;

public class Jsons {

    private static ObjectWriter WRITER;

    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(String.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
        WRITER = mapper.writer();
    }

    private Jsons() {

    }

    public static <T> String toJson(T value) {
        if (Objects.isNull(value)) {
            return "null";
        }

        try {
            return WRITER.writeValueAsString(value);
        } catch (Exception e) {
            return value.toString();
        }
    }
}
package com.nipungupta.helloworld;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by Nipun Gupta on 4/16/2016.
 */
public class JsonUtil {

    public static final JsonUtil.JsonUtilConfigurable instance;
    static {
        ObjectMapper mapper = new ObjectMapper();
        instance = new JsonUtil.JsonUtilConfigurable(mapper);
    }

    public static class JsonUtilConfigurable {
        public final ObjectMapper mapper;

        public JsonUtilConfigurable(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        public <E> E fromJson(String json, Class<E> type) {
            if (json == null || "".equals(json.trim())
                    || "\"\"".equals(json.trim())) {
                return null;
            }
            try {
                return mapper.readValue(json, type);
            } catch (IOException e) {
                System.out.println("error converting from json=" + json + e);
            }
            return null;
        }

        public String toJson(Object object) {
            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            } catch (IOException e) {
                System.out.println("error converting to json" + e);
            }
            return null;
        }
    }

    public static <E> E fromJson(String json, Class<E> type) {
        return instance.fromJson(json, type);
    }

    public static String toJson(Object object) {
        return instance.toJson(object);
    }
}

package com.nipungupta.helloworld;

import com.jayway.jsonpath.DocumentContext;

import java.util.List;
import java.util.Map;

/**
 * Created by Nipun Gupta on 4/16/2016.
 */
public class JsonPath {

    private String jsonString;
    private DocumentContext ctx;

    public JsonPath(String jsonString) {
        super();
        this.jsonString = jsonString;
        this.ctx = com.jayway.jsonpath.JsonPath.parse(this.jsonString);
    }

    public JsonPath(Map<?, ?> map) {
        this.jsonString = JsonUtil.toJson(map);
        this.ctx = com.jayway.jsonpath.JsonPath.parse(this.jsonString);
    }

    public JsonPath(List<?> list) {
        this.jsonString = JsonUtil.toJson(list);
        this.ctx = com.jayway.jsonpath.JsonPath.parse(this.jsonString);
    }

    public <T> T read(String path) {
        return this.ctx.read(path);
    }

    public <T> T readNotNull(String path) throws IllegalArgumentException {
        if (path==null || "".equals(path)) {
            throw new IllegalArgumentException("Null String");
        }
        return this.ctx.read(path);
    }

    public void put(String path, String key, Object value) {
        this.ctx = this.ctx.put(path, key, value);
    }

}

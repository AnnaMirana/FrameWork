package com.mhframework.utils;

import java.lang.reflect.Array;
import java.util.Collection;

public class JsonResult {
    private String status = "success";
    private int code = 200;
    private int length = 0;
    private Object data = "";

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Object getData() {
        return data;
    }

    @SuppressWarnings("rawtypes")
    public void setData(Object data) {
        if (data instanceof Collection) {
            setLength(((Collection) data).size());
        } else if (data.getClass().isArray()) {
            setLength(Array.getLength(data));
        }
        this.data = data;
    }

}

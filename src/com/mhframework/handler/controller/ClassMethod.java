package com.mhframework.handler.controller;

import java.lang.reflect.Method;
import java.util.regex.Matcher;

public class ClassMethod {

    private Class<?> cls;
    private Method method;
    private String url;
    private Matcher matcher = null;

    public ClassMethod(Class<?> cls, Method method, String url) {
        this.cls = cls;
        this.method = method;
        this.url = url;
    }

    public Class<?> getCls() {
        return cls;
    }

    public void setCls(Class<?> cls) {
        this.cls = cls;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

}

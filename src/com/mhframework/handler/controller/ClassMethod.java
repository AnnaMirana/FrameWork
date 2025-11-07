package com.mhframework.handler.controller;

import java.lang.reflect.Method;

public class ClassMethod {

    private Class<?> cls;
    private Method method;

    public ClassMethod(Class<?> cls, Method method) {
        this.cls = cls;
        this.method = method;
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

}

package com.mhframework.handler.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mhframework.annotation.UrlMapping;
import com.mhframework.utils.PackageScanner;

public class UrlHandler {

    public ClassMethod getByUrl(HashMap<String, ClassMethod> map, String url) {
        return map.get(url);
    }

    public HashMap<String, ClassMethod> urlMapping() {

        HashMap<String, ClassMethod> map = new HashMap<>();

        PackageScanner packageScanner = new PackageScanner(null);
        List<Class<?>> clsController = packageScanner.clsController();

        for (Class<?> cls : clsController) {
            List<Method> methods = methodeAnnoteByClass(cls, UrlMapping.class);

            for (Method method : methods) {
                UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                map.put(urlMapping.value(), new ClassMethod(cls, method));
            }

        }
        return map;
    }


    public static List<Method> methodeAnnoteByClass(Class<?> cls, Class<? extends Annotation> clsAnnotation) {
        List<Method> methods = new ArrayList<>();

        Method[] allMethode = cls.getMethods();

        for (Method method : allMethode) {
            if (method.getAnnotation(clsAnnotation) != null) {
                methods.add(method);
            }
        }
        return methods;
    }
}

package com.mhframework.handler.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import com.mhframework.annotation.UrlMapping;
import com.mhframework.utils.PackageScanner;

import jakarta.servlet.http.HttpServletRequest;

public class UrlHandler {

    public Object invokeMethodeUrl(HttpServletRequest request, ClassMethod classMethod) throws Exception {
        Object instance = classMethod.getCls().getConstructor().newInstance();
        Method method = classMethod.getMethod();

        Parameter[] parameters = method.getParameters();

        if (parameters.length == 0) {
            return method.invoke(instance);
        }

        Object[] valuesParam = new Object[parameters.length];
        Enumeration<String> nameParam = request.getParameterNames();

        while (nameParam.hasMoreElements()) {
            String name = nameParam.nextElement();
            for (int a = 0; a < parameters.length; a++) {
                if (parameters[a].getName().equals(name)) {
                    System.out.println("Nom de parametre dans req : " + name + ", Nom de parametree dans Parameters : " + parameters[a].getName());
                    valuesParam[a] = valueObjectByType(request.getParameter(name), parameters[a].getType());
                    break;
                }
            }
        }

        return method.invoke(instance, valuesParam);
    }

    private Object valueObjectByType(Object obj, Class<?> cls) throws Exception {
        if (cls.equals(Integer.class) || cls.equals(int.class)) {
            return Integer.valueOf((String) obj);
        } else if (cls.equals(Double.class) || cls.equals(double.class)) {
            return Double.valueOf((String) obj);
        } else if (cls.equals(Float.class) || cls.equals(float.class)) {
            return Float.valueOf((String) obj);
        }
        return (String) obj;
    }

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

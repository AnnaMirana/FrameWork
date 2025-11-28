package com.mhframework.handler.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mhframework.annotation.ParamRequest;
import com.mhframework.annotation.UrlMapping;
import com.mhframework.utils.PackageScanner;

import jakarta.servlet.http.HttpServletRequest;

public class UrlHandler {

    Pattern pattern = null;

    public Object invokeMethodeUrl(HttpServletRequest request, ClassMethod classMethod) throws Exception {
        Object instance = classMethod.getCls().getConstructor().newInstance();
        Method method = classMethod.getMethod();

        Parameter[] parameters = method.getParameters();

        System.out.println(parameters.length + " " + method.getName());

        if (parameters.length == 0) {
            return method.invoke(instance);
        }

        Object[] valuesParam = initTableau(parameters.length);
        Enumeration<String> nameParam = request.getParameterNames();

        try {
            if (!nameParam.hasMoreElements()) {
                for (int a = 0; a < valuesParam.length; a++) {
                    valuesParam[a] = valueObjectByType(null, parameters[a].getType());
                }
            }

            while (nameParam.hasMoreElements()) {
                String name = nameParam.nextElement();
                for (int a = 0; a < parameters.length; a++) {

                    ParamRequest paramRequest = parameters[a].getAnnotation(ParamRequest.class);

                    if (parameters[a].getName().equals(name)) {
                        System.out.println(
                                "Nom de parametre dans req : " + name + ", Nom de parametree dans Parameters : "
                                        + parameters[a].getName());
                        valuesParam[a] = valueObjectByType(request.getParameter(name), parameters[a].getType());
                    } else if (paramRequest != null && paramRequest.value().equals(name)) {
                        String nomVar = paramRequest.value();
                        System.out
                                .println("C'est dans une annotation , reqName : " + name + " , ParamName : " + nomVar);
                        valuesParam[a] = valueObjectByType(request.getParameter(name), parameters[a].getType());
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }

        return method.invoke(instance, valuesParam);
    }

    private Object[] initTableau(int length) {
        Object[] taObjects = new Object[length];
        for (int a = 0; a < length; a++) {
            taObjects[a] = null;
        }
        return taObjects;
    }

    private Object valueObjectByType(Object obj, Class<?> cls) throws Exception {

        System.out.println(obj + " " + cls.getTypeName());

        if (obj == null) {
            if (cls.equals(Integer.class) || cls.equals(int.class) ||
                    cls.equals(Double.class) || cls.equals(double.class) ||
                    cls.equals(Float.class) || cls.equals(float.class)) {
                return 0;
            }
            return null;
        }

        if (cls.equals(Integer.class) || cls.equals(int.class)) {
            return Integer.valueOf((String) obj);
        } else if (cls.equals(Double.class) || cls.equals(double.class)) {
            return Double.valueOf((String) obj);
        } else if (cls.equals(Float.class) || cls.equals(float.class)) {
            return Float.valueOf((String) obj);
        }
        return (String) obj;
    }

    private String urlToRegex(String url) {
        String regex = url.replaceAll("\\.", "\\\\.");
        regex = regex.replaceAll("\\{([^/]+?)\\}", "(?<$1>[^/]+)");
        return regex;
    }

    public ClassMethod getByUrl(HashMap<String, ClassMethod> map, String url) {

        for (String urlMap : map.keySet()) {
            if (urlMap.equals(url)) {
                return map.get(urlMap);
            }

            String urlRegex = urlToRegex(urlMap);

            pattern = Pattern.compile(urlRegex);
            Matcher matcher = pattern.matcher(url);

            if (matcher.matches()) {
                System.out.println("Mi Match leh izi !!");
                System.out.println(urlRegex);
                ClassMethod classMethod = map.get(urlMap);
                classMethod.setMatcher(matcher);

                return classMethod;
            }
        }
        return null;
    }

    public HashMap<String, ClassMethod> urlMapping() {
        HashMap<String, ClassMethod> map = new HashMap<>();

        PackageScanner packageScanner = new PackageScanner(null);
        List<Class<?>> clsController = packageScanner.clsController();

        for (Class<?> cls : clsController) {
            List<Method> methods = methodeAnnoteByClass(cls, UrlMapping.class);

            for (Method method : methods) {
                UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                map.put(urlMapping.value(), new ClassMethod(cls, method, urlMapping.value()));
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

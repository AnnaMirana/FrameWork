package com.mhframework.handler.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mhframework.annotation.ParamRequest;
import com.mhframework.annotation.method.GetMapping;
import com.mhframework.annotation.method.PostMapping;
import com.mhframework.utils.PackageScanner;
import com.mhframework.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;

public class UrlHandler {

    private Pattern pattern = null;
    private String[] listMethode = new String[] {"GET", "POST"};

    @SuppressWarnings("unchecked")
    private Class<? extends Annotation>[] listAnnotation = new Class[] {GetMapping.class, PostMapping.class};


        private boolean isTypeObject(Class<?> cls) {
            if (cls.equals(Integer.class) || cls.equals(int.class)) {
                return false;
            } else if (cls.equals(Double.class) || cls.equals(double.class)) {
                return false;
            } else if (cls.equals(Float.class) || cls.equals(float.class)) {
                return false;
            } else if (cls.equals(String.class)) {
                return false;
            }

            return true;
        }

    public Object invokeMethodeUrl(HttpServletRequest request, ClassMethod classMethod) throws Exception {
        Object instance = classMethod.getCls().getConstructor().newInstance();
        Method method = classMethod.getMethod();

        Parameter[] parameters = method.getParameters();

        System.out.println(parameters.length + " " + method.getName());

        if (parameters.length == 0) {
            System.out.println("oadray ary izi tato ah");
            return method.invoke(instance);
        }

        Object[] valuesParam = initTableau(parameters.length);
        Enumeration<String> nameParam = request.getParameterNames();

        try {
            if (!nameParam.hasMoreElements()) {
                System.out.println("tena ato ve zany");
                Matcher matcher = classMethod.getMatcher();
                for (int a = 0; a < valuesParam.length; a++) {
                    String value = matcher.group(parameters[a].getName());
                    if (value == null) {
                        throw new Exception("Le param " + parameters[a].getName() + " doit avoir du valeur car null est trouvé");
                    }
                    valuesParam[a] = valueObjectByType(value, parameters[a], request);
                }
            }


            while (nameParam.hasMoreElements()) {

                String name = nameParam.nextElement();
                for (int a = 0; a < parameters.length; a++) {

                    System.out.println(parameters[a].getName());

                    ParamRequest paramRequest = parameters[a].getAnnotation(ParamRequest.class);

                    if (isTypeObject(parameters[a].getType())) {
                        valuesParam[a] = valueOfTypeObject(parameters[a].getType(), request, null);
                    } else if (parameters[a].getName().equals(name)) {
                        System.out.println(
                                "Nom de parametre dans req : " + name + ", Nom de parametree dans Parameters : "
                                        + parameters[a].getName());
                        valuesParam[a] = valueObjectByType(request.getParameter(name), parameters[a], request);
                    } else if (paramRequest != null && paramRequest.value().equals(name)) {
                        String nomVar = paramRequest.value();
                        System.out
                                .println("C'est dans une annotation , reqName : " + name + " , ParamName : " + nomVar);
                        valuesParam[a] = valueObjectByType(request.getParameter(name), parameters[a], request);
                    } else if (Map.class.isAssignableFrom(parameters[a].getType())) {
                        valuesParam[a] = valueObjectByType(request.getParameter(name), parameters[a], request);
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

        private boolean isMapStringObject(Parameter parameter) throws Exception {

            if (!Map.class.isAssignableFrom(parameter.getType())) return false;

            Type type = parameter.getParameterizedType();

            if (!(type instanceof ParameterizedType)) return false;

            ParameterizedType pType = (ParameterizedType) type;

            Type[] args = pType.getActualTypeArguments();

            if (args.length != 2) return false;

            return args[0] == String.class && args[1] == Object.class;

        }       

                private Method getMethodByField(Field field, Class<?> cls) throws Exception {
                    System.out.println(field.getName());
                    return cls.getMethod("set" + Utils.capitalize(field.getName()), field.getType());
                }

            private Object valueOfTypeObject(Class<?> cls, HttpServletRequest request, String paramName) throws Exception {
                Field[] fields = cls.getDeclaredFields();
                Object instance = cls.getConstructor().newInstance();

                System.out.println(paramName);

                for (Field fld : fields) {
                    Method method = getMethodByField(fld, cls);

                    String parameterName = paramName != null ? paramName + "." + fld.getName() : fld.getName();


                    Object valueOfField = request.getParameter(parameterName);

                    Class<?> type = fld.getType();

                    System.out.println("FIeld : " + fld.getName() + " " + parameterName);

                    if (type.equals(Integer.class) || type.equals(int.class)) {
                        valueOfField =  Integer.valueOf((String) valueOfField);
                    } else if (type.equals(Double.class) || type.equals(double.class)) {
                        valueOfField =  Double.valueOf((String) valueOfField);
                    } else if (type.equals(Float.class) || type.equals(float.class)) {
                        valueOfField =  Float.valueOf((String) valueOfField);
                    } else if (type.equals(String.class)) {
                        valueOfField =  (String) valueOfField;
                    } else {
                        valueOfField =  valueOfTypeObject(fld.getType(), request, parameterName);
                    }

                    System.out.println(valueOfField);

                    method.invoke(instance, valueOfField);
                }

                return instance;
            }

    private Object valueObjectByType(Object obj, Parameter parameter, HttpServletRequest request) throws Exception {

        Class<?> cls = parameter.getType();
        System.out.println(cls.getName());

        System.out.println(obj + " " + cls.getTypeName());

        if (obj == null) {
            if (cls.equals(Integer.class) || cls.equals(int.class) ||
                    cls.equals(Double.class) || cls.equals(double.class) ||
                    cls.equals(Float.class) || cls.equals(float.class)) {
                return 0;
            }
            return null;
        }

        if (Map.class.isAssignableFrom(cls)) {

            if (!isMapStringObject(parameter)) {
                // Tsy mithrow leh izi fa null fotsiny leh retoure
                return null;
            }

            Map<String, Object> map = new HashMap<>();

            Enumeration<String> keys = request.getParameterNames();

            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                map.put(key, request.getParameter(key));
            }

            return map;

        }  else if (cls.equals(Integer.class) || cls.equals(int.class)) {
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

    public ClassMethod getByUrl(HashMap<String, HashMap<String, ClassMethod>> mapMethod, String url, String methode) {

        HashMap<String, ClassMethod> map = mapMethod.get(methode);

        for (String urlMap : map.keySet()) {

            System.out.println(urlMap);

            if (urlMap.equals(url)) {
                return map.get(urlMap);
            }

            String urlRegex = urlToRegex(urlMap);

            pattern = Pattern.compile(urlRegex);
            Matcher matcher = pattern.matcher(url);

            if (matcher.matches()) {
                System.out.println("Mi Match leh izi !!");
                ClassMethod classMethod = map.get(urlMap);
                classMethod.setMatcher(matcher);

                return classMethod;
            }
        }
        return null;
    }

    /**
     * Ito ilay mamerina anleh Hasmap miaraka amin methode
     * zany oe raha maka oe post de izay classmethode misy Postmapping no alainy
     * @return
     */
    public HashMap<String, HashMap<String, ClassMethod>> giveUrlMap() throws Exception {
        HashMap<String, HashMap<String, ClassMethod>> urlMap = new HashMap<>();
        
        for (int a = 0; a < listMethode.length; a++) {
            HashMap<String, ClassMethod> clsMethodeForAMethode =  urlMapping(listAnnotation[a]);
            urlMap.put(listMethode[a], clsMethodeForAMethode);
        }
        return urlMap;
    }

    /**
     * Hasmap avy amin'ny methode iray, Ex. Post ou Get
     * @param clsAnnotation
     * @return
     */
    public HashMap<String, ClassMethod> urlMapping(Class<? extends Annotation> clsAnnotation) throws Exception {
        HashMap<String, ClassMethod> map = new HashMap<>();

        PackageScanner packageScanner = new PackageScanner(null);
        List<Class<?>> clsController = packageScanner.clsController();

        for (Class<?> cls : clsController) {
            List<Method> methods = methodeAnnoteByClass(cls, clsAnnotation);

            for (Method method : methods) {
                String value = urlValue(method.getAnnotation(clsAnnotation));
                map.put(value, new ClassMethod(cls, method, value));
            }
        }
        return map;
    }

        private <A extends Annotation> String urlValue(A annotation) throws Exception {
            String value = null;

            for (int a = 0; a < listAnnotation.length; a++) {
                
                if (annotation.annotationType().equals(listAnnotation[a])) {

                    Method method = annotation.getClass().getDeclaredMethod("value");
                    value = (String) method.invoke(annotation);
                    break;
                }
            }

            if (value == null) {
                throw new Exception("Aucun URL pour ce type d'annotation");
            }
            return value;
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

package com.mhframework.handler.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
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

import com.mhframework.annotation.method.GetMapping;
import com.mhframework.annotation.method.PostMapping;
import com.mhframework.annotation.param.ParamRequest;
import com.mhframework.annotation.param.Session;
import com.mhframework.utils.PackageScanner;
import com.mhframework.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

public class UrlHandler {

    private Pattern pattern = null;
    private String[] listMethode = new String[] {"GET", "POST"};
    private List<String> listParamName = new ArrayList<>();
    private Pattern patternTab = Pattern.compile("^\\[(\\d)\\].*");
    private Pattern patternFile = Pattern.compile("(?<filename>.+)\\.(?<ext>.+)");
    private boolean isSession = false;
    private Map<String, Object> mapSession;

    @SuppressWarnings("unchecked")
    private Class<? extends Annotation>[] listAnnotation = new Class[] {GetMapping.class, PostMapping.class};

        private Object castByTypeClassic(Object obj, Class<?> cls) { 
            if (cls.equals(Integer.class) || cls.equals(int.class)) {
                return Integer.valueOf((String) obj);
            } else if (cls.equals(Double.class) || cls.equals(double.class)) {
                return Double.valueOf((String) obj);
            } else if (cls.equals(Float.class) || cls.equals(float.class)) {
                return Float.valueOf((String) obj);
            } else if (cls.equals(String.class)) {
                return (String) obj;
            }
            return null;
        }

        private boolean isTypeObject(Class<?> cls) {
            if (cls.equals(Integer.class) || cls.equals(int.class)) {
                return false;
            } else if (cls.equals(Double.class) || cls.equals(double.class)) {
                return false;
            } else if (cls.equals(Float.class) || cls.equals(float.class)) {
                return false;
            } else if (cls.equals(String.class)) {
                return false;
            } else if (cls.equals(Map.class)) {
                return false;
            }
            return true;
        }

        private Object invokeMethod(Method method, Object instance, Object[] valueParam, HttpServletRequest request) throws Exception {
            Object value = null;
            if (valueParam == null) {
                value =  method.invoke(instance);
            } else {
                value =  method.invoke(instance, valueParam);
            }

            if (isSession) {
                isSession = false;

                HttpSession session = request.getSession();

                for (String key : mapSession.keySet()) {
                    session.setAttribute(key, mapSession.get(key));
                }
            }
            return value;
        }

    public Object invokeMethodeUrl(HttpServletRequest request, ClassMethod classMethod) throws Exception {
        Object instance = classMethod.getCls().getConstructor().newInstance();
        Method method = classMethod.getMethod();

        Parameter[] parameters = method.getParameters();

        if (parameters.length == 0) {
            return invokeMethod(method, instance, null, request);
        }

        Object[] valuesParam = initTableau(parameters.length);
        Enumeration<String> enumParamName = request.getParameterNames();

        if (enumParamName.hasMoreElements()) {
                enumParamName.asIterator().forEachRemaining(e -> {
                listParamName.add(e);
            });
        }

        try {
            if (listParamName.isEmpty()) {
                Matcher matcher = classMethod.getMatcher();
                for (int a = 0; a < valuesParam.length; a++) {
                    if (matcher != null) {
                         String value = matcher.group(parameters[a].getName());
                        if (value == null) {
                            throw new Exception("Le param " + parameters[a].getName() + " doit avoir du valeur car null est trouvé");
                        }
                        valuesParam[a] = valueObjectByType(value, parameters[a], request);
                    } else if (Map.class.isAssignableFrom(parameters[a].getType())) {
                        valuesParam[a] = valueObjectByType(null, parameters[a], request);
                    }
                }
            }

            for (int a = 0; a < parameters.length; a++) {
                
                ParamRequest paramRequest = parameters[a].getAnnotation(ParamRequest.class);

                for (String name : this.listParamName) {

                    System.out.println(parameters[a].getName());

                    if (isTypeObject(parameters[a].getType())) {
                        valuesParam[a] = valueOfTypeObject(parameters[a].getType(), request, parameters[a].getName());
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
            e.printStackTrace();
            throw e;
        }

        return invokeMethod(method, instance, valuesParam, request);
    }

    private Object[] initTableau(int length) {
        Object[] taObjects = new Object[length];
        for (int a = 0; a < length; a++) {
            taObjects[a] = null;
        }
        return taObjects;
    }

        private Type[] giveParameteryzedTypeMap(Parameter parameter) {
            if (!Map.class.isAssignableFrom(parameter.getType())) {
                return null;
            }
            Type type = parameter.getParameterizedType();
            if (!(type instanceof ParameterizedType)) {
                return null;
            }
            ParameterizedType pType = (ParameterizedType) type;
            Type[] args = pType.getActualTypeArguments();
            return args;
        }

            private boolean checkAssignationClass(Class<?> cls, Type type) throws Exception{
                return cls.equals(Class.forName(type.getTypeName().split("<")[0]));
            }

        private boolean isMapParameterByClss(Parameter parameter, Class<?> cls1, Class<?> cls2) throws Exception {
            Type[] parTypes = giveParameteryzedTypeMap(parameter);
            if ( parTypes == null) {
                return false;
            }
            if (parTypes.length != 2) {
                return false;
            }

            return checkAssignationClass(cls1, parTypes[0]) && checkAssignationClass(cls2, parTypes[1]);
        }

                private Method getMethodByField(Field field, Class<?> cls) throws Exception {
                    System.out.println(field.getName());
                    return cls.getMethod("set" + Utils.capitalize(field.getName()), field.getType());
                }

                    /**
                     * Mijery ny isanleh tableau amin'ny alalan'ny parametre
                     * @param all
                     * @param varialbleName
                     * @return
                     * @throws Exception
                     */
                    private int lengthTab(List<String> all, String varialbleName) throws Exception {
                        List<String> filtre = all
                                .stream()
                                .filter(e -> e.startsWith(varialbleName))
                                .toList();
                        if (filtre.size() <= 0) {
                            throw new Exception("Il n'y a pas de tableau pour le parametre : " + varialbleName);
                        }
                        int max = 0;
                        boolean check = false;
                        Matcher matcher = null;
                        for (String str : filtre) {
                            String replStr = str.replace(varialbleName, "");
                            matcher = patternTab.matcher(replStr);
                            if (matcher.matches()) {
                                check = true;
                                int value = Integer.parseInt(matcher.group(1));
                                if (value > max) {
                                    max = value;
                                }
                            } else {
                                throw new Exception("Peut etre que ce n'est pas une tableau : " + str);
                            }
                            
                        }

                        return check ? max + 1 : max;
                    }

                    /**
                     * Mijery Dimension anah tableau anakiray
                     * @param cls
                     * @return
                     * @throws Exception
                     */
                    private int dimension(Class<?> cls) throws Exception {
                        String name = cls.getName();
                        int cpt = 0;
                        for (char c : name.toCharArray()) {
                            if (c == '[') {
                                cpt++;
                            }
                        }
                        return cpt;
                    }

            // DANGER Mila ovaina
            private Object valueOfTypeObject(Class<?> cls, HttpServletRequest request, String paramName) throws Exception {
                if (cls.isArray()) {
                    Class<?> componentType = cls.getComponentType();

                    if (isTypeObject(componentType) || dimension(cls) > 1) {
                        int lengthTab = lengthTab(listParamName, paramName);

                        Object array = Array.newInstance(componentType, lengthTab);

                        // System.out.println(array.getClass() + " ito leh classes " + cls);

                        for (int a = 0; a < lengthTab; a++) {
                            Array.set(array, a, valueOfTypeObject(componentType, request, paramName + "[" + a + "]"));
                        }

                        return array;

                    } else {
                        String[] values = request.getParameterValues(paramName);

                        int length = values == null ? 0 : values.length;

                        Object rep = Array.newInstance(componentType, length);

                        for (int a = 0; a < length; a++) {
                            Object val = values[a];
                            val = castByTypeClassic(val, componentType);
                            Array.set(rep, a, val);
                        }
                        return rep;
                    }
                } else {
                    Field[] fields = cls.getDeclaredFields();
                    Object instance = cls.getConstructor().newInstance();
                    
                    for (Field fld : fields) {
                        Method method = getMethodByField(fld, cls);
                        Class<?> fldType = fld.getType();
                        String parameterName = paramName + "." + fld.getName();

                        // System.out.println("Parametername : " + parameterName);

                        if (isTypeObject(fldType)) {
                            method.invoke(instance, valueOfTypeObject(fldType, request, parameterName));
                        } else {
                            Object valueOfField = request.getParameter(parameterName);

                            System.out.println("Field : " + fld.getName() + " " + parameterName);

                            Object tempValue = castByTypeClassic(valueOfField, fldType);

                            valueOfField = tempValue;

                            System.out.println("valueOfField : " + valueOfField);

                            method.invoke(instance, valueOfField);
                        }
                    }
                    return instance;
                }
            }

    private Object valueObjectByType(Object obj, Parameter parameter, HttpServletRequest request) throws Exception {

        Class<?> cls = parameter.getType();
        System.out.println(cls.getName());

        System.out.println(obj + " " + cls.getTypeName());

        boolean isMap = Map.class.isAssignableFrom(cls);

        if (obj == null && !isMap) {
            if (cls.equals(Integer.class) || cls.equals(int.class) ||
                    cls.equals(Double.class) || cls.equals(double.class) ||
                    cls.equals(Float.class) || cls.equals(float.class)) {
                return 0;
            }
            return null;
        }

        if (isMap) {

            if (isMapParameterByClss(parameter, String.class, Object.class)) {
                if (parameter.getAnnotation(Session.class) != null) {
                    isSession = true;

                    HttpSession httpSession = request.getSession();
                    mapSession = new HashMap<>();
                    
                    httpSession.getAttributeNames().asIterator().forEachRemaining((e) -> {
                        mapSession.put(e, httpSession.getAttribute(e));
                    });

                    return mapSession;

                } else {
                     Map<String, Object> map = new HashMap<>();
                    Enumeration<String> keys = request.getParameterNames();
                    while (keys.hasMoreElements()) {
                        String key = keys.nextElement();
                        map.put(key, request.getParameter(key));
                    }
                    return map;
                }
               
            } else if (isMapParameterByClss(parameter, String.class, List.class)) {
                Type[] tp = giveParameteryzedTypeMap(parameter);
                
                ParameterizedType listParamType = (ParameterizedType) tp[1];

                // ! Gestoin de fichier
                if (checkAssignationClass(MultpartFile.class, listParamType.getActualTypeArguments()[0])) {
                    Map<String, List<MultpartFile>> values = new  HashMap<>();
                    List<Part> parts = request.getParts().parallelStream().toList();

                    String name; MultpartFile multpartFile;
                    Matcher matcher;

                    List<MultpartFile> listMultpartFiles;

                    for (Part part : parts) {
                        name = part.getName();
                        
                        if (!values.containsKey(name)) {
                            listMultpartFiles = new ArrayList<>();
                            values.put(name, listMultpartFiles);
                        } else {
                            listMultpartFiles = values.get(name);
                        }

                        matcher = patternFile.matcher(part.getSubmittedFileName());
                        if (!matcher.matches()) {
                            throw new Exception();
                        }
                        if (matcher.group("ext") == null) {
                            throw new Exception("Le Fichier doivent avoir de l'extension");
                        }

                        multpartFile = new MultpartFile(matcher.group("filename"), matcher.group("ext"), part.getInputStream().readAllBytes());

                        values.get(name).add(multpartFile);
                    }

                    return values;
                }
            }

            return null;
        }
        return castByTypeClassic(obj, cls);
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

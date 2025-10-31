package com.mhframework.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.mhframework.annotation.classes.Controller;

public class PackageScanner {

    private String packageName;

    public PackageScanner(String packageName) {
        this.packageName = packageName;
    }

    public List<Class<?>> getClassAnnote() throws Exception {
        List<Class<?>> filtreClass = getClasses()
                            .stream()
                            .filter(e -> e.getAnnotation(Controller.class) != null)
                            .toList();
        return filtreClass;
    }

    public List<Class<?>> getClasses() throws ClassNotFoundException, IOException {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File directory = new File(resource.getFile());
            if (directory.exists()) {
                classes.addAll(findClasses(directory, packageName));
            }
        }

        return classes;
    }

    private List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files == null)
            return classes;

        for (File file : files) {
            if (file.isDirectory()) {
                // Parcours récursif des sous-packages
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().replace(".class", "");
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}

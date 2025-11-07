package com.mhframework.utils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.mhframework.annotation.classes.Controller;

public class PackageScanner {

    private String packageName;

    public PackageScanner(String packageName) {
        this.packageName = packageName;
    }

    public List<Class<?>> clsController() {
        return getListAnnote(getProjectClasses(), Controller.class);
    }

    public  static List<Class<?>> getListAnnote(List<Class<?>> classes, Class<? extends Annotation> cls) {
        return classes
                .stream()
                .filter(e -> e.getAnnotation(cls) != null)
                .toList();
    }

    private List<Class<?>> getProjectClasses() {
        List<Class<?>> classes = new ArrayList<>();

        try {
            String path = (packageName == null ? "" : packageName.replace(".", "/"));

            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                System.out.println(resource);

                if (resource.getProtocol().equals("file")) {
                    File directory = new File(URLDecoder.decode(resource.getFile(), "UTF-8"));
                    scanDirectory(directory, directory.getAbsolutePath(), classes);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return classes;
    }

    private void scanDirectory(File directory, String directoryRootPath, List<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files == null)
            return;

        for (File file : files) {

            if (file.isDirectory()) {
                scanDirectory(file, directoryRootPath, classes);
                continue;
            }

            if (file.getName().endsWith(".class")) {
                String absolute = file.getAbsolutePath();

                int substrCpt;

                if (packageName == null || packageName.length() == 0) {
                    substrCpt = directoryRootPath.length() + 1;
                } else {
                    substrCpt = directoryRootPath.length() - packageName.length();
                }

                String relative = absolute.substring(substrCpt);

                String className = relative
                        .replace(File.separatorChar, '.')
                        .replace(".class", "");

                try {
                    Class<?> cls = Thread.currentThread()
                            .getContextClassLoader()
                            .loadClass(className);

                    classes.add(cls);

                } catch (ClassNotFoundException e) {
                    System.out.println("Not found: " + className);
                }
            }
        }
    }
}

package com.mhframework.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class Configuration {

    private static final String propertiesDir = "conf";
    private static final String propertiesFile = "mh.properties";
    
    public static Properties loadProperties() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream(System.getProperty("user.dir") + File.separator + propertiesDir + File.separator + propertiesFile));
        return properties;
    }
}

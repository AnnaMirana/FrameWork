package com.mhframework.handler.controller;

import java.io.File;

import jakarta.servlet.http.Part;

public class MultpartFile {

    private String inputName;
    private Part part;

    public MultpartFile(String inputName, Part part) {
        this.inputName = inputName;
        this.part = part;
    }

    public void save(String path) throws Exception {
        String realPath = path + File.separator + System.currentTimeMillis() + "_" + inputName + "_" + part.getSubmittedFileName();
        part.write(realPath);
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

}

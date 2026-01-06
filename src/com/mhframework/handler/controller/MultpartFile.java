package com.mhframework.handler.controller;

import jakarta.servlet.http.Part;

public class MultpartFile {

    private Part part;

    public MultpartFile(Part part) {
        this.part = part;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

}

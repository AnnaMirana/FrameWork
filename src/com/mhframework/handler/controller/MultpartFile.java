package com.mhframework.handler.controller;

public class MultpartFile {

    private String name;
    private String extension;
    private byte[] bytes;

    public MultpartFile(String name, String extension, byte[] bytes) {
        this.name = name;
        this.extension = extension;
        this.bytes = bytes;
    }

    @Override
    public String toString() {
        return "MultpartFile [name=" + name + ", extension=" + extension + "]" + "len : " + bytes.length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

}

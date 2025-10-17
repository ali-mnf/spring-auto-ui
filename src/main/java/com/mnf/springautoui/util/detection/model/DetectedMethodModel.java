package com.mnf.springautoui.util.detection.model;

import org.springframework.http.HttpMethod;

import java.util.List;

public class    DetectedMethodModel {

    private HttpMethod httpMethod;

    private List<String> paths;

    private String controllerClassName;

    private String javaMethodName;

    private List<String> produces;

    private List<String> consumes;

    private List<MethodInputModel> methodInputModelList;

    private MethodOutputModel methodOutputModel;

    private List<String> headers;

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public List<MethodInputModel> getMethodInputModelList() {
        return methodInputModelList;
    }

    public void setMethodInputModelList(List<MethodInputModel> methodInputModelList) {
        this.methodInputModelList = methodInputModelList;
    }

    public MethodOutputModel getMethodOutputModel() {
        return methodOutputModel;
    }

    public void setMethodOutputModel(MethodOutputModel methodOutputModel) {
        this.methodOutputModel = methodOutputModel;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public String getControllerClassName() {
        return controllerClassName;
    }

    public void setControllerClassName(String controllerClassName) {
        this.controllerClassName = controllerClassName;
    }

    public String getJavaMethodName() {
        return javaMethodName;
    }

    public void setJavaMethodName(String javaMethodName) {
        this.javaMethodName = javaMethodName;
    }

    public List<String> getProduces() {
        return produces;
    }

    public void setProduces(List<String> produces) {
        this.produces = produces;
    }

    public List<String> getConsumes() {
        return consumes;
    }

    public void setConsumes(List<String> consumes) {
        this.consumes = consumes;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }
}

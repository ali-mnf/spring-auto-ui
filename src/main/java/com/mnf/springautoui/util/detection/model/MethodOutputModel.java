package com.mnf.springautoui.util.detection.model;

public class MethodOutputModel {

    private String name;

    private Class<?> returnClass;

    private ClassFieldsModel fieldsModel;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getReturnClass() {
        return returnClass;
    }

    public void setReturnClass(Class<?> returnClass) {
        this.returnClass = returnClass;
    }

    public ClassFieldsModel getFieldsModel() {
        return fieldsModel;
    }

    public void setFieldsModel(ClassFieldsModel fieldsModel) {
        this.fieldsModel = fieldsModel;
    }
}

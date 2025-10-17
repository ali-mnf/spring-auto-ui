package com.mnf.springautoui.util.detection.model;

public class MethodInputModel {

    private InputSourceEnum inputSourceEnum;

    private String name;

    private Class<?> Type;

    private boolean required;

    private String defaultValue;

    private ValidationMeta validation;

    private ClassFieldsModel fieldsModel;

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ValidationMeta getValidation() {
        return validation;
    }

    public void setValidation(ValidationMeta validation) {
        this.validation = validation;
    }

    public InputSourceEnum getInputSourceEnum() {
        return inputSourceEnum;
    }

    public void setInputSourceEnum(InputSourceEnum inputSourceEnum) {
        this.inputSourceEnum = inputSourceEnum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getType() {
        return Type;
    }

    public void setType(Class<?> type) {
        Type = type;
    }

    public ClassFieldsModel getFieldsModel() {
        return fieldsModel;
    }

    public void setFieldsModel(ClassFieldsModel fieldsModel) {
        this.fieldsModel = fieldsModel;
    }


    public static class ValidationMeta {
        private Integer min;
        private Integer max;
        private String pattern;
        private Boolean notNull;

        public Integer getMin() {
            return min;
        }

        public void setMin(Integer min) {
            this.min = min;
        }

        public Integer getMax() {
            return max;
        }

        public void setMax(Integer max) {
            this.max = max;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public Boolean getNotNull() {
            return notNull;
        }

        public void setNotNull(Boolean notNull) {
            this.notNull = notNull;
        }
    }
}

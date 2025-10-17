package com.mnf.springautoui.controller.models;

import java.util.List;

public record EndPointVM(String id,
                         String controller,
                         String controllerFqn,
                         String javaMethodName,
                         String httpMethod,
                         List<String> paths,
                         List<String> consumes,
                         List<String> produces,
                         List<String> headers,
                         List<InputVM> inputs,
                         OutputVM output) {

    public String firstPath() {
        return (paths != null && !paths.isEmpty()) ? paths.get(0) : "/";
    }

}

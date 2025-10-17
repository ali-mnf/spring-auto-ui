package com.mnf.springautoui.controller.models;

public record InputVM(String name,
                      String source,
                      String type,
                      Boolean required,
                      String defaultValue,
                      String exampleJson) {
}

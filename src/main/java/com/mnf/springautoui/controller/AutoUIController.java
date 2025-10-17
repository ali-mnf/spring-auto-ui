package com.mnf.springautoui.controller;

import com.mnf.springautoui.controller.models.EndPointVM;
import com.mnf.springautoui.controller.models.InputVM;
import com.mnf.springautoui.controller.models.OutputVM;
import com.mnf.springautoui.util.annotaions.ExcludeFromUI;
import com.mnf.springautoui.util.detection.ControllerMethodDetector;
import com.mnf.springautoui.util.detection.model.DetectedMethodModel;
import com.mnf.springautoui.util.detection.model.MethodInputModel;
import com.mnf.springautoui.util.detection.model.MethodOutputModel;
import jakarta.servlet.ServletContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@ExcludeFromUI
public class AutoUIController {

    private final ControllerMethodDetector detector;
    private final ServletContext servletContext;

    public AutoUIController(ControllerMethodDetector detector, ServletContext servletContext) {
        this.detector = detector;
        this.servletContext = servletContext;
    }


    @GetMapping(value = "/auto-ui", produces = MediaType.TEXT_HTML_VALUE)
    public RedirectView uiRoot() {
        return new RedirectView(servletContext.getContextPath() + "/auto-ui/index.html");
    }

    @GetMapping(value = "/auto-ui/api/endpoints", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<EndPointVM> endpoints() throws ClassNotFoundException {
        Map<Class<?>, List<DetectedMethodModel>> map = detector.AllEndPointsDetail();
        return map.entrySet().stream()
                .flatMap(e -> {
                    String controller = e.getKey().getSimpleName();
                    String controllerFqn = e.getKey().getName();
                    return e.getValue().stream().map(m -> toEndPointVM(controller, controllerFqn, m));
                })
                .sorted(Comparator.comparing(EndPointVM::controller).thenComparing(EndPointVM::firstPath))
                .collect(Collectors.toList());
    }

    private EndPointVM toEndPointVM(String controller, String controllerFqn, DetectedMethodModel m) {
        String id = controllerFqn + "#" +
                (m.getMethodOutputModel() != null ? m.getMethodOutputModel().getName() : "method");
        List<InputVM> inputs = Optional.ofNullable(m.getMethodInputModelList()).orElseGet(List::of)
                .stream()
                .map(i -> new InputVM(
                        i.getName(),
                        i.getInputSourceEnum() != null ? i.getInputSourceEnum().name() : null,
                        i.getType() != null ? i.getType().getSimpleName() : null,
                        i.isRequired(),
                        i.getDefaultValue(),
                        buildExampleJsonFor(i)
                )).toList();

        List<String> fields = new ArrayList<>();

        if (m.getMethodOutputModel() != null
                && m.getMethodOutputModel().getFieldsModel() != null
                && m.getMethodOutputModel().getFieldsModel().getFields() != null) {

            fields = m.getMethodOutputModel()
                    .getFieldsModel()
                    .getFields()
                    .stream()
                    .map(f -> f.getName() + ":" + f.getType().getSimpleName())
                    .toList();
        }

        OutputVM output = new OutputVM(
                Optional.ofNullable(m.getMethodOutputModel())
                        .map(MethodOutputModel::getReturnClass)
                        .map(Class::getSimpleName)
                        .orElse(null),
                fields
        );

        return new EndPointVM(
                id,
                controller,
                controllerFqn,
                /* javaMethodName */ null,  // set if you added it to DetectedMethodModel
                m.getHttpMethod() != null ? m.getHttpMethod().name() : "GET",
                Optional.ofNullable(m.getPaths()).orElseGet(List::of),
                Optional.ofNullable(m.getConsumes()).orElseGet(List::of),
                Optional.ofNullable(m.getProduces()).orElseGet(List::of),
                Optional.ofNullable(m.getHeaders()).orElseGet(List::of),
                inputs,
                output
        );
    }


    private String buildExampleJsonFor(MethodInputModel i) {
        if (i.getInputSourceEnum() == null || i.getType() == null) return null;
        if (!"REQUEST_BODY".equals(i.getInputSourceEnum().name())) return null;

        Class<?> t = i.getType();
        if (t.isPrimitive() || t == String.class || Number.class.isAssignableFrom(t) || t == Boolean.class) {
            if (t == String.class) return "\"example\"";
            if (t == Boolean.class || t == boolean.class) return "true";
            return "0";
        }
        if (t.isArray()) {
            return "[]";
        }
        if (java.util.Collection.class.isAssignableFrom(t)) {
            return "[]";
        }
        try {
            var f = t.getDeclaredFields();
            if (f.length == 0) return "{}";
            StringBuilder sb = new StringBuilder("{\n");
            for (int idx = 0; idx < f.length; idx++) {
                var field = f[idx];
                field.setAccessible(true);
                sb.append("  \"").append(field.getName()).append("\": ");
                Class<?> ft = field.getType();
                if (ft == String.class) {
                    sb.append("\"\"");
                } else if (ft == Boolean.class || ft == boolean.class) {
                    sb.append("false");
                } else if (Number.class.isAssignableFrom(ft)
                        || ft.isPrimitive() && ft != boolean.class && ft != char.class) {
                    sb.append("0");
                } else {
                    sb.append("{}");
                }
                if (idx < f.length - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("}");
            return sb.toString();
        } catch (Exception ignored) {
            return "{}";
        }
    }

}
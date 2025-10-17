package com.mnf.springautoui.util.detection;

import com.mnf.springautoui.util.annotaions.ExcludeFromUI;
import com.mnf.springautoui.util.annotaions.IncludeInUI;
import com.mnf.springautoui.util.detection.model.*;
import com.mnf.springautoui.util.propertiesConfig.DetectionTypeEnum;
import com.mnf.springautoui.util.propertiesConfig.PropertiesService;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

@Component
public class ControllerMethodDetector {

    private final ListableBeanFactory listableBeanFactory;
    private final PropertiesService propertiesService;
    private final ServletContext servletContext;


    @Autowired
    public ControllerMethodDetector(ListableBeanFactory listableBeanFactory, PropertiesService propertiesService, ServletContext servletContext) {
        this.listableBeanFactory = listableBeanFactory;
        this.propertiesService = propertiesService;

        this.servletContext = servletContext;
    }


    private List<String> resolveClassLevelPaths(Class<?> controller) {

        RequestMapping requestMapping = controller.getAnnotation(RequestMapping.class);
        List<String> raw = new ArrayList<>();
        if (requestMapping != null) {
            if (requestMapping.value().length > 0) raw.addAll(Arrays.asList(requestMapping.value()));
            if (requestMapping.path().length > 0) raw.addAll(Arrays.asList(requestMapping.path()));
        }
        List<String> norm = normalizePaths(raw);
        return norm.isEmpty() ? List.of("") : norm;
    }

    private List<String> resolveMethodPaths(Method method) {

        List<String> res = new ArrayList<>();
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);

        if (requestMapping != null) {

            if (requestMapping.value().length > 0) res.addAll(Arrays.asList(requestMapping.value()));
            if (requestMapping.path().length > 0) res.addAll(Arrays.asList(requestMapping.path()));

        } else if (method.getAnnotation(GetMapping.class) != null) {

            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            if (getMapping.value().length > 0) res.addAll(Arrays.asList(getMapping.value()));
            if (getMapping.path().length > 0) res.addAll(Arrays.asList(getMapping.path()));

        } else if (method.getAnnotation(PostMapping.class) != null) {

            PostMapping postMapping = method.getAnnotation(PostMapping.class);
            if (postMapping.value().length > 0) res.addAll(Arrays.asList(postMapping.value()));
            if (postMapping.path().length > 0) res.addAll(Arrays.asList(postMapping.path()));

        } else if (method.getAnnotation(PutMapping.class) != null) {

            PutMapping putMapping = method.getAnnotation(PutMapping.class);
            if (putMapping.value().length > 0) res.addAll(Arrays.asList(putMapping.value()));
            if (putMapping.path().length > 0) res.addAll(Arrays.asList(putMapping.path()));

        } else if (method.getAnnotation(DeleteMapping.class) != null) {

            DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
            if (deleteMapping.value().length > 0) res.addAll(Arrays.asList(deleteMapping.value()));
            if (deleteMapping.path().length > 0) res.addAll(Arrays.asList(deleteMapping.path()));
        }

        List<String> norm = normalizePaths(res);
        return norm.isEmpty() ? List.of("") : norm;
    }

    private List<String> resolveConsumes(Method method) {

        List<String> result = new ArrayList<>();
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null && requestMapping.consumes().length > 0)
            result.addAll(Arrays.asList(requestMapping.consumes()));

        if (result.isEmpty()) {

            if (method.isAnnotationPresent(PostMapping.class)) {

                String[] c = method.getAnnotation(PostMapping.class).consumes();
                if (c.length > 0) result.addAll(Arrays.asList(c));
            } else if (method.isAnnotationPresent(PutMapping.class)) {

                String[] c = method.getAnnotation(PutMapping.class).consumes();
                if (c.length > 0) result.addAll(Arrays.asList(c));
            }
        }
        return result;
    }

    private List<String> resolveProduces(Method method) {

        List<String> res = new ArrayList<>();
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null && requestMapping.produces().length > 0)
            res.addAll(Arrays.asList(requestMapping.produces()));

        if (res.isEmpty()) {

            if (method.isAnnotationPresent(GetMapping.class)) {
                String[] p = method.getAnnotation(GetMapping.class).produces();
                if (p.length > 0) res.addAll(Arrays.asList(p));

            } else if (method.isAnnotationPresent(PostMapping.class)) {
                String[] p = method.getAnnotation(PostMapping.class).produces();
                if (p.length > 0) res.addAll(Arrays.asList(p));

            } else if (method.isAnnotationPresent(PutMapping.class)) {
                String[] p = method.getAnnotation(PutMapping.class).produces();
                if (p.length > 0) res.addAll(Arrays.asList(p));

            } else if (method.isAnnotationPresent(DeleteMapping.class)) {
                String[] p = method.getAnnotation(DeleteMapping.class).produces();
                if (p.length > 0) res.addAll(Arrays.asList(p));
            }
        }
        return res;
    }

    private HttpMethod resolveHttpMethod(Method method) {

        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null && requestMapping.method().length > 0) {
            return HttpMethod.valueOf(requestMapping.method()[0].name());

        }
        if (method.isAnnotationPresent(GetMapping.class)) return HttpMethod.GET;
        if (method.isAnnotationPresent(PostMapping.class)) return HttpMethod.POST;
        if (method.isAnnotationPresent(PutMapping.class)) return HttpMethod.PUT;
        if (method.isAnnotationPresent(DeleteMapping.class)) return HttpMethod.DELETE;

        return HttpMethod.GET;
    }

    private List<String> resolveHeaders(Method method) {
        List<String> res = new ArrayList<>();
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null && requestMapping.headers().length > 0) {
            res.addAll(Arrays.asList(requestMapping.headers()));
        }

        if (res.isEmpty()) {
            if (method.isAnnotationPresent(GetMapping.class)) {
                String[] h = method.getAnnotation(GetMapping.class).headers();
                if (h.length > 0) res.addAll(Arrays.asList(h));
            } else if (method.isAnnotationPresent(PostMapping.class)) {
                String[] h = method.getAnnotation(PostMapping.class).headers();
                if (h.length > 0) res.addAll(Arrays.asList(h));
            } else if (method.isAnnotationPresent(PutMapping.class)) {
                String[] h = method.getAnnotation(PutMapping.class).headers();
                if (h.length > 0) res.addAll(Arrays.asList(h));
            } else if (method.isAnnotationPresent(DeleteMapping.class)) {
                String[] h = method.getAnnotation(DeleteMapping.class).headers();
                if (h.length > 0) res.addAll(Arrays.asList(h));
            }
        }
        return res;
    }


    private List<String> joinPaths(List<String> classPaths, List<String> methodPaths) {

        return classPaths.stream()
                .flatMap(cp -> methodPaths.stream()
                        .map(mp -> (cp + ("/".equals(mp) ? "" : mp)).replaceAll("//+", "/")))
                .map(p -> p.startsWith("/") ? p : "/" + p)
                .distinct()
                .toList();
    }

    public HashMap<Class<?>, List<Method>> getDetectedMethods() throws ClassNotFoundException {
        HashMap<Class<?>, List<Method>> classMethodsHashMap = new HashMap<>();
        Set<Class<?>> controllers = scanControllers();

        for (Class<?> raw : controllers) {

            Class<?> aClass = raw.getName().contains("$$") ? raw.getSuperclass() : raw;

            String pkg = aClass.getPackageName();
            if (pkg.startsWith("com.mnf.springautoui.controller")) {
                continue;
            }


            if (aClass.isAnnotationPresent(ExcludeFromUI.class)) {
                continue;
            }

            List<Method> classMethods = new ArrayList<>();

            if (propertiesService.getDetectionType() == DetectionTypeEnum.INCLUDE) {
                for (Method m : aClass.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(ExcludeFromUI.class)) continue;
                    if (!isRequestHandler(m)) continue;
                    classMethods.add(m);
                }
            } else { // EXCLUDE mode
                if (aClass.isAnnotationPresent(IncludeInUI.class)) {
                    for (Method m : aClass.getDeclaredMethods()) {
                        if (m.isAnnotationPresent(ExcludeFromUI.class)) continue;
                        if (!isRequestHandler(m)) continue;
                        classMethods.add(m);
                    }
                } else {
                    for (Method m : aClass.getDeclaredMethods()) {
                        if (!m.isAnnotationPresent(IncludeInUI.class)) continue;
                        if (!isRequestHandler(m)) continue;
                        classMethods.add(m);
                    }
                }
            }

            if (!classMethods.isEmpty()) {
                classMethodsHashMap.put(aClass, classMethods);
            }
        }
        return classMethodsHashMap;
    }

    private boolean isRequestHandler(Method m) {
        return m.isAnnotationPresent(RequestMapping.class)
                || m.isAnnotationPresent(GetMapping.class)
                || m.isAnnotationPresent(PostMapping.class)
                || m.isAnnotationPresent(PutMapping.class)
                || m.isAnnotationPresent(DeleteMapping.class)
                || m.isAnnotationPresent(PatchMapping.class);
    }


    public Set<Class<?>> scanControllers() {

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(Controller.class));

        Set<Class<?>> controllerClasses = new HashSet<>();
        for (BeanDefinition beanDefinition : provider.findCandidateComponents(propertiesService.getPackageToScan())) {
            try {

                Class<?> controllerClass = Class.forName(beanDefinition.getBeanClassName());
                controllerClasses.add(controllerClass);
            } catch (ClassNotFoundException e) {
                // Handle exception if needed
            }
        }

        return controllerClasses;
    }

    public HashMap<Class<?>, List<DetectedMethodModel>> AllEndPointsDetail() throws ClassNotFoundException {
        HashMap<Class<?>, List<Method>> detectedMethodsMap = this.getDetectedMethods();
        HashMap<Class<?>, List<DetectedMethodModel>> methodsModelMap = new HashMap<>();

        detectedMethodsMap.forEach((aClass, methods) -> {
            List<DetectedMethodModel> models = new ArrayList<>();
            for (Method method : methods) {
                models.add(this.getMethodDetails(method));
            }
            methodsModelMap.put(aClass, models);
        });

        return methodsModelMap;
    }

    private DetectedMethodModel getMethodDetails(Method method) {
        DetectedMethodModel model = new DetectedMethodModel();

        Class<?> controllerClass = method.getDeclaringClass();

        List<String> classPaths = resolveClassLevelPaths(controllerClass);
        List<String> methodPaths = resolveMethodPaths(method);
        List<String> fullPaths = joinPaths(classPaths, methodPaths);

        HttpMethod http = resolveHttpMethod(method);
        List<String> consumes = resolveConsumes(method);
        List<String> produces = resolveProduces(method);

        List<String> headers = resolveHeaders(method);
        model.setHeaders(headers);

        String ctx = servletContext.getContextPath();
        List<String> ctxPaths = fullPaths.stream()
                .map(p -> (ctx + p).replaceAll("//+", "/"))
                .toList();

        model.setControllerClassName(controllerClass.getName());
        model.setJavaMethodName(method.getName());
        model.setHttpMethod(http);
        model.setConsumes(consumes);
        model.setProduces(produces);
        model.setPaths(ctxPaths);

        model.setMethodInputModelList(this.getInputParameters(method));
        model.setMethodOutputModel(this.getOutputMethodModel(method));
        return model;
    }

    private List<MethodInputModel> getInputParameters(Method method) {

        List<MethodInputModel> params = new ArrayList<>();

        for (Parameter parameter : method.getParameters()) {

            MethodInputModel methodInputModel = new MethodInputModel();
            methodInputModel.setName(parameter.getName());
            methodInputModel.setType(parameter.getType());
            methodInputModel.setRequired(false);

            for (Annotation annotation : parameter.getAnnotations()) {

                if (annotation instanceof PathVariable pathVariable) {

                    methodInputModel.setInputSourceEnum(InputSourceEnum.PATH_VARIABLE);
                    methodInputModel.setRequired(pathVariable.required());

                    if (StringUtils.hasText(pathVariable.name())) methodInputModel.setName(pathVariable.name());
                    if (StringUtils.hasText(pathVariable.value())) methodInputModel.setName(pathVariable.value());

                } else if (annotation instanceof RequestParam requestParam) {

                    methodInputModel.setInputSourceEnum(InputSourceEnum.REQUEST_PARAM);
                    methodInputModel.setRequired(requestParam.required());

                    boolean required = requestParam.required();
                    if (hasExplicitDefault(requestParam.defaultValue()) || isOptionalParam(parameter)) {
                        required = false;
                    }
                    methodInputModel.setRequired(required);
                    if (hasExplicitDefault(requestParam.defaultValue())) {
                        methodInputModel.setDefaultValue(requestParam.defaultValue());
                    }

                    if (StringUtils.hasText(requestParam.name())) methodInputModel.setName(requestParam.name());
                    if (StringUtils.hasText(requestParam.value())) methodInputModel.setName(requestParam.value());

                } else if (annotation instanceof RequestHeader requestHeader) {

                    methodInputModel.setInputSourceEnum(InputSourceEnum.REQUEST_HEADER);
                    methodInputModel.setRequired(requestHeader.required());

                    if (StringUtils.hasText(requestHeader.name())) methodInputModel.setName(requestHeader.name());
                    if (StringUtils.hasText(requestHeader.value())) methodInputModel.setName(requestHeader.value());
                    boolean required = requestHeader.required();
                    if (hasExplicitDefault(requestHeader.defaultValue()) || isOptionalParam(parameter)) {
                        required = false;
                    }
                    methodInputModel.setRequired(required);
                    if (hasExplicitDefault(requestHeader.defaultValue())) {
                        methodInputModel.setDefaultValue(requestHeader.defaultValue());
                    }
                } else if (annotation instanceof RequestBody requestBody) {

                    methodInputModel.setInputSourceEnum(InputSourceEnum.REQUEST_BODY);
                    boolean required = requestBody.required() && !isOptionalParam(parameter);
                    methodInputModel.setRequired(required);
                }
            }

            MethodInputModel.ValidationMeta validationMeta = new MethodInputModel.ValidationMeta();

            for (Annotation annotation : parameter.getAnnotations()) {

                String an = annotation.annotationType().getName();
                switch (an) {
                    case "jakarta.validation.constraints.NotNull" -> validationMeta.setNotNull(true);
                    case "jakarta.validation.constraints.Min" -> {
                        try {
                            validationMeta.setMin((Integer) annotation.annotationType().getMethod("value").invoke(annotation));
                        } catch (Exception ignored) {
                        }
                    }
                    case "jakarta.validation.constraints.Max" -> {
                        try {
                            validationMeta.setMax((Integer) annotation.annotationType().getMethod("value").invoke(annotation));
                        } catch (Exception ignored) {
                        }
                    }
                    case "jakarta.validation.constraints.Size" -> {
                        try {
                            Integer min = (Integer) annotation.annotationType().getMethod("min").invoke(annotation);
                            Integer max = (Integer) annotation.annotationType().getMethod("max").invoke(annotation);
                            if (min != null && min > 0) validationMeta.setMin(min);
                            if (max != null && max < Integer.MAX_VALUE) validationMeta.setMax(max);
                        } catch (Exception ignored) {
                        }
                    }
                    case "jakarta.validation.constraints.Pattern" -> {
                        try {
                            validationMeta.setPattern((String) annotation.annotationType().getMethod("regexp").invoke(annotation));
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            methodInputModel.setValidation(validationMeta);

            params.add(methodInputModel);
        }
        return params;
    }

    private MethodOutputModel getOutputMethodModel(Method method) {

        MethodOutputModel methodOutputModel = new MethodOutputModel();

        Class<?> returnType = method.getReturnType();


        methodOutputModel.setReturnClass(returnType);
        methodOutputModel.setName(returnType.getSimpleName());

        methodOutputModel.setFieldsModel(this.getFields(returnType));

        return methodOutputModel;
    }

    private ClassFieldsModel getFields(Class<?> aClass) {

        ClassFieldsModel classFieldsModel = new ClassFieldsModel();

        if (Collection.class.isAssignableFrom(aClass)) {

            Type genericSuperclass = aClass.getGenericSuperclass();

            if (genericSuperclass instanceof ParameterizedType) {

                Type genericType = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
                if (genericType instanceof Class<?> collectionType) {
                    classFieldsModel.setInClass(genericType.getClass());
                    if (Collection.class.isAssignableFrom(collectionType)) {
                        // If the element type is also a Collection, recursively get its fields
                        this.getFields(collectionType);
                    } else {
                        // If the element type is a custom model, get its fields
                        classFieldsModel.setFields(List.of(collectionType.getDeclaredFields()));
                    }
                }

//                if (genericType instanceof Class<?> collectionType) {
//                    classFieldsModel.setInClass(genericType.getClass());
//                    this.getFields(collectionType);
//                }
//                classFieldsModel.setInClass(genericType.getClass());
            } else if (aClass.getTypeParameters().length > 0) {
                // Check if it's a parameterized type
                TypeVariable<?> typeVariable = aClass.getTypeParameters()[0];
                Type[] bounds = typeVariable.getBounds();

                if (bounds.length > 0 && bounds[0] instanceof Class<?> parameterizedType) {
                    classFieldsModel.setInClass(parameterizedType);
                    // Handle parameterized type as needed
                }
            }
        } else if (!aClass.isPrimitive() && !aClass.isArray() && !isWrapperType(aClass) && !String.class.isAssignableFrom(aClass)) {
            if (aClass.getDeclaredFields().length > 0) {

                classFieldsModel.setFields(List.of(aClass.getDeclaredFields()));
            }
        }


        return classFieldsModel;

    }

    private boolean isWrapperType(Class<?> clazz) {
        return clazz.equals(Integer.class)
                || clazz.equals(Long.class)
                || clazz.equals(Short.class)
                || clazz.equals(Float.class)
                || clazz.equals(Double.class)
                || clazz.equals(Byte.class)
                || clazz.equals(Character.class)
                || clazz.equals(Boolean.class);
    }

    private static boolean hasExplicitDefault(String v) {
        return StringUtils.hasText(v) && !ValueConstants.DEFAULT_NONE.equals(v);
    }

    private static boolean isOptionalParam(Parameter p) {
        return Optional.class.isAssignableFrom(p.getType());
    }

    private static List<String> normalizePaths(Collection<String> in) {

        if (in == null || in.isEmpty()) return List.of("");
        return in.stream()
                .filter(StringUtils::hasText)
                .map(p -> p.startsWith("/") ? p : "/" + p)
                .map(p -> p.replaceAll("//+", "/"))
                .distinct()
                .toList();
    }

}

package com.mnf.springautoui;

import com.mnf.springautoui.util.detection.ControllerMethodDetector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

@SpringBootTest
class SpringAutoUiApplicationTests {

    @Autowired
    private ControllerMethodDetector controllerMethodDetector;

    @Test
    void contextLoads() {
    }


}

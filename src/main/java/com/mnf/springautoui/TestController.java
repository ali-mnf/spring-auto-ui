package com.mnf.springautoui;

import com.mnf.springautoui.util.detection.model.MethodInputModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @PutMapping(value = "/method1/{t1}")
    public List<String> test1(@PathVariable Integer t1, @RequestBody String s1){
        return List.of("hi");
    }

    @RequestMapping(value = "/method2/{t1}",method = RequestMethod.GET)
    public String test2(@PathVariable Integer t1){
        return "hi";
    }


    @RequestMapping(value = "/method3/{t1}",method = RequestMethod.POST)
    public MethodInputModel test3(@PathVariable Integer t1, @RequestBody TestModel s1,@RequestParam(name = "aaa") String bbb
                                  ,@RequestHeader(name = "token") String token  ){
        return new MethodInputModel();
    }


}

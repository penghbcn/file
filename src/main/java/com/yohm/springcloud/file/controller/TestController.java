package com.yohm.springcloud.file.controller;

import com.yohm.springcloud.file.model.JsonResponse;
import com.yohm.springcloud.file.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping("/test")
    JsonResponse webTest(){
        return testService.webTest();
    }
    @PostMapping("/file/upload")
    JsonResponse uploadTest(@RequestParam("file") MultipartFile file,@RequestParam("path")String path){
        return testService.uploadTest(file,path);
    }
}

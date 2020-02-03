package com.yohm.springcloud.file.controller;

import com.yohm.springcloud.file.model.JsonResponse;
import com.yohm.springcloud.file.service.FtpService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/ftp")
public class FtpController {

    @Resource
    private FtpService ftpService;

    @PostMapping("/upload")
    JsonResponse upload(@RequestParam("file") MultipartFile file, @RequestParam("remote")String remote){
        return ftpService.upload(file,remote);
    }

    @GetMapping("/download")
    void download(@RequestParam("remote")String remote, @RequestParam("fileName")String fileName, HttpServletResponse response){
        ftpService.download(remote,fileName,response);
    }
}

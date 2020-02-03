package com.yohm.springcloud.file.service;

import com.yohm.springcloud.file.model.JsonResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface FtpService {
    JsonResponse upload(MultipartFile file, String remote);

    void download(String remote, String fileName, HttpServletResponse response);
}

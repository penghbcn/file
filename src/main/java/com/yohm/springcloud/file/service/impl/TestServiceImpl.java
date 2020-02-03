package com.yohm.springcloud.file.service.impl;

import com.yohm.springcloud.file.model.JsonResponse;
import com.yohm.springcloud.file.service.TestService;
import com.yohm.springcloud.file.utils.FtpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private FtpUtil ftpUtil;

    @Override
    public JsonResponse webTest() {
        ftpUtil.download("/","1.txt");
        return new JsonResponse(200,"success");
    }

    @Override
    public JsonResponse uploadTest(MultipartFile file,String path) {
        String uploadAddr = ftpUtil.upload(path,file);
        return new JsonResponse(200,"success",uploadAddr);
    }
}

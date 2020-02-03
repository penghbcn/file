package com.yohm.springcloud.file.service;

import com.yohm.springcloud.file.model.JsonResponse;
import org.springframework.web.multipart.MultipartFile;

public interface TestService {
    JsonResponse webTest();

    JsonResponse uploadTest(MultipartFile file,String path);
}

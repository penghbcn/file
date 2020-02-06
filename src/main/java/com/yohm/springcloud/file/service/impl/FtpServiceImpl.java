package com.yohm.springcloud.file.service.impl;

import com.yohm.springcloud.file.vo.JsonResponse;
import com.yohm.springcloud.file.service.FtpService;
import com.yohm.springcloud.file.utils.FtpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.Random;

@Service
public class FtpServiceImpl implements FtpService {
    private static final Logger LOG = LoggerFactory.getLogger(FtpServiceImpl.class);

    @Resource
    private FtpUtil ftpUtil;

    @Override
    public JsonResponse<String> upload(MultipartFile file, String remote) {
        String ftpFilePath = ftpUtil.upload(remote, file);
        if (StringUtils.isEmpty(ftpFilePath)) {
            return new JsonResponse<>(400, "failure");
        }
        return new JsonResponse<>(200, "success", ftpFilePath);
    }

    @Override
    public void download(String remote, String fileName, HttpServletResponse response) {
        File file = ftpUtil.download(remote, fileName);
        if(file == null){
//            return new JsonResponse<>(400, "文件不存在");
            return;
        }
        response.reset();
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            OutputStream outputStream = response.getOutputStream()){
            boolean isOnline = new Random().nextBoolean();
            System.out.println("isOnline = "+ isOnline);
            if(isOnline){
                URL u = new URL("file:///" +fileName);
                response.setContentType(u.openConnection().getContentType());
                response.setHeader("Content-Disposition", "inline; filename=" + file.getName());
            }else{
                response.setContentType("application/x-msdownload");
                response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            }
            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = bis.read(buf)) > 0){
                outputStream.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        file.delete();
//        return new JsonResponse<>(200, "success");
    }
}

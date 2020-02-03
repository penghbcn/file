package com.yohm.springcloud.file.utils;

import com.yohm.springcloud.file.annotation.FtpClientPool;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.ObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
public class FtpUtil {
    private static final Logger LOG = LoggerFactory.getLogger(FtpUtil.class);

    @Value("${ftp.host}")
    private String ftpHost;

    @Autowired
    @FtpClientPool
    private ObjectPool<FTPClient> ftpClientObjectPool;

    public String upload(String remote, String localFile) {
        File file = new File(localFile);
        return upload(remote,file);
    }

    public String upload(String remote, File file) {
        try {
            return upload(remote,file.getName(), new FileInputStream(file));
        } catch (IOException e) {
            LOG.error(e.getMessage(),e);
        }
        return null;
    }

    public String upload(String remote, MultipartFile file) {
        try {
            return upload(remote,file.getOriginalFilename(), file.getInputStream());
        } catch (IOException e) {
            LOG.error(e.getMessage(),e);
        }
        return null;
    }

    public String upload(String remote, String fileName, InputStream inputStream) {
        try {
            if(inputStream == null){
                return null;
            }
            FTPClient ftpClient = getFtpClient();
            if (!switchToTargetDirectory(remote, ftpClient)) {
                return null;
            }
            String encodingFileName = transferFtpEncoding(fileName);
            if(ftpClient.storeFile(encodingFileName, inputStream)){
                return generateFtpFilePath(remote,fileName);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(),e);
        }
        return null;
    }

    public File download(String remote, String fileName){
        remote = transferFtpEncoding(remote);
        fileName = transferFtpEncoding(fileName);
        File file = new File(fileName);
        try(FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            FTPClient ftpClient = getFtpClient();
            if (switchToTargetDirectory(remote, ftpClient)) {
                ftpClient.retrieveFile(fileName,fileOutputStream);
                return file;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(),e);
        }
        file.delete();
        return null;
    }

    private boolean switchToTargetDirectory(String remote, FTPClient ftpClient) throws IOException {
        remote = transferFtpEncoding(remote);
        if (ftpClient.changeWorkingDirectory(remote)) {
            return true;
        }
        String[] dirs = remote.split("/");
        for (String dir : dirs) {
            if (!StringUtils.isEmpty(dir) && !ftpClient.changeWorkingDirectory(dir)) {
                if (ftpClient.makeDirectory(dir)) {
                    if (!ftpClient.changeWorkingDirectory(dir)) {
                        LOG.error("切换目录[{}]失败，ftp返回状态： {}", dir, ftpClient.getReplyString());
                        return false;
                    }
                } else {
                    LOG.error("创建目录[{}]失败，ftp返回状态： {}", dir, ftpClient.getReplyString());
                    return false;
                }
            }
        }
        return true;
    }

    private String transferFtpEncoding(String source) {
        return new String(source.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
    }

    private String generateFtpFilePath(String remotePath, String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return null;
        }
        if (StringUtils.isEmpty(remotePath)) {
            remotePath = "/";
        } else {
            if (!remotePath.startsWith("/")) {
                remotePath = "/" + remotePath;
            }
            if (!remotePath.endsWith("/")) {
                remotePath = remotePath + "/";
            }
        }
        return String.format("%s%s%s", ftpHost, remotePath, fileName);
    }


    private FTPClient getFtpClient() throws Exception {
        return ftpClientObjectPool.borrowObject();
    }

    private void returnFtpClient(FTPClient ftpClient) throws Exception {
        ftpClientObjectPool.returnObject(ftpClient);
    }
}

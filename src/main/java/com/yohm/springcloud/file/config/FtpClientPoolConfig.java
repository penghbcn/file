package com.yohm.springcloud.file.config;

import com.yohm.springcloud.file.annotation.FtpClientPool;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class FtpClientPoolConfig {
    private static final Logger LOG = LoggerFactory.getLogger(FtpClientPoolConfig.class);
    @Autowired
    FtpClientFactory ftpClientFactory;

    @Bean
    @FtpClientPool
    public ObjectPool<FTPClient> getFtpClientPool() {
        FtpConfigBean ftpConfigBean = ftpClientFactory.ftpConfigBean;
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(ftpConfigBean.maxTotal);
        config.setMaxIdle(ftpConfigBean.maxIdle);
        config.setMinIdle(ftpConfigBean.minIdle);
        config.setTestOnBorrow(ftpConfigBean.testOnBorrow);
        config.setJmxEnabled(false);

        LOG.info("ftpClient连接池初始化完成." +
                "ftp服务器地址为[{}],ftp账号为[{}],maxTotal=[{}],maxIdle=[{}],minIdle=[{}],testOnBorrow=[{}]",
                ftpConfigBean.host,
                ftpConfigBean.username,
                ftpConfigBean.maxTotal,
                ftpConfigBean.maxIdle,
                ftpConfigBean.minIdle,
                ftpConfigBean.testOnBorrow);

        return new GenericObjectPool<FTPClient>(ftpClientFactory, config);
    }

    @Component
    static class FtpClientFactory implements PooledObjectFactory<FTPClient> {
        @Autowired
        FtpConfigBean ftpConfigBean;

        @Override
        public PooledObject<FTPClient> makeObject() throws Exception {
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(ftpConfigBean.host, ftpConfigBean.port);
            ftpClient.login(ftpConfigBean.username, ftpConfigBean.password);

            // 设置中文编码集，防止中文乱码
            ftpClient.setControlEncoding(StandardCharsets.UTF_8.name());
            // 设置PassiveMode传输
            ftpClient.enterLocalPassiveMode();
            // 设置二进制传输，使用BINARY_FILE_TYPE，ASCII容易造成文件损坏
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                LOG.error("连接到FTP失败.{}",ftpClient.getReplyString());
                ftpClient.disconnect();
                return null;
            }
            return new DefaultPooledObject(ftpClient);
        }

        @Override
        public void destroyObject(PooledObject<FTPClient> pooledObject) throws Exception {
            FTPClient ftpClient = pooledObject.getObject();
            if (ftpClient.isConnected()) {
                ftpClient.logout();
            }
            ftpClient.disconnect();
        }

        @Override
        public boolean validateObject(PooledObject<FTPClient> pooledObject) {
            FTPClient ftpClient = pooledObject.getObject();
            try {
                return ftpClient.sendNoOp();
            } catch (IOException e) {
                LOG.warn("ftpClient校验失败，失败信息为：{}", e.getMessage());
            }
            return false;
        }

        @Override
        public void activateObject(PooledObject<FTPClient> pooledObject) throws Exception {
            changeWorkingDirectoryToRoot(pooledObject);
        }

        private void changeWorkingDirectoryToRoot(PooledObject<FTPClient> pooledObject) throws IOException {
            FTPClient ftpClient = pooledObject.getObject();
            ftpClient.changeWorkingDirectory("/");
        }

        @Override
        public void passivateObject(PooledObject<FTPClient> pooledObject) throws Exception {
            changeWorkingDirectoryToRoot(pooledObject);
        }
    }

    @Configuration
    static class FtpConfigBean {
        @Value("${ftp.host}")
        private String host;
        @Value("${ftp.port}")
        private int port;
        @Value("${ftp.username}")
        private String username;
        @Value("${ftp.password}")
        private String password;
        @Value("${ftp.pool.max-total}")
        private int maxTotal;
        @Value("${ftp.pool.max-idle}")
        private int maxIdle;
        @Value("${ftp.pool.min-idle}")
        private int minIdle;
        @Value("${ftp.pool.test-on-borrow}")
        private boolean testOnBorrow;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getMaxTotal() {
            return maxTotal;
        }

        public void setMaxTotal(int maxTotal) {
            this.maxTotal = maxTotal;
        }

        public int getMaxIdle() {
            return maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public boolean getTestOnBorrow() {
            return testOnBorrow;
        }

        public void setTestOnBorrow(boolean testOnBorrow) {
            this.testOnBorrow = testOnBorrow;
        }

    }
}

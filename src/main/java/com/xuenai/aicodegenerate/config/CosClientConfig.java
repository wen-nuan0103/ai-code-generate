package com.xuenai.aicodegenerate.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "cos.client")
@Configuration
public class CosClientConfig {
    
    private String host;
    private String secretId;
    private String secretKey;
    private String region;
    private String bucket;
    
    @Bean   
    public COSClient cosClient() {
        BasicCOSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig config = new ClientConfig(new Region(region));
        return new COSClient(credentials, config);
    }
}

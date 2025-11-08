package com.xuenai.aicodegenerate.config;

import com.xuenai.aicodegenerate.custom.CustomRedisChatMemoryStore;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(value = "spring.data.redis")
@Configuration
public class RedisChatMemoryStoreConfig {

    private String host;

    private int port;

    private int db;

    private String prefix = "";

    private String username;

    private String password;

    private long ttl;

    @Bean
    public CustomRedisChatMemoryStore customRedisChatMemoryStore() {
        return CustomRedisChatMemoryStore.builder().host(host).port(port).username(username).password(password).db(db).prefix(prefix).ttl(ttl).build();
    }

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        return RedisChatMemoryStore.builder().host(host).port(port).password(password).ttl(ttl).build();
    }
}

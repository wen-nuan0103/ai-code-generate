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
        CustomRedisChatMemoryStore.CustomRedisChatMemoryStoreBuilder builder = CustomRedisChatMemoryStore.builder().host(host).port(port).username(username).db(db).prefix(prefix).ttl(ttl);
        if (password != null && !password.isEmpty()) {
            builder.password(password);
        }
        return builder.build();
    }

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        RedisChatMemoryStore.Builder builder = RedisChatMemoryStore.builder().host(host).port(port).ttl(ttl);
        if (password != null && !password.isEmpty()) {
            builder.password(password);
        }
        return builder.build();
    }
}

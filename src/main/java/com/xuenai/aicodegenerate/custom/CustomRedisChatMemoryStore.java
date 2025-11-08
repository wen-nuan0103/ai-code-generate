package com.xuenai.aicodegenerate.custom;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStoreException;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 持用户名、密码与数据库选择的 Redis ChatMemoryStore。
 * 使用 Jedis 连接池（线程安全、性能优）。
 */
@Slf4j
public class CustomRedisChatMemoryStore implements ChatMemoryStore {

    /**
     * Redis 主机名
     */
    private final String host;

    /**
     * Redis 端口
     */
    private final int port;

    /**
     * Redis 用户名（可为空）
     */
    private final String username;

    /**
     * Redis 密码（可为空）
     */
    private final String password;

    /**
     * Redis 数据库编号（默认 1）
     */
    private final int db;

    /**
     * Key 前缀，用于逻辑区分不同应用数据
     */
    private final String prefix;

    /**
     * 数据过期时间（秒），为 0 表示永久保存
     */
    private final Long ttl;

    /**
     * Jedis 连接池
     */
    private final JedisPool pool;

    @Builder
    public CustomRedisChatMemoryStore(String host, int port, String username, String password, int db, String prefix, Long ttl, JedisPool pool) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.db = db;
        this.prefix = prefix == null ? "" : prefix;
        this.ttl = ttl == null ? 0L : ttl;


        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(50);
        config.setMaxIdle(10);
        config.setMinIdle(2);

        if (username != null && password != null) {
            this.pool = new JedisPool(config, host, port, 5000, username, password);
        } else if (password != null) {
            this.pool = new JedisPool(config, host, port, 5000, password);
        } else {
            this.pool = new JedisPool(config, host, port);
        }
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = prefix + toMemoryId(memoryId);
        try (Jedis jedis = pool.getResource()) {
            jedis.select(db);
            String json = jedis.get(key);
            if (json == null) {
                return new ArrayList<>();
            }
            return ChatMessageDeserializer.messagesFromJson(json);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = prefix + toMemoryId(memoryId);
        String json = ChatMessageSerializer.messagesToJson(messages);
        try (Jedis jedis = pool.getResource()) {
            jedis.select(db);
            if (ttl > 0) {
                jedis.setex(key, ttl.intValue(), json);
            } else {
                jedis.set(key, json);
            }
        } catch (Exception e) {
            throw new RedisChatMemoryStoreException("Set memory error, msg=" + e.getMessage());
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = prefix + toMemoryId(memoryId);
        try (Jedis jedis = pool.getResource()) {
            jedis.select(db);
            jedis.del(key);
        } catch (Exception e) {

        }
    }

    private String toMemoryId(Object memoryId) {
        if (memoryId == null || memoryId.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("memoryId cannot be null or empty");
        }
        return memoryId.toString();
    }
}

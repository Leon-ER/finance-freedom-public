package finance.freedom.finance_freedom_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class RedisConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {

        ObjectMapper redisObjectMapper = new ObjectMapper();
        redisObjectMapper.registerModule(new JavaTimeModule());
        redisObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        redisObjectMapper.activateDefaultTyping(
                redisObjectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(redisObjectMapper)
                        )
                );
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheBuilderCustomizer(RedisCacheConfiguration redisCacheConfiguration) {
        return (builder) -> builder
                .withCacheConfiguration("transaction", redisCacheConfiguration.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("budget", redisCacheConfiguration.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("savingGoal", redisCacheConfiguration.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("linkedAccount", redisCacheConfiguration.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("user", redisCacheConfiguration.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("aws-secret", redisCacheConfiguration.entryTtl(Duration.ofMinutes(60)));
    }
}

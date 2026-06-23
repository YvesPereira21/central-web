package io.centralweb.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.io.IOException;
import java.util.List;

@Configuration
@SuppressWarnings({"deprecation", "removal"})
public class RedisConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        SimpleModule pageModule = new SimpleModule();
        pageModule.addDeserializer(PageImpl.class, new PageDeserializer());
        pageModule.addDeserializer(Page.class, new JsonDeserializer<Page<?>>() {
            private final PageDeserializer delegate = new PageDeserializer();
            @Override
            public Page<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return delegate.deserialize(p, ctxt);
            }
        });
        objectMapper.registerModule(pageModule);

        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, 
            ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)));
    }

    public static class PageDeserializer extends JsonDeserializer<PageImpl<?>> {
        private static final ObjectMapper plainMapper = new ObjectMapper();

        @Override
        public PageImpl<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = plainMapper.readTree(p);
            JsonParser contentParser = node.get("content").traverse(p.getCodec());
            List<?> content = contentParser.readValueAs(new TypeReference<List<Object>>() {});
            int number = node.has("number") ? node.get("number").asInt() : 0;
            int size = node.has("size") ? Math.max(1, node.get("size").asInt()) : 10;
            long totalElements = node.has("totalElements") ? node.get("totalElements").asLong() : content.size();
            return new PageImpl<>(content, PageRequest.of(number, size), totalElements);
        }
    }
}

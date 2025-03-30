package com.example.project.demo_bc_xfin_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.client.RestTemplate;
import com.example.project.demo_bc_xfin_service.codewave.RedisManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class AppConfig {
  @Bean
  RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  RedisManager redisManager(RedisConnectionFactory factory,
      ObjectMapper objectMapper) {
    return new RedisManager(factory, objectMapper);
  }

  // @Bean
  // ObjectMapper objectMapper() {
  //   return new ObjectMapper();
  // }


    @Bean
    public ObjectMapper objectMapper() {
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      return mapper;
    }


}

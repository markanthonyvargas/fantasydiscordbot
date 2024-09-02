package com.markvargas.discordbot.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfiguration {

  @Bean
  @Qualifier("yahooRestTemplate")
  public RestTemplate yahooRestTemplate() {
    return new RestTemplate();
  }
}

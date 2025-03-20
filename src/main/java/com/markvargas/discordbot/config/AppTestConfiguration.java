package com.markvargas.discordbot.config;

import discord4j.core.GatewayDiscordClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Configuration
@Profile("test")
public class AppTestConfiguration {

  @Bean
  @Qualifier("yahooRestClient")
  public RestClient yahooRestClient() {
    return RestClient.builder().build();
  }

  @Bean
  @Qualifier("yahooAuthTokenRestClient")
  public RestClient yahooAuthTokenRestClient() {
    return RestClient.builder().build();
  }

  @Bean
  public GatewayDiscordClient discordClient() {
    return null;
  }
}

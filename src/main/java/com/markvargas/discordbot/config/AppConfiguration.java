package com.markvargas.discordbot.config;

import com.markvargas.discordbot.client.yahoo.service.YahooService;
import java.util.Base64;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfiguration {

  @Value("${clientId}")
  private String clientId;

  @Value("${clientSecret}")
  private String clientSecret;

  @Value("${channelId}")
  private String channelId;

  @Value("${botToken}")
  private String botToken;

  @Bean
  @Qualifier("yahooRestTemplate")
  public RestTemplate yahooRestTemplate() {
    return new RestTemplate();
  }

  @Bean
  @Qualifier("yahooRestClient")
  public RestClient yahooRestClient() {
    return RestClient.builder()
        .baseUrl("https://fantasysports.yahooapis.com")
        .defaultHeader("Authorization", "Bearer " + YahooService.getAuthToken())
        .build();
  }

  @Bean
  @Qualifier("yahooAuthTokenRestClient")
  public RestClient yahooAuthTokenRestClient() {
    byte[] encodedClientCredentials =
        Base64.getEncoder().encode((clientId + ":" + clientSecret).getBytes());
    return RestClient.builder()
        .baseUrl("https://api.login.yahoo.com/oauth2/get_token")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedClientCredentials))
        .build();
  }

  @Bean
  @Qualifier("discordRestClient")
  public RestClient discordRestClient() {
    return RestClient.builder().baseUrl("https://discord.com/api/v10/channels/" + channelId + "/messages").build();
  }

  @Bean
  @Qualifier("discordRestTemplate")
  public RestTemplate discordRestTemplate() {
    return new RestTemplate();
  }
}

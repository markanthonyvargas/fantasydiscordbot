package com.markvargas.discordbot.config;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import java.util.Base64;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestClient;

@Configuration
@EnableRetry
@Profile("!test")
public class AppConfiguration {

  @Value("${clientId}")
  private String clientId;

  @Value("${clientSecret}")
  private String clientSecret;

  @Value("${botToken}")
  private String botToken;

  @Bean
  @Qualifier("yahooRestClient")
  public RestClient yahooRestClient() {
    return RestClient.builder().baseUrl("https://fantasysports.yahooapis.com").build();
  }

  @Bean
  @Qualifier("yahooAuthTokenRestClient")
  public RestClient yahooAuthTokenRestClient() {
    String encodedClientCredentials =
        new String(Base64.getEncoder().encode((clientId + ":" + clientSecret).getBytes()));
    Consumer<HttpHeaders> headersConsumer =
        httpHeaders -> {
          httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
          httpHeaders.setBasicAuth(encodedClientCredentials);
        };
    return RestClient.builder()
        .baseUrl("https://api.login.yahoo.com/oauth2/get_token")
        .defaultHeaders(headersConsumer)
        .build();
  }

  @Bean
  public GatewayDiscordClient discordClient() {
    return DiscordClientBuilder.create(botToken).build().login().block();
  }
}

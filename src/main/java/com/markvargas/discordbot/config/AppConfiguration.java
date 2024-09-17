package com.markvargas.discordbot.config;

import java.util.Base64;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestClient;

@Configuration
@EnableRetry
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
  @Qualifier("discordRestClient")
  public RestClient discordRestClient() {
    Consumer<HttpHeaders> headersConsumer =
        httpHeaders -> {
          httpHeaders.setContentType(MediaType.APPLICATION_JSON);
          httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bot " + botToken);
          httpHeaders.set(HttpHeaders.USER_AGENT, "DiscordBot (fantasydiscordbot.onrender.com, 1");
        };
    return RestClient.builder()
        .baseUrl("https://discord.com/api/v10/channels/" + channelId + "/messages")
        .defaultHeaders(headersConsumer)
        .build();
  }
}

package com.markvargas.discordbot.client.yahoo.model;

import lombok.Data;

@Data
public class YahooAuthToken {

  private String access_token;
  private String refresh_token;
  private int expires_in;
  private String token_type;
}

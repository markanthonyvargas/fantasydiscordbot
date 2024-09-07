package com.markvargas.discordbot.client.yahoo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class League {

  private Scoreboard scoreboard;
  private Standings standings;
  private Transaction[] transactions;
  private Team[] teams;
  private int current_week;
}

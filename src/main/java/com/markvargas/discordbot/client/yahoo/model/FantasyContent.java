package com.markvargas.discordbot.client.yahoo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FantasyContent {

  private League league;
  private Team[] teams;
  private League[] leagues;
}

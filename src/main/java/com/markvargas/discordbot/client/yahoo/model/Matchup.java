package com.markvargas.discordbot.client.yahoo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Matchup {

  private Team[] teams;
}

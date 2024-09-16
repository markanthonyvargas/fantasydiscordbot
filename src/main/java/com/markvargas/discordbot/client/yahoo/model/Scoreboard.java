package com.markvargas.discordbot.client.yahoo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Scoreboard {

  private Matchup[] matchups;
  private HashMap<String, Game> games;
}

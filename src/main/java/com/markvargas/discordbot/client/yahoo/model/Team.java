package com.markvargas.discordbot.client.yahoo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Team {

  private String team_key;
  private String name;
  private TeamPoints team_points;
  private TeamProjectedPoints team_projected_points;
  private TeamStandings team_standings;
  private Roster roster;
  private double win_probability;
}

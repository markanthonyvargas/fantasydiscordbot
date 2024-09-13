package com.markvargas.discordbot.client.yahoo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Team implements Comparable<Team> {

  private String team_key;
  private String name;
  private TeamPoints team_points;
  private TeamProjectedPoints team_projected_points;
  private TeamStandings team_standings;
  private Roster roster;
  private double win_probability;

  public String getName() {
    return this.name.replace("_", "\\_");
  }

  @Override
  public int compareTo(Team b) {
    return Double.compare(this.getTeam_points().getTotal(), b.getTeam_points().getTotal());
  }
}

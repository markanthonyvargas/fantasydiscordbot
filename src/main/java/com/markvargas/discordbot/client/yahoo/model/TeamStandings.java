package com.markvargas.discordbot.client.yahoo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamStandings {

  private int rank;
  private OutcomeTotals outcome_totals;
  private double points_for;
}

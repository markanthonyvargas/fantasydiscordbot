package com.markvargas.discordbot.client.yahoo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamProjectedPoints {

  private double total;

  @Override
  public String toString() {
    return Double.toString(this.total);
  }
}

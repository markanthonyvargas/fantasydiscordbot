package com.markvargas.discordbot.client.yahoo.model;

import lombok.Data;

@Data
public class OutcomeTotals {

  private int wins;
  private int losses;
  private int ties;
  private double percentage;
}

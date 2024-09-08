package com.markvargas.discordbot.client.yahoo.model;

import java.util.Comparator;

public class PlayerComparator implements Comparator<Player> {

  @Override
  public int compare(Player a, Player b) {
    return Double.compare(a.getPlayer_points().getTotal(), b.getPlayer_points().getTotal());
  }
}

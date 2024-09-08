package com.markvargas.discordbot.client.yahoo.service;

import com.markvargas.discordbot.client.yahoo.model.Matchup;
import com.markvargas.discordbot.client.yahoo.model.Team;
import java.util.HashMap;
import java.util.Map;

public class TrophyHelper {

  public static Map<String, Team> getHighAndLowScores(Matchup[] matchups) {
    Map<String, Team> teamAndScore = new HashMap<>();
    Team highScoreTeam = new Team();
    Team lowScoreTeam = new Team();
    double highScore = 0;
    double lowScore = 1000;
    for (Matchup matchup : matchups) {
      Team team1 = matchup.getTeams()[0];
      Team team2 = matchup.getTeams()[1];
      if (team1.getTeam_points().getTotal() >= highScore) {
        highScoreTeam = team1;
        highScore = team1.getTeam_points().getTotal();
      }
      if (team2.getTeam_points().getTotal() >= highScore) {
        highScoreTeam = team2;
        highScore = team2.getTeam_points().getTotal();
      }
      if (team1.getTeam_points().getTotal() <= lowScore) {
        lowScoreTeam = team1;
        lowScore = team1.getTeam_points().getTotal();
      }
      if (team2.getTeam_points().getTotal() <= lowScore) {
        lowScoreTeam = team2;
        lowScore = team2.getTeam_points().getTotal();
      }
    }
    teamAndScore.put("highScore", highScoreTeam);
    teamAndScore.put("lowScore", lowScoreTeam);
    return teamAndScore;
  }

  public static Map<String, Team> getBlowout(Matchup[] matchups) {
    double blowoutMargin = 0.0;
    Team blowoutTeam = new Team();
    Team losingTeam = new Team();

    for (Matchup matchup : matchups) {
      Team team1 = matchup.getTeams()[0];
      Team team2 = matchup.getTeams()[1];
      if (Math.abs(team1.getTeam_points().getTotal() - team2.getTeam_points().getTotal())
          >= blowoutMargin) {
        blowoutTeam =
            team1.getTeam_points().getTotal() >= team2.getTeam_points().getTotal() ? team1 : team2;
        losingTeam =
            team1.getTeam_points().getTotal() <= team2.getTeam_points().getTotal() ? team1 : team2;
        blowoutMargin =
            Math.abs(team1.getTeam_points().getTotal() - team2.getTeam_points().getTotal());
      }
    }

    Map<String, Team> blowOut = new HashMap<>();
    blowOut.put("winningTeam", blowoutTeam);
    blowOut.put("losingTeam", losingTeam);
    return blowOut;
  }

  public static Map<String, Team> getCloseWin(Matchup[] matchups) {
    double winMargin = 1000;
    Team blowoutTeam = new Team();
    Team losingTeam = new Team();

    for (Matchup matchup : matchups) {
      Team team1 = matchup.getTeams()[0];
      Team team2 = matchup.getTeams()[1];
      if (Math.abs(team1.getTeam_points().getTotal() - team2.getTeam_points().getTotal())
          <= winMargin) {
        blowoutTeam =
            team1.getTeam_points().getTotal() >= team2.getTeam_points().getTotal() ? team1 : team2;
        losingTeam =
            team1.getTeam_points().getTotal() <= team2.getTeam_points().getTotal() ? team1 : team2;
        winMargin = Math.abs(team1.getTeam_points().getTotal() - team2.getTeam_points().getTotal());
      }
    }

    Map<String, Team> blowOut = new HashMap<>();
    blowOut.put("winningTeam", blowoutTeam);
    blowOut.put("losingTeam", losingTeam);
    return blowOut;
  }

  public static String[] getLuckyTeam(Team[] teams) {
    Map<String, Double> weeklyScores = new HashMap<>();
    for (Team team : teams) {
      weeklyScores.put(team.getName(), team.getTeam_points().getTotal());
    }

    Team luckyTeam = new Team();
    int wins = 1000;
    for (Team team : teams) {
      int teamWins = 0;
      for (Map.Entry<String, Double> entry : weeklyScores.entrySet()) {
        if (!team.getName().equals(entry.getKey())
            && team.getTeam_points().getTotal() > entry.getValue()) {
          teamWins++;
        }
      }
      if (teamWins <= wins) {
        luckyTeam = team;
        wins = teamWins;
      }
    }

    return new String[] {luckyTeam.getName(), Integer.toString(wins)};
  }
}

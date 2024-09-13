package com.markvargas.discordbot.client.yahoo.service;

import static com.markvargas.discordbot.client.yahoo.service.YahooService.getAuthToken;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.markvargas.discordbot.client.yahoo.model.*;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
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

  public static String[] getLuckyTeam(Team[] teams, Matchup[] matchups) {
    List<String> winningTeams = new ArrayList<>();

    for (Matchup matchup : matchups) {
      winningTeams.add(matchup.getWinner_team_key());
    }

    Team luckyTeam = new Team();
    Arrays.sort(teams);
    int wins = 0;

    for (int i = 0; i < teams.length; i++) {
      if (winningTeams.contains(teams[i].getTeam_key())) {
        wins = i;
        luckyTeam = teams[i];
        break;
      }
    }

    return new String[] {luckyTeam.getName(), Integer.toString(wins)};
  }

  public static String[] getUnluckyTeam(Team[] teams, Matchup[] matchups) {
    List<String> winningTeams = new ArrayList<>();

    for (Matchup matchup : matchups) {
      winningTeams.add(matchup.getWinner_team_key());
    }

    Team unluckyTeam = new Team();
    Arrays.sort(teams);
    Collections.reverse(Arrays.asList(teams));
    int wins = 0;

    for (int i = 0; i < teams.length; i++) {
      if (!winningTeams.contains(teams[i].getTeam_key())) {
        wins = 11 - i;
        unluckyTeam = teams[i];
        break;
      }
    }

    return new String[] {unluckyTeam.getName(), Integer.toString(wins)};
  }

  public static Team getOverachiever(Team[] teams) {
    Team overachiever = null;
    double pointsOverProjection = 0.0;

    for (Team team : teams) {
      if (team.getTeam_points().getTotal() - team.getTeam_projected_points().getTotal()
          > pointsOverProjection) {
        overachiever = team;
        pointsOverProjection =
            team.getTeam_points().getTotal() - team.getTeam_projected_points().getTotal();
      }
    }

    return overachiever;
  }

  public static Team getUnderachiever(Team[] teams) {
    Team underachiever = null;
    double pointsOverProjection = 1000.0;

    for (Team team : teams) {
      if (team.getTeam_points().getTotal() - team.getTeam_projected_points().getTotal()
          < pointsOverProjection) {
        underachiever = team;
        pointsOverProjection =
            team.getTeam_points().getTotal() - team.getTeam_projected_points().getTotal();
      }
    }

    return underachiever;
  }

  public static String[] getBestAndWorstManager(
      Team[] teams, Team[] weeklyScores, int currentWeek, String leagueId) {
    String getPlayerWeekStatsUrl =
        "https://fantasysports.yahooapis.com/fantasy/v2/league/449.l."
            + leagueId
            + "/players;player_keys=";
    String getPlayerWeekStatsUri = "/stats;type=week;week=" + currentWeek;
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken());
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    String[] bestAndWorstManager = new String[5];
    String bestManagerTeamName = "";
    String worstManagerTeamName = "";
    double optimalPointPercentage = 0.0;
    double worstPointPercentage = 1000.0;
    double worstPointDifference = 0.0;
    int index = 0;

    for (Team team : teams) {
      Player[] players = team.getRoster().getPlayers();
      StringBuilder sb = new StringBuilder();
      for (Player player : players) {
        sb.append(player.getPlayer_key()).append(",");
      }

      String playerKeys = sb.toString();
      playerKeys = playerKeys.substring(0, playerKeys.length() - 1);
      String finalUrl = getPlayerWeekStatsUrl + playerKeys + getPlayerWeekStatsUri;
      RestTemplate yahooRestTemplate = new RestTemplate();
      ResponseEntity<String> responseEntity =
          yahooRestTemplate.exchange(finalUrl, HttpMethod.GET, entity, String.class);
      XmlMapper xmlMapper = new XmlMapper();
      FantasyContent fantasyContent = null;
      try {
        fantasyContent = xmlMapper.readValue(responseEntity.getBody(), FantasyContent.class);
      } catch (Exception e) {
        log.error("Error while attempting to get weekly scores for {}", team.getName());
      }
      if (fantasyContent != null) {
        List<Player> playersWithWeeklyScores =
            new LinkedList<>(Arrays.asList(fantasyContent.getLeague().getPlayers()));
        List<Player> optimalLineup = new ArrayList<>();

        List<Player> qbs = getPlayersByPosition("QB", playersWithWeeklyScores);
        PlayerComparator playerComparator = new PlayerComparator();
        qbs.sort(playerComparator);
        Collections.reverse(qbs);
        optimalLineup.add(qbs.get(0));
        playersWithWeeklyScores.remove(qbs.get(0));

        List<Player> wrs = getPlayersByPosition("WR", playersWithWeeklyScores);
        wrs.sort(playerComparator);
        Collections.reverse(wrs);
        optimalLineup.add(wrs.get(0));
        optimalLineup.add(wrs.get(1));
        playersWithWeeklyScores.remove(wrs.get(0));
        playersWithWeeklyScores.remove(wrs.get(1));

        List<Player> rbs = getPlayersByPosition("RB", playersWithWeeklyScores);
        rbs.sort(playerComparator);
        Collections.reverse(rbs);
        optimalLineup.add(rbs.get(0));
        optimalLineup.add(rbs.get(1));
        playersWithWeeklyScores.remove(rbs.get(0));
        playersWithWeeklyScores.remove(rbs.get(1));

        List<Player> tes = getPlayersByPosition("TE", playersWithWeeklyScores);
        tes.sort(playerComparator);
        Collections.reverse(tes);
        optimalLineup.add(tes.get(0));
        playersWithWeeklyScores.remove(tes.get(0));

        List<Player> flex = getPlayersByPosition("W/R/T", playersWithWeeklyScores);
        flex.sort(playerComparator);
        Collections.reverse(flex);
        optimalLineup.add(flex.get(0));
        playersWithWeeklyScores.remove(flex.get(0));

        List<Player> kickers = getPlayersByPosition("K", playersWithWeeklyScores);
        kickers.sort(playerComparator);
        Collections.reverse(kickers);
        optimalLineup.add(kickers.get(0));
        playersWithWeeklyScores.remove(kickers.get(0));

        List<Player> defense = getPlayersByPosition("DEF", playersWithWeeklyScores);
        defense.sort(playerComparator);
        Collections.reverse(defense);
        optimalLineup.add(defense.get(0));
        playersWithWeeklyScores.remove(defense.get(0));

        double optimalScore = 0.0;
        for (Player player : optimalLineup) {
          optimalScore += player.getPlayer_points().getTotal();
        }

        double currentTeamOptimalScorePercentage =
            weeklyScores[index].getTeam_points().getTotal() / optimalScore;

        if (currentTeamOptimalScorePercentage > optimalPointPercentage) {
          optimalPointPercentage = currentTeamOptimalScorePercentage;
          bestManagerTeamName = team.getName();
        }
        if (currentTeamOptimalScorePercentage < worstPointPercentage) {
          worstPointPercentage = currentTeamOptimalScorePercentage;
          worstManagerTeamName = team.getName();
          worstPointDifference = optimalScore - weeklyScores[index].getTeam_points().getTotal();
        }
      }
      index++;
    }
    bestAndWorstManager[0] = bestManagerTeamName;
    bestAndWorstManager[1] = Double.toString(optimalPointPercentage * 100);
    bestAndWorstManager[2] = worstManagerTeamName;
    bestAndWorstManager[3] = Double.toString(worstPointPercentage * 100);
    bestAndWorstManager[4] = Double.toString(worstPointDifference);

    return bestAndWorstManager;
  }

  private static List<Player> getPlayersByPosition(String position, List<Player> players) {
    List<Player> playersByPosition = new ArrayList<>();
    for (Player player : players) {
      if (player.getEligible_positions().contains(position)) {
        playersByPosition.add(player);
      }
    }
    return playersByPosition;
  }
}

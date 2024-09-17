package com.markvargas.discordbot.client.yahoo.service;

import static com.markvargas.discordbot.client.yahoo.service.YahooService.getAuthToken;

import com.fasterxml.jackson.core.JsonProcessingException;
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

  public static Map<String, Team> getHighAndLowScores(Team[] teams) {
    Map<String, Team> teamAndScore = new HashMap<>();
    Arrays.sort(teams);
    teamAndScore.put("highScore", teams[teams.length - 1]);
    teamAndScore.put("lowScore", teams[0]);
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

  public static String[] getBestAndWorstManager(Team[] weeklyScores, int currentWeek)
      throws JsonProcessingException {
    String getPlayerWeekStatsUrl = "https://fantasysports.yahooapis.com/fantasy/v2/team/";
    String getPlayerWeekStatsUri =
        "/roster;type=week;week=" + currentWeek + "/players/stats;type=week;week=" + currentWeek;
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken());
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    String[] bestAndWorstManager = new String[5];
    String bestManagerTeamName = "";
    String worstManagerTeamName = "";
    double optimalPointPercentage = 0.0;
    double worstPointPercentage = 1000.0;
    double worstPointDifference = 0.0;
    XmlMapper xmlMapper = new XmlMapper();
    RestTemplate yahooRestTemplate = new RestTemplate();

    for (Team team : weeklyScores) {
      ResponseEntity<String> response =
          yahooRestTemplate.exchange(
              getPlayerWeekStatsUrl + team.getTeam_key() + getPlayerWeekStatsUri,
              HttpMethod.GET,
              entity,
              String.class);
      FantasyContent fantasyContent;
      fantasyContent = xmlMapper.readValue(response.getBody(), FantasyContent.class);
      List<Player> playersWithWeeklyScores =
          new LinkedList<>(Arrays.asList(fantasyContent.getTeam().getRoster().getPlayers()));
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

      double currentTeamOptimalScorePercentage = team.getTeam_points().getTotal() / optimalScore;

      if (currentTeamOptimalScorePercentage > optimalPointPercentage) {
        optimalPointPercentage = currentTeamOptimalScorePercentage;
        bestManagerTeamName = team.getName();
      }
      if (currentTeamOptimalScorePercentage < worstPointPercentage) {
        worstPointPercentage = currentTeamOptimalScorePercentage;
        worstManagerTeamName = team.getName();
        worstPointDifference = optimalScore - team.getTeam_points().getTotal();
      }
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

package com.markvargas.discordbot.client.yahoo.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.markvargas.discordbot.client.yahoo.model.FantasyContent;
import com.markvargas.discordbot.client.yahoo.model.Game;
import com.markvargas.discordbot.client.yahoo.model.Matchup;
import com.markvargas.discordbot.client.yahoo.model.Player;
import com.markvargas.discordbot.client.yahoo.model.ScoreboardResponse;
import com.markvargas.discordbot.client.yahoo.model.Team;
import com.markvargas.discordbot.client.yahoo.model.Transaction;
import com.markvargas.discordbot.client.yahoo.model.YahooAuthToken;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class YahooService {

  @Autowired
  @Qualifier("yahooRestTemplate")
  private RestTemplate yahooRestTemplate;

  @Value("${refreshToken}")
  private String refreshToken;

  @Value("${clientId}")
  private String clientId;

  @Value("${clientSecret}")
  private String clientSecret;

  @Value("${leagueId}")
  private String leagueId;

  @Value("${redirecUri")
  private String redirectUri;

  private static final DecimalFormat df = new DecimalFormat("0.00");

  public void saveAuthToken() {
    String authUrl = "https://api.login.yahoo.com/oauth2/get_token";
    String requestBody =
        "redirect_uri="
            + redirectUri
            + "&refresh_token="
            + refreshToken
            + "&grant_type=refresh_token";
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    byte[] encodedClientCredentials =
        Base64.getEncoder().encode((clientId + ":" + clientSecret).getBytes());
    headers.add(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedClientCredentials));
    HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
    try {
      log.info("Attempting to get access token from Yahoo...");
      YahooAuthToken authToken =
          yahooRestTemplate.postForObject(authUrl, request, YahooAuthToken.class);
      log.info("Yahoo access token retrieval successful");
      File tokenFile = new File("./app/token.txt");
      if (tokenFile.createNewFile()) {
        log.info("File created: {}", tokenFile.getName());
      } else {
        log.info("File {} already exists", tokenFile.getName());
      }
      FileWriter fileWriter = new FileWriter(tokenFile);
      fileWriter.write(authToken.getAccess_token());
      fileWriter.close();
    } catch (Exception e) {
      log.error("Unable to get access token from Yahoo due to", e);
    }
  }

  public static String getAuthToken() {
    StringBuilder token = new StringBuilder();
    try {
      File file = new File("./app/token.txt");
      Scanner scanner = new Scanner(file);
      while (scanner.hasNextLine()) {
        token.append(scanner.nextLine());
      }
      scanner.close();
      return token.toString();
    } catch (Exception e) {
      log.error("Could not read token from file due to", e);
      return "";
    }
  }

  public String getMatchups() {
    String url =
        "https://fantasysports.yahooapis.com/fantasy/v2/league/449.l." + leagueId + "/scoreboard";
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken());
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    try {
      log.info("Attempting to get matchups");
      ResponseEntity<String> response =
          yahooRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      XmlMapper xmlMapper = new XmlMapper();
      FantasyContent fantasyContent = xmlMapper.readValue(response.getBody(), FantasyContent.class);
      StringBuilder matchups = new StringBuilder();
      matchups.append("**Matchups**\n");
      StringBuilder projectedScores = new StringBuilder();
      projectedScores.append("Approximate Projected Scores\n");
      for (Matchup matchup : fantasyContent.getLeague().getScoreboard().getMatchups()) {
        Team team1 = matchup.getTeams()[0];
        Team team2 = matchup.getTeams()[1];
        matchups.append(team1.getName()).append(" vs ").append(team2.getName()).append("\n");
        projectedScores
            .append(team1.getName())
            .append(" ")
            .append(team1.getTeam_projected_points().getTotal())
            .append(" - ")
            .append(team2.getTeam_projected_points().getTotal())
            .append(" ")
            .append(team2.getName())
            .append("\n");
      }
      return matchups + "\n" + projectedScores;
    } catch (Exception e) {
      log.error("Could not get matchups due to", e);
      return "";
    }
  }

  public String getScoreUpdates(boolean isFinalUpdate) {
    String url =
        "https://fantasysports.yahooapis.com/fantasy/v2/league/449.l." + leagueId + "/scoreboard";
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken());
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    try {
      log.info("Attempting to get score updates");
      ResponseEntity<String> response =
          yahooRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      XmlMapper xmlMapper = new XmlMapper();
      FantasyContent fantasyContent = xmlMapper.readValue(response.getBody(), FantasyContent.class);
      StringBuilder currentPoints = new StringBuilder();
      if (isFinalUpdate) {
        url += ";type=week;week=" + (fantasyContent.getLeague().getCurrent_week() - 1);
        response = yahooRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        fantasyContent = xmlMapper.readValue(response.getBody(), FantasyContent.class);
      }
      currentPoints.append(isFinalUpdate ? "**Final Score Update**" : "**Score Update**").append("\n");
      StringBuilder projectedPoints = new StringBuilder();
      projectedPoints.append("**Approximate Projected Scores**").append("\n");
      for (Matchup matchup : fantasyContent.getLeague().getScoreboard().getMatchups()) {
        Team team1 = matchup.getTeams()[0];
        Team team2 = matchup.getTeams()[1];
        currentPoints
            .append(team1.getName())
            .append(" ")
            .append(team1.getTeam_points())
            .append(" - ")
            .append(team2.getTeam_points())
            .append(" ")
            .append(team2.getName())
            .append("\n");
        projectedPoints
            .append(team1.getName())
            .append(" ")
            .append(team1.getTeam_projected_points())
            .append(" - ")
            .append(team2.getTeam_projected_points())
            .append(" ")
            .append(team2.getName())
            .append("\n");
      }
      return currentPoints + "\n" + (isFinalUpdate ? "" : projectedPoints.toString());
    } catch (Exception e) {
      log.error("Could not get score updates due to", e);
      return "";
    }
  }

  public String getStandings() {
    String url =
        "https://fantasysports.yahooapis.com/fantasy/v2/league/449.l." + leagueId + "/standings";
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken());
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    try {
      log.info("Attempting to get standings");
      ResponseEntity<String> response =
          yahooRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      XmlMapper xmlMapper = new XmlMapper();
      FantasyContent fantasyContent = xmlMapper.readValue(response.getBody(), FantasyContent.class);
      StringBuilder sb = new StringBuilder();
      sb.append("**Current Standings:**").append("\n");
      int rank = 1;
      for (Team team : fantasyContent.getLeague().getStandings().getTeams()) {
        sb.append(rank)
            .append(":\t")
            .append(team.getName())
            .append(" (")
            .append(team.getTeam_standings().getOutcome_totals().getWins())
            .append("-")
            .append(team.getTeam_standings().getOutcome_totals().getLosses())
            .append(") ")
            .append(team.getTeam_standings().getPoints_for())
            .append("\n");
        rank++;
      }
      return sb.toString();
    } catch (Exception e) {
      log.error("Could not get standings due to", e);
      return "";
    }
  }

  public String getPlayersToMonitor() {
    String uri = "league/449.l." + leagueId + "/teams/roster";
    String url = "https://fantasysports.yahooapis.com/fantasy/v2/" + uri;
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken());
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    try {
      log.info("Attempting to get rosters");
      ResponseEntity<String> response =
          yahooRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      XmlMapper xmlMapper = new XmlMapper();
      FantasyContent fantasyContent = xmlMapper.readValue(response.getBody(), FantasyContent.class);

      String completedGamesUrl =
          "https://api-secure.sports.yahoo.com/v1/editorial/s/scoreboard?lang=en-US&region=US&tz=America/Chicago&ysp_redesign=1&ysp_platform=desktop&leagues=nfl&week="
              + fantasyContent.getLeague().getCurrent_week()
              + "&season=current&sched_states=2&v=2&ysp_enable_last_update=0&include_last_play=0";
      List<String> completedTeams = new ArrayList<>();
      ResponseEntity<ScoreboardResponse> scoreboardResponseEntity =
          yahooRestTemplate.exchange(
              completedGamesUrl, HttpMethod.GET, null, ScoreboardResponse.class);
      ScoreboardResponse scoreboardResponse = scoreboardResponseEntity.getBody();
      for (Map.Entry<String, Game> entry :
          scoreboardResponse.getService().getScoreboard().getGames().entrySet()) {
        if (entry.getValue().getStatus_type().equals("status.type.final")) {
          completedTeams.add(entry.getValue().getHome_team_id());
          completedTeams.add(entry.getValue().getAway_team_id());
        }
      }
      StringBuilder playersToMonitor = new StringBuilder();
      playersToMonitor.append("**Starting Players to Monitor**:\n");
      for (Team team : fantasyContent.getLeague().getTeams()) {
        List<Player> injuredPlayers = new ArrayList<>();
        for (Player player : team.getRoster().getPlayers()) {
          if (!player.getSelected_position().getPosition().equals("BN")
              && !player.getSelected_position().getPosition().equals("IR")
              && !completedTeams.contains(player.getEditorial_team_key())
              && !StringUtils.isEmpty(player.getStatus_full())) {
            injuredPlayers.add(player);
          }
        }
        if (!injuredPlayers.isEmpty()) {
          playersToMonitor.append(team.getName()).append(":\n");
          for (Player player : injuredPlayers) {
            playersToMonitor
                .append(player.getSelected_position().getPosition())
                .append(" ")
                .append(player.getName().getFull())
                .append(" - ")
                .append(player.getStatus_full())
                .append("\n");
          }
          playersToMonitor.append("\n");
        }
      }
      return playersToMonitor.toString();
    } catch (Exception e) {
      log.error("Could not get rosters due to", e);
      return "";
    }
  }

  public String getWaiverTransactions() {
    String url =
        "https://fantasysports.yahooapis.com/fantasy/v2/leagues;league_keys=449.l."
            + leagueId
            + "/transactions;type=add";
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken());
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    try {
      log.info("Attempting to get waiver report");
      ResponseEntity<String> response =
          yahooRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      XmlMapper xmlMapper = new XmlMapper();
      FantasyContent fantasyContent = xmlMapper.readValue(response.getBody(), FantasyContent.class);
      StringBuilder sb = new StringBuilder();
      sb.append("**Waiver Report:**").append("\n");
      for (Transaction transaction : fantasyContent.getLeagues()[0].getTransactions()) {
        long timestampMillis = transaction.getTimestamp() * 1000;
        Calendar today = Calendar.getInstance();
        Calendar timestamp = Calendar.getInstance();
        timestamp.setTimeInMillis(timestampMillis);
        if (today.get(Calendar.DAY_OF_YEAR) == timestamp.get(Calendar.DAY_OF_YEAR)) {
          Player[] players = transaction.getPlayers();
          if (players[0].getTransaction_data().getSource_type().equals("waivers")) {
            String teamName = players[0].getTransaction_data().getDestination_team_name();
            sb.append(teamName).append("\n");
            for (Player player : players) {
              sb.append(
                      player.getTransaction_data().getType().equals("add") ? "ADDED " : "DROPPED ")
                  .append(player.getDisplay_position())
                  .append(" ")
                  .append(player.getName().getFull())
                  .append("\n");
            }
            sb.append("\n");
          }
        }
      }
      return sb.toString();
    } catch (Exception e) {
      log.error("Could not get waiver report due to", e);
      return "";
    }
  }

  public String getTrophies() {
    String matchupsUrl =
        "https://fantasysports.yahooapis.com/fantasy/v2/league/449.l." + leagueId + "/scoreboard";
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken());
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    try {
      log.info("Attempting to get trophy information");
      ResponseEntity<String> matchupsResponseEntity =
          yahooRestTemplate.exchange(matchupsUrl, HttpMethod.GET, entity, String.class);
      XmlMapper xmlMapper = new XmlMapper();
      FantasyContent fantasyContent =
          xmlMapper.readValue(matchupsResponseEntity.getBody(), FantasyContent.class);
      matchupsUrl += ";type=week;week=" + (fantasyContent.getLeague().getCurrent_week() - 1);
      matchupsResponseEntity =
          yahooRestTemplate.exchange(matchupsUrl, HttpMethod.GET, entity, String.class);
      fantasyContent = xmlMapper.readValue(matchupsResponseEntity.getBody(), FantasyContent.class);
      String weeklyScoresUrl =
          "https://fantasysports.yahooapis.com/fantasy/v2/league/449.l.16001/teams/stats;type=week;week="
              + (fantasyContent.getLeague().getCurrent_week() - 1);
      ResponseEntity<String> weeklyScoresResponseEntity =
          yahooRestTemplate.exchange(weeklyScoresUrl, HttpMethod.GET, entity, String.class);
      Team[] weeklyScoresByTeam =
          xmlMapper
              .readValue(weeklyScoresResponseEntity.getBody(), FantasyContent.class)
              .getLeague()
              .getTeams();
      String weeklyRosterUrl =
          "https://fantasysports.yahooapis.com/fantasy/v2/league/449.l."
              + leagueId
              + "/teams/roster";
      ResponseEntity<String> weeklyRosterEntity =
          yahooRestTemplate.exchange(weeklyRosterUrl, HttpMethod.GET, entity, String.class);
      Team[] weeklyRosters =
          xmlMapper
              .readValue(weeklyRosterEntity.getBody(), FantasyContent.class)
              .getLeague()
              .getTeams();
      StringBuilder sb = new StringBuilder();
      sb.append("**Trophies of the week:**\n");
      Matchup[] matchups = fantasyContent.getLeague().getScoreboard().getMatchups();
      Map<String, Team> highAndLowScores = TrophyHelper.getHighAndLowScores(matchups);
      sb.append(":crown: High score :crown:\n")
          .append(highAndLowScores.get("highScore").getName())
          .append(" with ")
          .append(highAndLowScores.get("highScore").getTeam_points().getTotal())
          .append(" points\n");
      sb.append(":poop: Low score :poop:\n")
          .append(highAndLowScores.get("lowScore").getName())
          .append(" with ")
          .append(highAndLowScores.get("lowScore").getTeam_points().getTotal())
          .append(" points\n");

      Map<String, Team> blowoutTeams = TrophyHelper.getBlowout(matchups);
      sb.append(":scream: Blow out :scream:\n")
          .append(blowoutTeams.get("winningTeam").getName())
          .append(" blew out ")
          .append(blowoutTeams.get("losingTeam").getName())
          .append(" by ")
          .append(
              df.format(
                  blowoutTeams.get("winningTeam").getTeam_points().getTotal()
                      - blowoutTeams.get("losingTeam").getTeam_points().getTotal()))
          .append(" points\n");

      Map<String, Team> closeWinTeams = TrophyHelper.getCloseWin(matchups);
      sb.append(":sweat_smile: Close win :sweat_smile:\n")
          .append(closeWinTeams.get("winningTeam").getName())
          .append(" barely beat ")
          .append(closeWinTeams.get("losingTeam").getName())
          .append(" by ")
          .append(
              df.format(
                  closeWinTeams.get("winningTeam").getTeam_points().getTotal()
                      - closeWinTeams.get("losingTeam").getTeam_points().getTotal()))
          .append(" points\n");

      String[] luckyTeam = TrophyHelper.getLuckyTeam(weeklyScoresByTeam, matchups);
      sb.append(":four_leaf_clover: Lucky :four_leaf_clover:\n")
          .append(luckyTeam[0])
          .append(" was ")
          .append(luckyTeam[1])
          .append("-")
          .append(11 - Integer.parseInt(luckyTeam[1]))
          .append(" against the league, but still got the win\n");

      String[] unluckyTeam = TrophyHelper.getUnluckyTeam(weeklyScoresByTeam, matchups);
      sb.append(":rage: Unlucky :rage:\n")
          .append(unluckyTeam[0])
          .append(" was ")
          .append(unluckyTeam[1])
          .append("-")
          .append(11 - Integer.parseInt(unluckyTeam[1]))
          .append(" against the league, but still took an L\n");

      Team overachiever = TrophyHelper.getOverachiever(weeklyScoresByTeam);
      if (overachiever != null) {
        sb.append(":chart_with_upwards_trend: Overachiever :chart_with_upwards_trend:\n")
            .append(overachiever.getName())
            .append(" was ")
            .append(
                df.format(
                    overachiever.getTeam_points().getTotal()
                        - overachiever.getTeam_projected_points().getTotal()))
            .append(" points over their projection\n");
      }

      Team underachiever = TrophyHelper.getUnderachiever(weeklyScoresByTeam);
      if (underachiever != null) {
        sb.append(":chart_with_downwards_trend: Underachiever :chart_with_downwards_trend:\n")
            .append(underachiever.getName())
            .append(" was ")
            .append(
                df.format(
                    Math.abs(
                        underachiever.getTeam_points().getTotal()
                            - underachiever.getTeam_projected_points().getTotal())))
            .append(" points under their projection\n");
      }

      String[] bestAndWorstManager =
          TrophyHelper.getBestAndWorstManager(
              weeklyRosters,
              weeklyScoresByTeam,
              fantasyContent.getLeague().getCurrent_week() - 1,
              leagueId);
      sb.append(":robot: Best Manager :robot:\n")
          .append(bestAndWorstManager[0])
          .append(" scored ")
          .append(df.format(Double.parseDouble(bestAndWorstManager[1])))
          .append("% of their optimal score!\n");
      sb.append(":clown: Worst Manager :clown:\n")
          .append(bestAndWorstManager[2])
          .append(" left ")
          .append(df.format(Double.parseDouble(bestAndWorstManager[4])))
          .append(" points on their bench. Only scoring ")
          .append(df.format(Double.parseDouble(bestAndWorstManager[3])))
          .append("% of their optimal score.");

      return sb.toString();
    } catch (Exception e) {
      log.error("Could not get trophies due to", e);
      return "";
    }
  }
}

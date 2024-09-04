package com.markvargas.discordbot.client.yahoo.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.markvargas.discordbot.client.yahoo.model.FantasyContent;
import com.markvargas.discordbot.client.yahoo.model.Matchup;
import com.markvargas.discordbot.client.yahoo.model.Player;
import com.markvargas.discordbot.client.yahoo.model.Team;
import com.markvargas.discordbot.client.yahoo.model.Transaction;
import com.markvargas.discordbot.client.yahoo.model.YahooAuthToken;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
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
      File tokenFile = new File("token.txt");
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

  public String getAuthToken() {
    StringBuilder token = new StringBuilder();
    try {
      File file = new File("token.txt");
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
      matchups.append("Matchups\n");
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
      currentPoints.append(isFinalUpdate ? "Final Score Update" : "Score Update").append("\n");
      StringBuilder projectedPoints = new StringBuilder();
      projectedPoints.append("Approximate Projected Scores").append("\n");
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
      return currentPoints + "\n" + (isFinalUpdate ? "" : projectedPoints);
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
      log.info("Attempting to get score updates");
      ResponseEntity<String> response =
          yahooRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      XmlMapper xmlMapper = new XmlMapper();
      FantasyContent fantasyContent = xmlMapper.readValue(response.getBody(), FantasyContent.class);
      StringBuilder sb = new StringBuilder();
      sb.append("Current Standings:").append("\n");
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

  public String getCloseScores() {
    String url =
        "https://fantasysports.yahooapis.com/fantasy/v2/league/449.l." + leagueId + "/scoreboard";
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken());
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    try {
      log.info("Attempting to get close scores");
      ResponseEntity<String> response =
          yahooRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      XmlMapper xmlMapper = new XmlMapper();
      FantasyContent fantasyContent = xmlMapper.readValue(response.getBody(), FantasyContent.class);
      StringBuilder closeScores = new StringBuilder();
      closeScores.append("Close Scores\n");
      for (Matchup matchup : fantasyContent.getLeague().getScoreboard().getMatchups()) {
        Team team1 = matchup.getTeams()[0];
        Team team2 = matchup.getTeams()[1];
        if (Math.abs(team1.getTeam_points().getTotal() - team2.getTeam_points().getTotal())
            <= 15.0) {
          closeScores
              .append(team1.getName())
              .append(" ")
              .append(team1.getTeam_points().getTotal())
              .append(" - ")
              .append(team2.getTeam_points().getTotal())
              .append(" ")
              .append(team2.getName())
              .append("\n");
        }
      }
      return closeScores.toString();
    } catch (Exception e) {
      log.error("Could not get close scores due to", e);
      return "";
    }
  }

  public String getPlayersToMonitor() {
    String fullLeagueId = "449.l." + leagueId;
    String uri =
        "teams;team_keys="
            + fullLeagueId
            + ".t.1,"
            + fullLeagueId
            + ".t.2,"
            + fullLeagueId
            + ".t.3,"
            + fullLeagueId
            + ".t.4,"
            + fullLeagueId
            + ".t.5,"
            + fullLeagueId
            + ".t.6,"
            + fullLeagueId
            + ".t.7,"
            + fullLeagueId
            + ".t.8,"
            + fullLeagueId
            + ".t.9,"
            + fullLeagueId
            + ".t.10,"
            + fullLeagueId
            + ".t.11,"
            + fullLeagueId
            + ".t.12/roster";
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
      StringBuilder playersToMonitor = new StringBuilder();
      playersToMonitor.append("Starting Players to Monitor:\n");
      for (Team team : fantasyContent.getTeams()) {
        List<Player> injuredPlayers = new ArrayList<>();
        for (Player player : team.getRoster().getPlayers()) {
          if (!player.getSelected_position().getPosition().equals("BN")
              && !player.getSelected_position().getPosition().equals("IR")
              && player.getIs_editable() != 0
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
      log.info("Attempting to get score updates");
      ResponseEntity<String> response =
          yahooRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      XmlMapper xmlMapper = new XmlMapper();
      FantasyContent fantasyContent = xmlMapper.readValue(response.getBody(), FantasyContent.class);
      StringBuilder sb = new StringBuilder();
      sb.append("Waiver Report:").append("\n");
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
}

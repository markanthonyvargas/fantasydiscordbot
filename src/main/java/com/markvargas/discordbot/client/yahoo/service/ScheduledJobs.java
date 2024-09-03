package com.markvargas.discordbot.client.yahoo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ScheduledJobs {

  @Autowired private YahooService yahooService;

  @Scheduled(fixedRate = 2700_000)
  public void getAuthTokenJob() {
    log.info("Fetching new access token");
    yahooService.saveAuthToken();
    log.info("Fetched new access token");
  }

  @Scheduled(cron = "0 30 19 * * 4", zone = "America/New_York")
  public void getMatchupsJob() {
    log.info("Scheduled job for getting matchups running...");
    String matchups = yahooService.getMatchups();
    log.info(matchups);
  }

  @Schedules({
    @Scheduled(cron = "0 0 16 * * 0", zone = "America/New_York"),
    @Scheduled(cron = "0 0 20 * * 0", zone = "America/New_York"),
    @Scheduled(cron = "0 30 7 * * 1", zone = "America/New_York")
  })
  public void getScoreUpdatesJob() {
    log.info("Scheduled job for getting score updates running...");
    String scoreUpdates = yahooService.getScoreUpdates(false);
    log.info(scoreUpdates);
  }

  @Scheduled(cron = "0 30 7 * * 2", zone = "America/New_York")
  public void getFinalScoreUpdateJob() {
    log.info("Scheduled job for getting final score update running...");
    String scoreUpdates = yahooService.getScoreUpdates(true);
    log.info(scoreUpdates);
  }

  @Scheduled(cron = "0 30 7 * * 3", zone = "America/New_York")
  public void getStandingsJob() {
    log.info("Scheduled job for getting standings running...");
    String standings = yahooService.getStandings();
    log.info(standings);
  }

  @Scheduled(cron = "0 30 18 * * 1", zone = "America/New_York")
  public void getCloseScores() {
    log.info("Scheduled job for getting standings running...");
    String closeScores = yahooService.getCloseScores();
    log.info(closeScores);
  }

  @Scheduled(cron = "0 30 7 * * 0", zone = "America/New_York")
  public void getPlayersToMonitorJob() {
    log.info("Scheduled job for getting players to monitor running...");
    String playersToMonitor = yahooService.getPlayersToMonitor();
    log.info(playersToMonitor);
  }
}

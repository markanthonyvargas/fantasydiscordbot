package com.markvargas.discordbot.jobs;

import com.markvargas.discordbot.client.discord.service.DiscordService;
import com.markvargas.discordbot.client.yahoo.service.YahooService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ScheduledJobs {

  @Autowired private YahooService yahooService;

  @Autowired private DiscordService discordService;

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
    if (StringUtils.isEmpty(matchups)) {
      log.warn("Message not posted as it was empty");
    } else {
      discordService.createMessage(matchups);
    }
  }

  @Schedules({
    @Scheduled(cron = "0 0 16 * * 0", zone = "America/New_York"),
    @Scheduled(cron = "0 0 20 * * 0", zone = "America/New_York"),
    @Scheduled(cron = "0 30 7 * * 1", zone = "America/New_York"),
    @Scheduled(cron = "0 30 7 * * 5", zone = "America/New_York")
  })
  public void getScoreUpdatesJob() {
    log.info("Scheduled job for getting score updates running...");
    String scoreUpdates = yahooService.getScoreUpdates(false);
    if (StringUtils.isEmpty(scoreUpdates)) {
      log.warn("Message not posted as it was empty");
    } else {
      discordService.createMessage(scoreUpdates);
    }
  }

  @Scheduled(cron = "0 30 7 * * 2", zone = "America/New_York")
  public void getFinalScoreUpdateJob() {
    log.info("Scheduled job for getting final score update running...");
    String scoreUpdates = yahooService.getScoreUpdates(true);
    if (StringUtils.isEmpty(scoreUpdates)) {
      log.warn("Message not posted as it was empty");
    } else {
      discordService.createMessage(scoreUpdates);
    }
  }

  @Scheduled(cron = "0 30 7 * * 3", zone = "America/New_York")
  public void getStandingsJob() {
    log.info("Scheduled job for getting standings running...");
    String standings = yahooService.getStandings();
    if (StringUtils.isEmpty(standings)) {
      log.warn("Message not posted as it was empty");
    } else {
      discordService.createMessage(standings);
    }
  }

  @Scheduled(cron = "0 30 7 * * 0", zone = "America/New_York")
  public void getPlayersToMonitorJob() {
    log.info("Scheduled job for getting players to monitor running...");
    String playersToMonitor = yahooService.getPlayersToMonitor();
    if (StringUtils.isEmpty(playersToMonitor)) {
      log.warn("Message not posted as it was empty");
    } else {
      discordService.createMessage(playersToMonitor);
    }
  }

  @Scheduled(cron = "0 30 7 * * *", zone = "America/New_York")
  public void getWaiverReport() {
    log.info("Scheduled job for getting waiver report running...");
    String waiverReport = yahooService.getWaiverTransactions();
    if (StringUtils.isEmpty(waiverReport)) {
      log.warn("Message not posted as it was empty");
    } else if (waiverReport.equals("Waiver Report:\n")) {
      log.info("No waiver transactions processed today, message won't be posted");
    } else {
      discordService.createMessage(waiverReport);
    }
  }

  @Scheduled(cron = "0 30 7 * * 2", zone = "America/New_York")
  public void getTrophiesJob() {
    log.info("Scheduled job for getting trophies running...");
    String trophies = yahooService.getTrophies();
    if (StringUtils.isEmpty(trophies)) {
      log.warn("Message not posted as it was empty");
    } else {
      discordService.createMessage(trophies);
    }
  }
}

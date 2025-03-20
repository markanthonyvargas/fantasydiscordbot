package com.markvargas.discordbot.jobs;

import static org.mockito.Mockito.*;

import com.markvargas.discordbot.client.discord.service.DiscordService;
import com.markvargas.discordbot.client.yahoo.service.YahooService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {ScheduledJobs.class})
@ActiveProfiles("test")
class ScheduledJobsTest {

  @Autowired private ScheduledJobs scheduledJobs;

  @MockBean private YahooService yahooService;

  @MockBean private DiscordService discordService;

  @Test
  public void testGetMatchupsJob() {
    String message = "message";
    when(yahooService.getMatchups()).thenReturn(message);
    scheduledJobs.getMatchupsJob();
    verify(yahooService, times(1)).getMatchups();
    verify(discordService, times(1)).createMessage(message);
  }

  @Test
  public void testGetScoreUpdatesJob() {
    String message = "message";
    when(yahooService.getScoreUpdates(false)).thenReturn(message);
    scheduledJobs.getScoreUpdatesJob();
    verify(yahooService, times(1)).getScoreUpdates(false);
    verify(discordService, times(1)).createMessage(message);
  }

  @Test
  public void testGetFinalScoreUpdateJob() {
    String message = "message";
    when(yahooService.getScoreUpdates(true)).thenReturn(message);
    scheduledJobs.getFinalScoreUpdateJob();
    verify(yahooService, times(1)).getScoreUpdates(true);
    verify(discordService, times(1)).createMessage(message);
  }

  @Test
  public void testGetStandingsJob() {
    String message = "message";
    when(yahooService.getStandings()).thenReturn(message);
    scheduledJobs.getStandingsJob();
    verify(yahooService, times(1)).getStandings();
    verify(discordService, times(1)).createMessage(message);
  }

  @Test
  public void testGetPlayersToMonitorJob() {
    String message = "message";
    when(yahooService.getPlayersToMonitor()).thenReturn(message);
    scheduledJobs.getPlayersToMonitorJob();
    verify(yahooService, times(1)).getPlayersToMonitor();
    verify(discordService, times(1)).createMessage(message);
  }

  @Test
  public void testGetWaiverReportJob() {
    String message = "message";
    when(yahooService.getWaiverTransactions()).thenReturn(message);
    scheduledJobs.getWaiverReport();
    verify(yahooService, times(1)).getWaiverTransactions();
    verify(discordService, times(1)).createMessage(message);
  }

  @Test
  public void testGetWaiverReportNoTransactionsJob() {
    String message = "**Waiver Report:**\n";
    when(yahooService.getWaiverTransactions()).thenReturn(message);
    scheduledJobs.getWaiverReport();
    verify(yahooService, times(1)).getWaiverTransactions();
    verify(discordService, times(0)).createMessage(message);
  }

  @Test
  public void testGetTrophiesJob() {
    String message = "message";
    when(yahooService.getTrophies()).thenReturn(message);
    scheduledJobs.getTrophiesJob();
    verify(yahooService, times(1)).getTrophies();
    verify(discordService, times(1)).createMessage(message);
  }

  @Test
  public void testGetPowerRankingsJob() {
    String message = "message";
    when(yahooService.getPowerRankings()).thenReturn(message);
    scheduledJobs.getPowerRankingsJob();
    verify(yahooService, times(1)).getPowerRankings();
    verify(discordService, times(1)).createMessage(message);
  }
}

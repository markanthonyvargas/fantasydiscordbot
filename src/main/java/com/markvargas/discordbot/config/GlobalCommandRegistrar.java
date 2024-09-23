package com.markvargas.discordbot.config;

import discord4j.common.JacksonResources;
import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GlobalCommandRegistrar implements ApplicationRunner {

  private final RestClient client;

  public GlobalCommandRegistrar(GatewayDiscordClient gatewayDiscordClient) {
    this.client = gatewayDiscordClient.getRestClient();
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    final JacksonResources d4jMapper = JacksonResources.create();

    PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();
    final ApplicationService applicationService = client.getApplicationService();
    final long applicationId = client.getApplicationId().block();

    List<ApplicationCommandRequest> commands = new ArrayList<>();
    for (Resource resource : matcher.getResources("commands/*.json")) {
      ApplicationCommandRequest request =
          d4jMapper
              .getObjectMapper()
              .readValue(resource.getInputStream(), ApplicationCommandRequest.class);
      commands.add(request);
    }

    applicationService
        .bulkOverwriteGlobalApplicationCommand(applicationId, commands)
        .doOnNext(ignore -> log.debug("Successfully registered Global Commands"))
        .doOnError(e -> log.error("Failed to register global commands", e))
        .subscribe();
  }
}

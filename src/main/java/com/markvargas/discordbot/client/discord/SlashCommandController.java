package com.markvargas.discordbot.client.discord;

import com.markvargas.discordbot.client.discord.model.PingPong;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SlashCommandController {

    @PostMapping("/ping")
    PingPong returnPong(@RequestBody PingPong pingPong) {
        if (pingPong.getType() == 1) {
            return new PingPong(1);
        }
        return null;
    }
}

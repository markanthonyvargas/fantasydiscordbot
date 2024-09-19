package com.markvargas.discordbot.client.discord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markvargas.discordbot.client.discord.model.Interaction;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.HexFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class SlashCommandController {

  private static final String PUBLIC_KEY =
      "4ca20c3abe47f0dbfc77d1ac4effd6d0caff67254901127b79ad571a3412fe17";

  private boolean isVerified(String signature, String timestamp, Interaction interaction)
      throws Exception {
    log.info("Signature: {}", signature);
    log.info("Timestamp: {}", timestamp);
    log.info("Interaction: {}", interaction);

    ObjectMapper objectMapper = new ObjectMapper();
    String body = objectMapper.writeValueAsString(interaction);
    String data = timestamp + body;

    byte[] byteKey = HexFormat.of().parseHex(PUBLIC_KEY);
    byte[] byteSignature = HexFormat.of().parseHex(signature);
    X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(byteKey);
    KeyFactory kf = KeyFactory.getInstance("Ed25519");
    PublicKey publicKey = kf.generatePublic(x509EncodedKeySpec);

    Signature sig = Signature.getInstance("Ed25519");
    sig.initVerify(publicKey);
    sig.update(data.getBytes(StandardCharsets.UTF_8));
    return sig.verify(byteSignature);
  }

  @PostMapping("/api/interactions")
  public ResponseEntity<Interaction> interaction(
      @RequestHeader(name = "X-Signature-Ed25519", required = false) String signature,
      @RequestHeader(name = "X-Signature-Timestamp", required = false) String timestamp,
      @RequestBody Interaction interaction)
      throws Exception {
    if (StringUtils.isEmpty(signature)
        || StringUtils.isEmpty(timestamp)
        || !isVerified(signature, timestamp, interaction)) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    if (interaction.getType() == 1) {
      return ResponseEntity.ok(interaction);
    }
    return ResponseEntity.ok(null);
  }
}

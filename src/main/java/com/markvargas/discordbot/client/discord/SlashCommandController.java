package com.markvargas.discordbot.client.discord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markvargas.discordbot.client.discord.model.Interaction;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.NamedParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.buf.HexUtils;
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
//
    KeyFactory kf = KeyFactory.getInstance("Ed25519");
    boolean xisodd = false;
    int lastByteInt = byteKey[byteKey.length - 1];
    if ((lastByteInt & 255) >> 7 == 1) {
      xisodd = true;
    }

    byteKey[byteKey.length - 1] &= 127;
    int i = 0;
    int j = byteKey.length - 1;
    byte temp;
    while (j > i) {
      temp = byteKey[j];
      byteKey[j] = byteKey[i];
      byteKey[i] = temp;
      j--;
      i++;
    }
    BigInteger y = new BigInteger(1, byteKey);

    NamedParameterSpec paramSpec = new NamedParameterSpec("Ed25519");
    EdECPoint ep = new EdECPoint(xisodd, y);
    EdECPublicKeySpec publicKeySpec = new EdECPublicKeySpec(paramSpec, ep);
    PublicKey publicKey = kf.generatePublic(publicKeySpec);

    Signature sig = Signature.getInstance("Ed25519");
    sig.initVerify(publicKey);
    byte[] message = data.getBytes(StandardCharsets.UTF_8);
    sig.update(message);
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

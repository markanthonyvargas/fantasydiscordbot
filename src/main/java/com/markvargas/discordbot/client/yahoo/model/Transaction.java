package com.markvargas.discordbot.client.yahoo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {
  private String transaction_key;
  private String transaction_id;
  private String type;
  private String status;
  private long timestamp;
  private Player[] players;
}

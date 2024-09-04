package com.markvargas.discordbot.client.yahoo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {

  private Name name;
  private SelectedPosition selected_position;
  private String status_full;
  private String display_position;
  private TransactionData transaction_data;
  private int is_editable;
}

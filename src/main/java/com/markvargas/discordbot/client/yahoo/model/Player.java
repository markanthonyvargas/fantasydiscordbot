package com.markvargas.discordbot.client.yahoo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {

  private Name name;
  private SelectedPosition selected_position;
  private String status_full;
  private String display_position;
  private TransactionData transaction_data;
  private String editorial_team_key;
  private PlayerPoints player_points;
  private String player_key;
  private List<String> eligible_positions;
}

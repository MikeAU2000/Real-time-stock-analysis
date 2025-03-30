package com.example.project.demo_bc_xfin_service.model.DTO;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockChartDTO {
  private Long id;
  private String symbol;

  private Long regularMarketTime;
  private Double regularMarketPrice;
  private Double regularMarketChangePercent;
  private Double bid;
  private Integer bidSize;
  private Double ask;
  private Integer askSize;
  private String type;
  private LocalDateTime apiDateTime;
  private LocalDateTime marketUnixTime;
  private Double smaFivePrice;
  private Double smaTenPrice;
  private Double smaTwentyPrice;
}

package com.example.project.demo_bc_xfin_service.entity.fiveMinsEntity;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TSTOCKS_PRICE")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TstocksPriceEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // @ManyToOne
  // @JoinColumn(name = "symbol", referencedColumnName = "symbol")
  // private TstocksEntity tstock;
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
}

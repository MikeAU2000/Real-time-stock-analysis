package com.example.project.demo_bc_xfin_service.entity;

import java.io.Serializable;
import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TSTOCKS_PRICE_OHLCV")
@IdClass(StockPriceId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TstocksPriceOhlcvEntity implements Serializable {

    @Id
    private LocalDate tradeDate;

    @Id
    private String symbol;

    @Id
    @Column(name = "valid_range")
    private String validRange;

    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;

}

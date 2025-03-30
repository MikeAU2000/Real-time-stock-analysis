package com.example.project.demo_bc_xfin_service.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class StockPriceId implements Serializable {
  private LocalDate tradeDate;
  private String symbol;
  private String validRange;


    public StockPriceId() {}

    public StockPriceId(LocalDate tradeDate, String symbol, String validRange) {
        this.tradeDate = tradeDate;
        this.symbol = symbol;
        this.validRange = validRange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockPriceId that = (StockPriceId) o;
        return Objects.equals(tradeDate, that.tradeDate) && Objects.equals(symbol, that.symbol) && Objects.equals(validRange, that.validRange);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeDate, symbol, validRange);
    }
}

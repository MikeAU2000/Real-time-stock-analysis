package com.example.project.demo_bc_xfin_service.dto;

import java.util.List;
import lombok.Data;


@Data
public class QuoteResponseDto {
  private QuoteResponse quoteResponse;

  @Data
  public static class QuoteResponse {
    private List<Result> result;
    private Object error; // 錯誤可能為 null
  }

  @Data
  public static class Result {
    private String language;
    private String region;
    private String quoteType;
    private String typeDisp;
    private String quoteSourceName;
    private boolean triggerable;
    private String customPriceAlertConfidence;
    private double fiftyTwoWeekLowChange;
    private double fiftyTwoWeekLowChangePercent;
    private String fiftyTwoWeekRange;
    private double fiftyTwoWeekHighChange;
    private double fiftyTwoWeekHighChangePercent;
    private double fiftyTwoWeekLow;
    private double fiftyTwoWeekHigh;
    private double fiftyTwoWeekChangePercent;
    private long earningsTimestamp;
    private long earningsTimestampStart;
    private long earningsTimestampEnd;
    private long earningsCallTimestampStart;
    private long earningsCallTimestampEnd;
    private boolean isEarningsDateEstimate;
    private double trailingAnnualDividendRate;
    private double trailingPE;
    private double dividendRate;
    private double trailingAnnualDividendYield;
    private double dividendYield;
    private double epsTrailingTwelveMonths;
    private double epsForward;
    private double epsCurrentYear;
    private double priceEpsCurrentYear;
    private long sharesOutstanding;
    private double bookValue;
    private double fiftyDayAverage;
    private double fiftyDayAverageChange;
    private double fiftyDayAverageChangePercent;
    private double twoHundredDayAverage;
    private double twoHundredDayAverageChange;
    private double twoHundredDayAverageChangePercent;
    private long marketCap;
    private double forwardPE;
    private double priceToBook;
    private int sourceInterval;
    private int exchangeDataDelayedBy;
    private String averageAnalystRating;
    private String currency;
    private List<CorporateAction> corporateActions;
    private long regularMarketTime;
    private String exchange;
    private String messageBoardId;
    private String exchangeTimezoneName;
    private double regularMarketChangePercent;
    private double regularMarketPrice;
    private String marketState;
    private boolean tradeable;
    private boolean cryptoTradeable;
    private String shortName;
    private String longName;
    private String exchangeTimezoneShortName;
    private long gmtOffSetMilliseconds;
    private String market;
    private boolean esgPopulated;
    private boolean hasPrePostMarketData;
    private long firstTradeDateMilliseconds;
    private int priceHint;
    private double regularMarketChange;
    private double regularMarketDayHigh;
    private String regularMarketDayRange;
    private double regularMarketDayLow;
    private long regularMarketVolume;
    private double regularMarketPreviousClose;
    private double bid;
    private double ask;
    private int bidSize;
    private int askSize;
    private String fullExchangeName;
    private String financialCurrency;
    private double regularMarketOpen;
    private long averageDailyVolume3Month;
    private long averageDailyVolume10Day;
    private String symbol;
  }

  @Data
  public static class CorporateAction {
    private String header;
    private String message;
    private Meta meta;
  }

  @Data
  public static class Meta {
    private String eventType;
    private long dateEpochMs;
    private String amount;
  }
}



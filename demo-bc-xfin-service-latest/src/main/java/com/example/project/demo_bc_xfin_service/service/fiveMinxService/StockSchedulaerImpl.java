package com.example.project.demo_bc_xfin_service.service.fiveMinxService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.project.demo_bc_xfin_service.codewave.RedisManager;
import com.example.project.demo_bc_xfin_service.dto.QuoteResponseDto;
import com.example.project.demo_bc_xfin_service.entity.fiveMinsEntity.TstocksEntity;
import com.example.project.demo_bc_xfin_service.model.DTO.StockChartDTO;
import com.example.project.demo_bc_xfin_service.repository.TstocksPriceRepository;
import com.example.project.demo_bc_xfin_service.repository.TstocksRepository;
import com.example.project.demo_bc_xfin_service.service.dailyChartsService.DbUpload;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class StockSchedulaerImpl {

  @Autowired
  private TstocksRepository tstocksRepository;

  @Autowired
  private RedisManager redisManager;

  @Autowired
  private StockServiceimpl stockServiceimpl;

  @Autowired
  private TstocksPriceRepository tstocksPriceRepository;

  @Autowired
  private DbUpload dbUpload;

  @Scheduled(cron = "0 0 17 * * MON-FRI")
  public void runDailyTask() throws JsonProcessingException {
    List<String> stockSymbols = new ArrayList<>();
    String[] redisData = this.redisManager.get("stock", String[].class);
    if (redisData != null) {
      stockSymbols = Arrays.asList(redisData);
    } else {
      List<TstocksEntity> tstocksEntityList = tstocksRepository.findAll();
      for (TstocksEntity tstocksEntity : tstocksEntityList) {
        stockSymbols.add(tstocksEntity.getSymbol());
      }
      this.redisManager.set("stock", stockSymbols, Duration.ofDays(1));

    }
    for (String string : redisData) {
      ZoneId zone = ZoneId.of("Asia/Hong_Kong");
      LocalDateTime startDateTime = LocalDateTime.now();
        Long startPoint = startDateTime.atZone(zone).toEpochSecond();
      Long endPoint = startPoint + 1;
      dbUpload.collectData(string, "1d", startPoint, endPoint);
    }
    System.out.println(LocalDateTime.now());
  }

  @Scheduled(cron = "0 0 23 * * FRI")
  public void runWeeklyTask() throws JsonProcessingException {
    List<String> stockSymbols = new ArrayList<>();
    String[] redisData = this.redisManager.get("stock", String[].class);
    if (redisData != null) {
      stockSymbols = Arrays.asList(redisData);
    } else {
      List<TstocksEntity> tstocksEntityList = tstocksRepository.findAll();
      for (TstocksEntity tstocksEntity : tstocksEntityList) {
        stockSymbols.add(tstocksEntity.getSymbol());
      }
      this.redisManager.set("stock", stockSymbols, Duration.ofDays(1));

    }
    for (String string : redisData) {
      ZoneId zone = ZoneId.of("Asia/Hong_Kong");
      LocalDateTime endDateTime = LocalDateTime.now();
      Long endPoint = endDateTime.atZone(zone).toEpochSecond();
      Long startPoint = endPoint-396000L;
      dbUpload.collectData(string, "1wk", startPoint, endPoint);
    }
    System.out.println(LocalDateTime.now());
  }

  @Scheduled(cron = "0 0 1 1 * ?") // 每月 1 日 00:00 執行
  public void runMonthlyTask() throws JsonProcessingException {
    List<String> stockSymbols = new ArrayList<>();
    String[] redisData = this.redisManager.get("stock", String[].class);
    if (redisData != null) {
      stockSymbols = Arrays.asList(redisData);
    } else {
      List<TstocksEntity> tstocksEntityList = tstocksRepository.findAll();
      for (TstocksEntity tstocksEntity : tstocksEntityList) {
        stockSymbols.add(tstocksEntity.getSymbol());
      }
      this.redisManager.set("stock", stockSymbols, Duration.ofDays(1));

    }
    for (String string : redisData) {
      ZoneId zone = ZoneId.of("Asia/Hong_Kong");
      LocalDateTime endDateTime = LocalDateTime.now();
      Long endPoint = endDateTime.atZone(zone).toEpochSecond();
      Long startPoint = endPoint - 1L;
      dbUpload.collectData(string, "1mo", startPoint, endPoint);
    }
    System.out.println(LocalDateTime.now());
  }

  @Scheduled(fixedRate = 300000) // 每5分鐘執行一次
  public void fetchStockData() throws JsonProcessingException {
    List<String> stockSymbols = new ArrayList<>();
    String[] redisData = this.redisManager.get("stock", String[].class);
    if (redisData != null) {
      stockSymbols = Arrays.asList(redisData);
    } else {
      List<TstocksEntity> tstocksEntityList = tstocksRepository.findAll();
      for (TstocksEntity tstocksEntity : tstocksEntityList) {
        stockSymbols.add(tstocksEntity.getSymbol());
      }
      this.redisManager.set("stock", stockSymbols, Duration.ofDays(1));
    }
    List<StockChartDTO> stockChartDTOs = new ArrayList<>();
    System.out.println(LocalDateTime.now());
    for (String string : stockSymbols) {
      QuoteResponseDto.Result quoteResponseDto = stockServiceimpl
          .getYahooData(string).getQuoteResponse().getResult().get(0);
      if (quoteResponseDto.getMarketState().equals("PRE")) {
        this.redisManager.delete(string);
        this.redisManager.delete("StockChartDTO-" + string);
      } else {
        stockServiceimpl.savetstocksPriceEntity(string);
        stockChartDTOs = stockServiceimpl.getStockChartDTO(string,
            tstocksPriceRepository
                .findTopBySymbolOrderByRegularMarketTimeDesc(string)
                .getApiDateTime().toLocalDate());
        this.redisManager.set("StockChartDTO-" + string, stockChartDTOs,
            Duration.ofDays(7));
      }
      
    }
  }


}

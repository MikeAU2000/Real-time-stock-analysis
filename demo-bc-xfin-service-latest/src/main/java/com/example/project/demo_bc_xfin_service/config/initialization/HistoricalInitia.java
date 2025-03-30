package com.example.project.demo_bc_xfin_service.config.initialization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.project.demo_bc_xfin_service.codewave.RedisManager;
import com.example.project.demo_bc_xfin_service.entity.TstocksPriceOhlcvEntity;
import com.example.project.demo_bc_xfin_service.entity.fiveMinsEntity.TstocksEntity;
import com.example.project.demo_bc_xfin_service.repository.TstocksPriceOhlcvRepository;
import com.example.project.demo_bc_xfin_service.repository.TstocksRepository;
import com.example.project.demo_bc_xfin_service.service.dailyChartsService.DbUpload;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class HistoricalInitia {
  @Autowired
  private RedisManager redisManager;

  @Autowired
  private TstocksRepository tstocksRepository;

  @Autowired
  private DbUpload dbUpload;

  @Autowired
  private TstocksPriceOhlcvRepository tstocksPriceOhlcvRepository;


  public void initia() throws JsonProcessingException {
    List<String> stockSymbols = new ArrayList<>();
    String[] redisData = this.redisManager.get("stocks", String[].class);
    if (redisData != null) {
      stockSymbols = Arrays.asList(redisData);
    } else {
      List<TstocksEntity> tstocksEntityList = tstocksRepository.findAll();
      for (TstocksEntity tstocksEntity : tstocksEntityList) {
        stockSymbols.add(tstocksEntity.getSymbol());
      }
      this.redisManager.set("stocks", stockSymbols);
    }
    
    for (String sym : stockSymbols) {
      this.dbUpload.saveStockDataFromDTO(sym,"1d");
      //deleteLatestStock(sym, "1d"); 
      this.dbUpload.saveStockDataFromDTO(sym,"1wk");
      deleteLatestStock(sym, "1wk"); 
      this.dbUpload.saveStockDataFromDTO(sym,"1mo");
      deleteLatestStock(sym, "1mo");
    }
  }

  public void deleteLatestStock(String symbol, String validRange) {
    TstocksPriceOhlcvEntity tstocksPriceOhlcvEntity = 
        tstocksPriceOhlcvRepository.findTopBySymbolAndValidRangeOrderByTradeDateDesc(symbol, validRange);
    
    if (tstocksPriceOhlcvEntity != null) {
        tstocksPriceOhlcvRepository.delete(tstocksPriceOhlcvEntity);
    }
  }
}

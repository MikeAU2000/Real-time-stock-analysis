package com.example.project.demo_bc_xfin_service.controller;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.example.project.demo_bc_xfin_service.entity.TstocksPriceOhlcvEntity;
import com.example.project.demo_bc_xfin_service.mapper.CandleStickMapper;
import com.example.project.demo_bc_xfin_service.model.DTO.CandleStickDTO;
import com.example.project.demo_bc_xfin_service.model.DTO.StockChartDTO;
import com.example.project.demo_bc_xfin_service.service.dailyChartsService.DbUpload;
import com.example.project.demo_bc_xfin_service.service.fiveMinxService.StockServiceimpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class Operation {
  @Autowired
  private StockServiceimpl stockServiceimpl;

  @Autowired
  private CandleStickMapper candleStickMapper;

  @Autowired
  private DbUpload dbUpload;
  
  @CrossOrigin
  @GetMapping("/databy")
  public List<CandleStickDTO> getCandleStick(@RequestParam String symbol,@RequestParam String validRange,@RequestParam String periodType,@RequestParam int period) throws JsonProcessingException {
      return this.candleStickMapper.getCandleStickMapper(symbol, validRange, period,periodType);
  }
  @CrossOrigin
  @GetMapping("/StockChartDtoredisdata")
  public List<StockChartDTO> getDto(@RequestParam (value = "symbols") String sym) throws JsonProcessingException {
      return stockServiceimpl.getStockChartDTORedis(sym);
  }
  
  @GetMapping("/collectData")
  public List<TstocksPriceOhlcvEntity> collectData(@RequestParam (value = "symbols") String sym, @RequestParam (value = "validRange") String validRange, @RequestParam (value = "time") Long time) throws JsonProcessingException {
      return dbUpload.collectData(sym, validRange, time-1,time);
  }

}



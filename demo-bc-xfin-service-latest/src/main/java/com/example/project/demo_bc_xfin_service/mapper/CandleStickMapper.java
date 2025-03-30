package com.example.project.demo_bc_xfin_service.mapper;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.project.demo_bc_xfin_service.model.DTO.CandleStickDTO;
import com.example.project.demo_bc_xfin_service.service.dailyChartsService.DbUpload;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class CandleStickMapper {
  @Autowired
  private DbUpload dbUpload;

  public List<CandleStickDTO> getCandleStickMapper(String symbol,String validRange,int period,String periodType) throws JsonProcessingException{
    periodType=periodType.toUpperCase();
    switch (periodType) {
      case "YEAR":
        return this.dbUpload.getStocksEntityByYears(symbol, validRange, period);
      case "MONTH":
        return this.dbUpload.getStocksEntityByMonths(symbol, validRange, period);
      case "DAY":
        return this.dbUpload.getStocksEntityByDays(symbol, validRange, period);
        default:
        throw new IllegalArgumentException("Invalid type: " + periodType);
    }
  }
}

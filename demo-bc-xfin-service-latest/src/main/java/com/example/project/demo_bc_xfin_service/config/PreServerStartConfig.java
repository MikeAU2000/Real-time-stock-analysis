package com.example.project.demo_bc_xfin_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.example.project.demo_bc_xfin_service.config.initialization.HistoricalInitia;
import com.example.project.demo_bc_xfin_service.config.initialization.StockInit;
import com.example.project.demo_bc_xfin_service.repository.TstocksPriceOhlcvRepository;
import com.example.project.demo_bc_xfin_service.repository.TstocksPriceRepository;
import com.example.project.demo_bc_xfin_service.repository.TstocksRepository;

@Component
public class PreServerStartConfig implements CommandLineRunner {
  @Autowired
  private TstocksRepository tstocksRepository;

  @Autowired
  private StockInit stockInit;

  @Autowired
  private TstocksPriceOhlcvRepository tstocksPriceOhlcvRepository;

  @Autowired
  private HistoricalInitia historicalInitia;

  @Autowired
  private TstocksPriceRepository tstocksPriceRepository;

  @Override
  public void run(String... args) throws Exception {
    
    if (tstocksRepository.count() == 0) {
      stockInit.initializeStocks();
    }
    if (tstocksPriceRepository.count() == 0) {
      stockInit.intiia5MinData();
    }
    if (tstocksPriceOhlcvRepository.count() == 0) {
      historicalInitia.initia();
    }
  }
}

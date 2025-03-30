package com.example.project.demo_bc_xfin_service.config.initialization;


import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.project.demo_bc_xfin_service.entity.fiveMinsEntity.TstocksEntity;
import com.example.project.demo_bc_xfin_service.repository.TstocksRepository;
import com.example.project.demo_bc_xfin_service.service.fiveMinxService.StockServiceimpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;

@Component
public class StockInit {
  @Autowired
  private TstocksRepository tstocksRepository;

  @Autowired
  private StockServiceimpl stockServiceimpl;

  @Transactional
  public void initializeStocks(){
    TstocksEntity stock1= TstocksEntity.builder().symbol("0388.HK").build();
    TstocksEntity stock2= TstocksEntity.builder().symbol("0700.HK").build();
    TstocksEntity stock3= TstocksEntity.builder().symbol("0005.HK").build();
    TstocksEntity stock4= TstocksEntity.builder().symbol("0001.HK").build();
    TstocksEntity stock5= TstocksEntity.builder().symbol("9988.HK").build();
    TstocksEntity stock6= TstocksEntity.builder().symbol("3690.HK").build();
    TstocksEntity stock7= TstocksEntity.builder().symbol("1211.HK").build();
    TstocksEntity stock8= TstocksEntity.builder().symbol("1299.HK").build();
    TstocksEntity stock9= TstocksEntity.builder().symbol("0012.HK").build();
    TstocksEntity stock10= TstocksEntity.builder().symbol("0016.HK").build();


    tstocksRepository.save(stock1);
    tstocksRepository.save(stock2);
    tstocksRepository.save(stock3);
    tstocksRepository.save(stock4);
    tstocksRepository.save(stock5);
    tstocksRepository.save(stock6);
    tstocksRepository.save(stock7);
    tstocksRepository.save(stock8);
    tstocksRepository.save(stock9);
    tstocksRepository.save(stock10);
  }

  @Transactional
  public  void intiia5MinData() throws JsonProcessingException{
    List<String> stockSymbols = new ArrayList<>();
    // String[] redisData = this.redisManager.get("stock", String[].class);
    // if (redisData != null) {
    //   stockSymbols = Arrays.asList(redisData);
    // } else {
      List<TstocksEntity> tstocksEntityList = tstocksRepository.findAll();
      for (TstocksEntity tstocksEntity : tstocksEntityList) {
        stockSymbols.add(tstocksEntity.getSymbol());
      }
      // this.redisManager.set("stock", stockSymbols, Duration.ofDays(1));
    //}
    // List<StockChartDTO> stockChartDTOs = new ArrayList<>();
    //System.out.println(LocalDateTime.now());
    for (String string : stockSymbols) {
      
        stockServiceimpl.savetstocksPriceEntity(string);
    }
      
    }
}

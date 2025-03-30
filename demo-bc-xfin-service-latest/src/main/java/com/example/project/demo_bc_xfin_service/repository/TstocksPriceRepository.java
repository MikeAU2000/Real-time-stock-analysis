package com.example.project.demo_bc_xfin_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.project.demo_bc_xfin_service.entity.fiveMinsEntity.TstocksPriceEntity;

@Repository
public interface TstocksPriceRepository
    extends JpaRepository<TstocksPriceEntity, Integer> {

  List<TstocksPriceEntity> findAllBySymbolAndApiDateTimeBetween(String symbol,
      LocalDateTime start, LocalDateTime end);

  TstocksPriceEntity findTopBySymbolOrderByRegularMarketTimeDesc(String symbol);


  List<TstocksPriceEntity> findTop5BySymbolAndRegularMarketTimeBeforeOrderByRegularMarketTimeDesc(
        String symbol,
        Long regularMarketTime
);

List<TstocksPriceEntity> findTop10BySymbolAndRegularMarketTimeBeforeOrderByRegularMarketTimeDesc(
    String symbol,
    Long regularMarketTime
);

List<TstocksPriceEntity> findTop20BySymbolAndRegularMarketTimeBeforeOrderByRegularMarketTimeDesc(
        String symbol,
        Long regularMarketTime
);
    
Optional<TstocksPriceEntity> findTopBySymbolAndApiDateTimeBetweenOrderByApiDateTimeAsc(String symbol,LocalDateTime start, LocalDateTime end);

Optional<TstocksPriceEntity> findTopBySymbolAndApiDateTimeBetweenOrderByApiDateTimeDesc(String symbol,LocalDateTime start, LocalDateTime end);

Optional<TstocksPriceEntity> findTopBySymbolAndApiDateTimeBetweenOrderByRegularMarketPriceDesc(String symbol,LocalDateTime start, LocalDateTime end);

Optional<TstocksPriceEntity> findTopBySymbolAndApiDateTimeBetweenOrderByRegularMarketPriceAsc(String symbol,LocalDateTime start, LocalDateTime end);



}

package com.example.project.demo_bc_xfin_service.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.project.demo_bc_xfin_service.entity.StockPriceId;
import com.example.project.demo_bc_xfin_service.entity.TstocksPriceOhlcvEntity;

@Repository
public interface TstocksPriceOhlcvRepository
                extends JpaRepository<TstocksPriceOhlcvEntity, StockPriceId> {
        List<TstocksPriceOhlcvEntity> findTop10BySymbolAndValidRangeOrderByTradeDateDesc(
                        String symbol, String validRange);

        List<TstocksPriceOhlcvEntity> findTop20BySymbolAndValidRangeOrderByTradeDateDesc(
                        String symbol, String validRange);

        List<TstocksPriceOhlcvEntity> findTop30BySymbolAndValidRangeOrderByTradeDateDesc(
                        String symbol, String validRange);

        TstocksPriceOhlcvEntity findTopBySymbolAndValidRangeOrderByTradeDateDesc(
                        String symbol, String validRange);

        List<TstocksPriceOhlcvEntity> findBySymbolAndValidRangeAndTradeDateBetween(
                        String symbol, String validRange, LocalDate startDate,
                        LocalDate endDate);

        List<TstocksPriceOhlcvEntity> findTop5BySymbolAndValidRangeAndTradeDateBeforeOrderByTradeDateDesc(
                        String symbol, String validRange, LocalDate tradeDate);

        List<TstocksPriceOhlcvEntity> findTop10BySymbolAndValidRangeAndTradeDateBeforeOrderByTradeDateDesc(
                        String symbol, String validRange, LocalDate tradeDate);

        List<TstocksPriceOhlcvEntity> findTop20BySymbolAndValidRangeAndTradeDateBeforeOrderByTradeDateDesc(
                        String symbol, String validRange, LocalDate tradeDate);
}

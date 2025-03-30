package com.example.project.demo_bc_xfin_service.service.dailyChartsService;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.project.demo_bc_xfin_service.codewave.RedisManager;
import com.example.project.demo_bc_xfin_service.dto.YahooFinanceChartDto;
import com.example.project.demo_bc_xfin_service.entity.TstocksPriceOhlcvEntity;
import com.example.project.demo_bc_xfin_service.entity.fiveMinsEntity.TstocksEntity;
import com.example.project.demo_bc_xfin_service.entity.fiveMinsEntity.TstocksPriceEntity;
import com.example.project.demo_bc_xfin_service.manager.yfManager.CrubManager;
import com.example.project.demo_bc_xfin_service.manager.yfManager.YahooFinanceManager;
import com.example.project.demo_bc_xfin_service.manager.yfManager.YahooService;
import com.example.project.demo_bc_xfin_service.model.DTO.CandleStickDTO;
import com.example.project.demo_bc_xfin_service.repository.TstocksPriceOhlcvRepository;
import com.example.project.demo_bc_xfin_service.repository.TstocksPriceRepository;
import com.example.project.demo_bc_xfin_service.repository.TstocksRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class DbUpload {
        @Autowired
        private YahooService yahooService;

        @Autowired
        private CrubManager crubManager;

        @Autowired
        private YahooFinanceManager yahooFinanceManager;

        @Autowired
        private TstocksPriceOhlcvRepository tstocksPriceOhlcvRepository;

        @Autowired
        private RedisManager redisManager;

        @Autowired
        private TstocksPriceRepository tstocksPriceRepository;

        @Autowired
        private TstocksRepository tstocksRepository;



        public YahooFinanceChartDto getYahooData(String symbol, String range) {
                String cookies = yahooService.getYahooCookies();
                String key = crubManager.getKey(cookies);
                Long now = Instant.now().toEpochMilli();
                Long startPoint = 1514739600L; // 2019-12-30
                YahooFinanceChartDto yahooFinanceChartDto = yahooFinanceManager
                                .getStockChartDTO(symbol, key, cookies, range,
                                                startPoint, now);
                return yahooFinanceChartDto;
        }

        public List<TstocksPriceOhlcvEntity> collectData(String symbol,
                        String range, Long startPoint, Long endPoint) {
                List<TstocksPriceOhlcvEntity> tstocksPriceOhlcvEntities =
                                new ArrayList<>();
                String cookies = yahooService.getYahooCookies();
                String key = crubManager.getKey(cookies);
                YahooFinanceChartDto yahooFinanceChartDto = yahooFinanceManager
                                .getStockChartDTO(symbol, key, cookies, range,
                                                startPoint, endPoint);
                if (yahooFinanceChartDto == null
                                || yahooFinanceChartDto.getChart() == null
                                || yahooFinanceChartDto.getChart()
                                                .getResult() == null
                                || yahooFinanceChartDto.getChart().getResult()
                                                .isEmpty()) {
                        throw new IllegalArgumentException(
                                        "StockChartDTO is null or empty for symbol: "
                                                        + symbol);
                }
                YahooFinanceChartDto.Result result = yahooFinanceChartDto
                                .getChart().getResult().get(0);

                if (result.getMeta() == null || result.getIndicators() == null
                                || result.getIndicators().getQuote() == null
                                || result.getIndicators().getQuote()
                                                .isEmpty()) {
                        throw new IllegalArgumentException(
                                        "Missing essential data in DTO for symbol: "
                                                        + symbol);
                }

                YahooFinanceChartDto.Meta meta = result.getMeta();
                YahooFinanceChartDto.Quote quote =
                                result.getIndicators().getQuote().get(0);
                List<Long> timestamps = result.getTimestamp();

                if (timestamps == null || timestamps.isEmpty()
                                || quote.getOpen() == null
                                || quote.getHigh() == null
                                || quote.getLow() == null
                                || quote.getClose() == null
                                || quote.getVolume() == null) {
                        return null;
                }

                for (int i = 0; i < timestamps.size(); i++) {
                        LocalDate tradeDate = Instant
                                        .ofEpochSecond(timestamps.get(i))
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate();

                        TstocksPriceOhlcvEntity entity = TstocksPriceOhlcvEntity
                                        .builder().tradeDate(tradeDate)
                                        .symbol(meta.getSymbol())
                                        .validRange(range)
                                        .open(i < quote.getOpen().size()
                                                        ? priceFormatter(quote
                                                                        .getOpen()
                                                                        .get(i))
                                                        : null)
                                        .high(i < quote.getHigh().size()
                                                        ? priceFormatter(quote
                                                                        .getHigh()
                                                                        .get(i))
                                                        : null)
                                        .low(i < quote.getLow().size()
                                                        ? priceFormatter(quote
                                                                        .getLow()
                                                                        .get(i))
                                                        : null)
                                        .close(i < quote.getClose().size()
                                                        ? priceFormatter(quote
                                                                        .getClose()
                                                                        .get(i))
                                                        : null)
                                        .volume(i < quote.getVolume().size()
                                                        ? quote.getVolume()
                                                                        .get(i)
                                                        : null)
                                        .build();
                        if (entity.getClose() == null) {
                                continue;
                        }

                        tstocksPriceOhlcvRepository.save(entity);
                        tstocksPriceOhlcvEntities.add(entity);
                }
                return tstocksPriceOhlcvEntities;

        }

        public void saveStockDataFromDTO(String symbol, String validRange) {
                YahooFinanceChartDto yahooFinanceChartDto =
                                getYahooData(symbol, validRange);

                if (yahooFinanceChartDto == null
                                || yahooFinanceChartDto.getChart() == null
                                || yahooFinanceChartDto.getChart()
                                                .getResult() == null
                                || yahooFinanceChartDto.getChart().getResult()
                                                .isEmpty()) {
                        throw new IllegalArgumentException(
                                        "StockChartDTO is null or empty for symbol: "
                                                        + symbol);
                }

                YahooFinanceChartDto.Result result = yahooFinanceChartDto
                                .getChart().getResult().get(0);

                if (result.getMeta() == null || result.getIndicators() == null
                                || result.getIndicators().getQuote() == null
                                || result.getIndicators().getQuote()
                                                .isEmpty()) {
                        throw new IllegalArgumentException(
                                        "Missing essential data in DTO for symbol: "
                                                        + symbol);
                }

                YahooFinanceChartDto.Meta meta = result.getMeta();
                YahooFinanceChartDto.Quote quote =
                                result.getIndicators().getQuote().get(0);
                List<Long> timestamps = result.getTimestamp();

                if (timestamps == null || timestamps.isEmpty()
                                || quote.getOpen() == null
                                || quote.getHigh() == null
                                || quote.getLow() == null
                                || quote.getClose() == null
                                || quote.getVolume() == null) {
                        throw new IllegalArgumentException(
                                        "Timestamp or OHLCV data is missing for symbol: "
                                                        + symbol);
                }

                for (int i = 0; i < timestamps.size(); i++) {
                        LocalDate tradeDate = Instant
                                        .ofEpochSecond(timestamps.get(i))
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate();

                        TstocksPriceOhlcvEntity entity = TstocksPriceOhlcvEntity
                                        .builder().tradeDate(tradeDate)
                                        .symbol(meta.getSymbol())
                                        .validRange(validRange)
                                        .open(i < quote.getOpen().size()
                                                        ? priceFormatter(quote
                                                                        .getOpen()
                                                                        .get(i))
                                                        : null)
                                        .high(i < quote.getHigh().size()
                                                        ? priceFormatter(quote
                                                                        .getHigh()
                                                                        .get(i))
                                                        : null)
                                        .low(i < quote.getLow().size()
                                                        ? priceFormatter(quote
                                                                        .getLow()
                                                                        .get(i))
                                                        : null)
                                        .close(i < quote.getClose().size()
                                                        ? priceFormatter(quote
                                                                        .getClose()
                                                                        .get(i))
                                                        : null)
                                        .volume(i < quote.getVolume().size()
                                                        ? quote.getVolume()
                                                                        .get(i)
                                                        : null)
                                        .build();
                        if (entity.getClose() == null) {
                                continue;
                        }

                        tstocksPriceOhlcvRepository.save(entity);
                }
        }

        public Double priceFormatter(Double price) {
                if (price == null) {
                        return null; // 避免傳入 null 時發生錯誤
                }
                return BigDecimal.valueOf(price)
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue();
        }

        public List<TstocksPriceOhlcvEntity> getCandleStickData(String symbol,
                        String validRange, Integer num) {
                if (num == 10) {
                        return this.tstocksPriceOhlcvRepository
                                        .findTop10BySymbolAndValidRangeOrderByTradeDateDesc(
                                                        symbol, validRange);
                } else if (num == 20) {
                        return this.tstocksPriceOhlcvRepository
                                        .findTop20BySymbolAndValidRangeOrderByTradeDateDesc(
                                                        symbol, validRange);
                } else {
                        return this.tstocksPriceOhlcvRepository
                                        .findTop30BySymbolAndValidRangeOrderByTradeDateDesc(
                                                        symbol, validRange);
                }
        }

        public List<CandleStickDTO> getStocksEntityByYears(String symbol,
                        String validRange, int years)
                        throws JsonProcessingException {
                List<TstocksPriceOhlcvEntity> tstocksPriceOhlcvEntitiesList =
                                new ArrayList<>();
                List<CandleStickDTO> candleStickDTOs = new ArrayList<>();
                CandleStickDTO[] redisData = this.redisManager.get(
                                symbol + validRange + years + "year",
                                CandleStickDTO[].class);
                if (redisData != null) {
                        candleStickDTOs = Arrays.asList(redisData);
                        return candleStickDTOs;
                }
                ZoneId zone = ZoneId.of("Asia/Hong_Kong");
                LocalDate now = LocalDate.now(zone);
                LocalDate endDate = now;
                LocalDate startDate = now.minusYears(years);


                tstocksPriceOhlcvEntitiesList = tstocksPriceOhlcvRepository
                                .findBySymbolAndValidRangeAndTradeDateBetween(
                                                symbol, validRange, startDate,
                                                endDate);
                if (validRange.equals("1d")) {
                        if (getTodayCandleStickData(symbol) != null) {
                                tstocksPriceOhlcvEntitiesList
                                                .add(getTodayCandleStickData(
                                                                symbol));
                        }
                } else if (validRange.equals("1wk")) {
                        tstocksPriceOhlcvEntitiesList.add(
                                        getThisWeekCandleStickData(symbol));
                } else if (validRange.equals("1mo")) {
                        tstocksPriceOhlcvEntitiesList.add(
                                        getThisMonthCandleStickData(symbol));
                }
                candleStickDTOs = candleStickDtoMapper(
                                tstocksPriceOhlcvEntitiesList);
                this.redisManager.set(symbol + validRange + years + "year",
                                candleStickDTOs, Duration.ofMinutes(5));
                return candleStickDTOs;
        }

        public List<CandleStickDTO> getStocksEntityByMonths(String symbol,
                        String validRange, int months)
                        throws JsonProcessingException {
                List<TstocksPriceOhlcvEntity> tstocksPriceOhlcvEntitiesList =
                                new ArrayList<>();
                this.redisManager
                                .delete(symbol + validRange + months + "month");
                TstocksPriceOhlcvEntity[] redisData = this.redisManager.get(
                                symbol + validRange + months + "month",
                                TstocksPriceOhlcvEntity[].class);
                if (redisData != null) {
                        tstocksPriceOhlcvEntitiesList =
                                        Arrays.asList(redisData);
                        return candleStickDtoMapper(
                                        tstocksPriceOhlcvEntitiesList);
                }
                ZoneId zone = ZoneId.of("Asia/Hong_Kong");
                LocalDate now = LocalDate.now(zone);
                LocalDate endDate = now;
                LocalDate startDate = now.minusMonths(months);


                tstocksPriceOhlcvEntitiesList = tstocksPriceOhlcvRepository
                                .findBySymbolAndValidRangeAndTradeDateBetween(
                                                symbol, validRange, startDate,
                                                endDate);
                if (validRange.equals("1d")) {
                        if (getTodayCandleStickData(symbol) != null) {
                                tstocksPriceOhlcvEntitiesList
                                                .add(getTodayCandleStickData(
                                                                symbol));
                        }
                } else if (validRange.equals("1wk")) {
                        tstocksPriceOhlcvEntitiesList.add(
                                        getThisWeekCandleStickData(symbol));
                } else if (validRange.equals("1mo")) {
                        tstocksPriceOhlcvEntitiesList.add(
                                        getThisMonthCandleStickData(symbol));
                }

                this.redisManager.set(symbol + validRange + months + "month",
                                tstocksPriceOhlcvEntitiesList,
                                Duration.ofMinutes(5));
                return candleStickDtoMapper(tstocksPriceOhlcvEntitiesList);
        }

        public List<CandleStickDTO> getStocksEntityByDays(String symbol,
                        String validRange, int days)
                        throws JsonProcessingException {
                List<TstocksPriceOhlcvEntity> tstocksPriceOhlcvEntitiesList =
                                new ArrayList<>();
                TstocksPriceOhlcvEntity[] redisData = this.redisManager.get(
                                symbol + validRange + days + "day",
                                TstocksPriceOhlcvEntity[].class);
                if (redisData != null) {
                        tstocksPriceOhlcvEntitiesList =
                                        Arrays.asList(redisData);
                        return candleStickDtoMapper(
                                        tstocksPriceOhlcvEntitiesList);
                }
                ZoneId zone = ZoneId.of("Asia/Hong_Kong");
                LocalDate now = LocalDate.now(zone);
                LocalDate endDate = now;
                LocalDate startDate = now.minusDays(days);


                tstocksPriceOhlcvEntitiesList = tstocksPriceOhlcvRepository
                                .findBySymbolAndValidRangeAndTradeDateBetween(
                                                symbol, validRange, startDate,
                                                endDate);
                if (getTodayCandleStickData(symbol) != null) {
                        tstocksPriceOhlcvEntitiesList
                                        .add(getTodayCandleStickData(symbol));
                }
                this.redisManager.set(symbol + validRange + days + "day",
                                tstocksPriceOhlcvEntitiesList,
                                Duration.ofMinutes(5));
                return candleStickDtoMapper(tstocksPriceOhlcvEntitiesList);
        }

        public TstocksPriceOhlcvEntity getTodayCandleStickData(String symbol)
                        throws JsonProcessingException {
                List<String> stockSymbols = new ArrayList<>();
                String[] redisData =
                                this.redisManager.get("stocks", String[].class);
                if (redisData != null) {
                        stockSymbols = Arrays.asList(redisData);
                } else {
                        List<TstocksEntity> tstocksEntityList =
                                        tstocksRepository.findAll();
                        for (TstocksEntity tstocksEntity : tstocksEntityList) {
                                stockSymbols.add(tstocksEntity.getSymbol());
                        }
                        this.redisManager.set("stocks", stockSymbols);
                }
                ZoneId zone = ZoneId.of("Asia/Hong_Kong");
                LocalDateTime now = LocalDateTime.now(zone);
                LocalDateTime startOfDay = LocalDate.now(zone).atStartOfDay();
                Double closePrice = 0.0;
                Optional<TstocksPriceEntity> close = tstocksPriceRepository
                                .findTopBySymbolAndApiDateTimeBetweenOrderByApiDateTimeDesc(
                                                symbol, startOfDay, now);
                if (close.isPresent()) {
                        closePrice = close.get().getRegularMarketPrice();
                }
                if (closePrice == 0.0) {
                        return null;
                }

                Double openPrice = 0.0;
                Optional<TstocksPriceEntity> open = tstocksPriceRepository
                                .findTopBySymbolAndApiDateTimeBetweenOrderByApiDateTimeAsc(
                                                symbol, startOfDay, now);
                if (open.isPresent()) {
                        openPrice = open.get().getRegularMarketPrice();
                }

                Double highPrice = 0.0;
                Optional<TstocksPriceEntity> high = tstocksPriceRepository
                                .findTopBySymbolAndApiDateTimeBetweenOrderByRegularMarketPriceDesc(
                                                symbol, startOfDay, now);
                if (high.isPresent()) {
                        highPrice = high.get().getRegularMarketPrice();
                }

                Double lowPrice = 0.0;
                Optional<TstocksPriceEntity> low = tstocksPriceRepository
                                .findTopBySymbolAndApiDateTimeBetweenOrderByRegularMarketPriceAsc(
                                                symbol, startOfDay, now);
                if (low.isPresent()) {
                        lowPrice = low.get().getRegularMarketPrice();
                }
                return TstocksPriceOhlcvEntity.builder()
                                .tradeDate(LocalDate.now(zone)).symbol(symbol)
                                .validRange("1d").open(openPrice)
                                .high(highPrice).low(lowPrice).close(closePrice)
                                .volume(1000000L).build();
        }

        public TstocksPriceOhlcvEntity getThisWeekCandleStickData(String symbol)
                        throws JsonProcessingException {
                TstocksPriceOhlcvEntity tstocksPriceOhlcvEntity =
                                this.tstocksPriceOhlcvRepository
                                                .findTopBySymbolAndValidRangeOrderByTradeDateDesc(
                                                                symbol, "1wk");
                                //my error
                if (tstocksPriceOhlcvEntity == null) {
                        return getTodayCandleStickData(symbol);
                }

                ZoneId zone = ZoneId.of("Asia/Hong_Kong");
                LocalDate startData = tstocksPriceOhlcvEntity.getTradeDate();
                List<TstocksPriceOhlcvEntity> thisWeekCandleStickDataList =
                                this.tstocksPriceOhlcvRepository
                                                .findBySymbolAndValidRangeAndTradeDateBetween(
                                                                symbol, "1d",
                                                                startData,
                                                                LocalDate.now(zone));
                if (getTodayCandleStickData(symbol) != null) {
                        thisWeekCandleStickDataList
                                        .add(getTodayCandleStickData(symbol));
                }

                if (thisWeekCandleStickDataList.isEmpty()) {
                        return getTodayCandleStickData(symbol);
                }

                LocalDate tradeDate = LocalDate.now(zone);
                String validRange = "1wk";
                Double high = Double.MIN_VALUE;
                Double low = Double.MAX_VALUE;
                Double open = thisWeekCandleStickDataList.get(0).getOpen();
                Double close = thisWeekCandleStickDataList
                                .get(thisWeekCandleStickDataList.size() - 1)
                                .getClose();
                Long volume = 10000000L;
                for (TstocksPriceOhlcvEntity tstocksPriceOhlcvEntity2 : thisWeekCandleStickDataList) {
                        if (tstocksPriceOhlcvEntity2.getHigh() > high) {
                                high = tstocksPriceOhlcvEntity2.getHigh();
                        }
                        if (tstocksPriceOhlcvEntity2.getLow() < low) {
                                low = tstocksPriceOhlcvEntity2.getLow();
                        }
                }
                return TstocksPriceOhlcvEntity.builder().tradeDate(tradeDate)
                                .symbol(symbol).validRange(validRange)
                                .high(high).low(low).open(open).close(close)
                                .volume(volume).build();
        }

        public TstocksPriceOhlcvEntity getThisMonthCandleStickData(
                        String symbol) throws JsonProcessingException {
                TstocksPriceOhlcvEntity tstocksPriceOhlcvEntity =
                                this.tstocksPriceOhlcvRepository
                                                .findTopBySymbolAndValidRangeOrderByTradeDateDesc(
                                                                symbol, "1mo");

                if (tstocksPriceOhlcvEntity == null) {
                        return getThisWeekCandleStickData(symbol);
                }

                ZoneId zone = ZoneId.of("Asia/Hong_Kong");
                LocalDate startData = tstocksPriceOhlcvEntity.getTradeDate();
                List<TstocksPriceOhlcvEntity> thisMonthCandleStickDataList =
                                this.tstocksPriceOhlcvRepository
                                                .findBySymbolAndValidRangeAndTradeDateBetween(
                                                                symbol, "1wk",
                                                                startData,
                                                                LocalDate.now(zone));
                thisMonthCandleStickDataList
                                .add(getThisWeekCandleStickData(symbol));
                
                if (thisMonthCandleStickDataList.isEmpty()) {
                        return getThisWeekCandleStickData(symbol);
                }
                
                LocalDate tradeDate = LocalDate.now(zone);
                String validRange = "1mo";
                Double high = Double.MIN_VALUE;
                Double low = Double.MAX_VALUE;
                Double open = thisMonthCandleStickDataList.get(0).getOpen();
                Double close = thisMonthCandleStickDataList
                                .get(thisMonthCandleStickDataList.size() - 1)
                                .getClose();
                Long volume = 10000000L;
                for (TstocksPriceOhlcvEntity tstocksPriceOhlcvEntity2 : thisMonthCandleStickDataList) {
                        if (tstocksPriceOhlcvEntity2.getHigh() > high) {
                                high = tstocksPriceOhlcvEntity2.getHigh();
                        }
                        if (tstocksPriceOhlcvEntity2.getLow() < low) {
                                low = tstocksPriceOhlcvEntity2.getLow();
                        }
                }
                return TstocksPriceOhlcvEntity.builder().tradeDate(tradeDate)
                                .symbol(symbol).validRange(validRange)
                                .high(high).low(low).open(open).close(close)
                                .volume(volume).build();
        }

        public List<CandleStickDTO> candleStickDtoMapper(
                        List<TstocksPriceOhlcvEntity> tstocksPriceOhlcvEntitiesList) {
                // ZoneId zone = ZoneId.of("Asia/Hong_Kong");
                List<CandleStickDTO> candleStickDTOs = new ArrayList<>();
                for (TstocksPriceOhlcvEntity tstocksPriceOhlcvEntity : tstocksPriceOhlcvEntitiesList) {
                        String validRange =
                                        tstocksPriceOhlcvEntity.getValidRange();
                        LocalDate tradeDate =
                                        tstocksPriceOhlcvEntity.getTradeDate();
                        String symbol = tstocksPriceOhlcvEntity.getSymbol();
                        Double fiveMaPrice = fiveMaCalculator(symbol,
                                        validRange, tradeDate);
                        Double tenMaPrice = tenMaCalculator(symbol, validRange,
                                        tradeDate);
                        Double twentyMaPrice = twentyMaCalculator(symbol,
                                        validRange, tradeDate);

                        CandleStickDTO candleStickDTO = CandleStickDTO.builder()
                                        .tradeDate(tstocksPriceOhlcvEntity
                                                        .getTradeDate())
                                        .symbol(tstocksPriceOhlcvEntity
                                                        .getSymbol())
                                        .validRange(tstocksPriceOhlcvEntity
                                                        .getValidRange())
                                        .open(tstocksPriceOhlcvEntity.getOpen())
                                        .high(tstocksPriceOhlcvEntity.getHigh())
                                        .low(tstocksPriceOhlcvEntity.getLow())
                                        .close(tstocksPriceOhlcvEntity
                                                        .getClose())
                                        .volume(tstocksPriceOhlcvEntity
                                                        .getVolume())
                                        .fiveMa(fiveMaPrice).tenMa(tenMaPrice)
                                        .twentyMa(twentyMaPrice).build();
                        candleStickDTOs.add(candleStickDTO);
                }
                return candleStickDTOs;
        }

        public Double fiveMaCalculator(String symbol, String validRange,
                        LocalDate tradeDate) {
                List<TstocksPriceOhlcvEntity> fiveMas =
                                tstocksPriceOhlcvRepository
                                                .findTop5BySymbolAndValidRangeAndTradeDateBeforeOrderByTradeDateDesc(
                                                                symbol,
                                                                validRange,
                                                                tradeDate);
                Double fiveMaPrice = 0.0;
                for (TstocksPriceOhlcvEntity t : fiveMas) {
                        fiveMaPrice = BigDecimal.valueOf(fiveMaPrice)
                                        .add(BigDecimal.valueOf(t.getClose()))
                                        .doubleValue();
                }
                fiveMaPrice = BigDecimal.valueOf(fiveMaPrice)
                                .divide(BigDecimal.valueOf(5), 2,
                                                RoundingMode.HALF_UP)
                                .doubleValue();
                return fiveMaPrice;
        }

        public Double tenMaCalculator(String symbol, String validRange,
                        LocalDate tradeDate) {
                List<TstocksPriceOhlcvEntity> tenMas =
                                tstocksPriceOhlcvRepository
                                                .findTop10BySymbolAndValidRangeAndTradeDateBeforeOrderByTradeDateDesc(
                                                                symbol,
                                                                validRange,
                                                                tradeDate);
                Double tenMaPrice = 0.0;
                for (TstocksPriceOhlcvEntity t : tenMas) {
                        tenMaPrice = BigDecimal.valueOf(tenMaPrice)
                                        .add(BigDecimal.valueOf(t.getClose()))
                                        .doubleValue();
                }
                tenMaPrice = BigDecimal.valueOf(tenMaPrice)
                                .divide(BigDecimal.valueOf(10), 2,
                                                RoundingMode.HALF_UP)
                                .doubleValue();
                return tenMaPrice;
        }

        public Double twentyMaCalculator(String symbol, String validRange,
                        LocalDate tradeDate) {
                List<TstocksPriceOhlcvEntity> twentyMas =
                                tstocksPriceOhlcvRepository
                                                .findTop20BySymbolAndValidRangeAndTradeDateBeforeOrderByTradeDateDesc(
                                                                symbol,
                                                                validRange,
                                                                tradeDate);
                Double twentyMaPrice = 0.0;
                for (TstocksPriceOhlcvEntity t : twentyMas) {
                        twentyMaPrice = BigDecimal.valueOf(twentyMaPrice)
                                        .add(BigDecimal.valueOf(t.getClose()))
                                        .doubleValue();
                }
                twentyMaPrice = BigDecimal.valueOf(twentyMaPrice)
                                .divide(BigDecimal.valueOf(20), 2,
                                                RoundingMode.HALF_UP)
                                .doubleValue();
                return twentyMaPrice;
        }

}


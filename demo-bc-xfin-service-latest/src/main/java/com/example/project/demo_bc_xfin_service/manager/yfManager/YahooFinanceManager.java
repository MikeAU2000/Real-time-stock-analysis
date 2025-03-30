package com.example.project.demo_bc_xfin_service.manager.yfManager;


import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.example.project.demo_bc_xfin_service.dto.QuoteResponseDto;
import com.example.project.demo_bc_xfin_service.dto.YahooFinanceChartDto;


@Component
public class YahooFinanceManager {
  @Autowired
  private RestTemplate restTemplate;

  @Value("${api.yahooFinance.domain}")
  private String domain;

  @Value("${api.yahooFinance.endpoints.data}")
  private String data;

  @Value("${api.yahooFinance.endpoints.crumb}")
  private String crumb;



  private static final String USER_AGENT =
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

  public QuoteResponseDto getQuoteResponseDto(String num, String key, String cookies) {
    String url = domain + data + num + crumb + key;

    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
    headers.set(HttpHeaders.COOKIE, cookies);

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<QuoteResponseDto> response = restTemplate.exchange(url,
        HttpMethod.GET, entity, QuoteResponseDto.class);

    return response.getBody();

  }

  public YahooFinanceChartDto getStockChartDTO(String num, String key, String cookies, String range, Long startPoint, Long endpoint) {
    //Long now= Instant.now().toEpochMilli();
    //Long startPoint = 1104767999L; //2005-01-03
    //星期一會對這個星期，月份1號會對上個月
    String url =
    "https://query1.finance.yahoo.com/v8/finance/chart/"+num+"?period1="+startPoint+"&period2="+endpoint+"&interval="+range+"&events=history&crumb="+key;

    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
    headers.set(HttpHeaders.COOKIE, cookies);

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<YahooFinanceChartDto> response = restTemplate.exchange(url,
        HttpMethod.GET, entity, YahooFinanceChartDto.class);

    return response.getBody();

  }

  // public YahooFinanceChartDto getStockChartDTO(String num, String key, String cookies, String range) {
  //   Long now= Instant.now().toEpochMilli();
  //   Long startPoint = 1104767999L; //2005-01-03
  //   //星期一會對這個星期，月份1號會對上個月
  //   String url =
  //   "https://query1.finance.yahoo.com/v8/finance/chart/"+num+"?period1="+startPoint+"&period2="+now+"&interval="+range+"&events=history&crumb="+key;

  //   HttpHeaders headers = new HttpHeaders();
  //   headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
  //   headers.set(HttpHeaders.COOKIE, cookies);

  //   HttpEntity<String> entity = new HttpEntity<>(headers);
  //   ResponseEntity<YahooFinanceChartDto> response = restTemplate.exchange(url,
  //       HttpMethod.GET, entity, YahooFinanceChartDto.class);

  //   return response.getBody();

  // }
}

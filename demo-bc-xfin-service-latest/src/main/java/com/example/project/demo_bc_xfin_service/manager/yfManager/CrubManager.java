package com.example.project.demo_bc_xfin_service.manager.yfManager;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CrubManager {
  @Autowired
  private RestTemplate restTemplate;

  @Value("${api.yahooFinance.domain}")
  private String domain;
  
  @Value("${api.yahooFinance.endpoints.ky}")
  private String key;

  // 這裡的 cookie 需要在你的環境測試是否有效
  private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

  public String getKey(String cookies) {
    String url = domain+key;

    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
    headers.set(HttpHeaders.COOKIE, cookies);

    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

    return response.getBody();
  }
}


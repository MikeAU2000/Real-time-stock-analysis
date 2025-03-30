package com.example.project.demo_bc_xfin_service.manager.yfManager;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;

import java.util.stream.Collectors;

@Service
public class YahooService {
    @Autowired
    public RestTemplate restTemplate;
    // 常見 User-Agent
    // private final String[] userAgents = {
    //         "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    //         "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    //         "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/537.36 (KHTML, like Gecko) Version/15.0 Mobile/15E148 Safari/537.36",
    //         "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
    // };

    public String getYahooCookies() {
      String url= "https://fc.yahoo.com";
      HttpHeaders headers= new HttpHeaders();
      headers.set("User-Agent","Mozilla/5.0");
      HttpEntity<String> entity= new HttpEntity<>(headers);
      List<String> rawCookies= null;
      try {
        ResponseEntity<String> response=
        restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      } catch (HttpClientErrorException | HttpServerErrorException ex) {
        rawCookies = ex.getResponseHeaders().get(HttpHeaders.SET_COOKIE);
      }


      String formattedCookie = rawCookies.stream()
                .map(cookie -> cookie.split(";", 2)[0]) // 取 "A3=..."，去掉 Expires、Domain 等
                .collect(Collectors.joining("; "));

      return formattedCookie;
    }
}


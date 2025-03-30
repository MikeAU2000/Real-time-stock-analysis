package com.example.project.demo_bc_xfin_service.codewave;


import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

//IllegalArgumentException
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class)
  public ApiResp<Void> handleIllegalArgumentException(IllegalArgumentException i){
    return ApiResp.<Void>builder()
      .syscode(SysCode.ID_NOT_FOUND)
      .build();
  }

  @ExceptionHandler(HttpClientErrorException.class)
  public ApiResp<Void> handleBusinessException(){
    return ApiResp.<Void>builder()
      .syscode(SysCode.RestTemplate_Error)
      .build();
  }
}


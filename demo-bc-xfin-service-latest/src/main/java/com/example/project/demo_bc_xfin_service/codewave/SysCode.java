package com.example.project.demo_bc_xfin_service.codewave;


public enum SysCode {
  
  OK("000000", "Success."),
  ID_NOT_FOUND("900001", "ID not found."),
  RestTemplate_Error("3", "URL_Error"),
  
  // Pre-handled Runtime Exception
  RTE_NPE("999999", "Null Pointer Exception."),;

  private final String code;
  private final String message;
  private SysCode(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public String getCode() {
    return this.code;
  }
  public String getMessage() {
    return this.message;
  }
}


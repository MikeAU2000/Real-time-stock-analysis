package com.example.project.demo_bc_xfin_service.model.DTO;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandleStickDTO {
    private LocalDate tradeDate;
    private String symbol;
    private String validRange;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;
    private Double fiveMa;
    private Double tenMa;
    private Double twentyMa;
}

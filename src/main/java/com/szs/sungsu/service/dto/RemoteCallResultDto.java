package com.szs.sungsu.service.dto;

import lombok.Data;

import java.math.BigDecimal;

// 외부 API 응답 파싱용 dto
@Data
public class RemoteCallResultDto {

    //총지급액
    private BigDecimal totalPay;
    //산출세액
    private BigDecimal calculatedTax;
    //보험료
    private BigDecimal insurance;
    //교육비
    private BigDecimal education;
    //기부금
    private BigDecimal donation;
    //의료비
    private BigDecimal medical;
    //퇴직연금
    private BigDecimal retirement;
}

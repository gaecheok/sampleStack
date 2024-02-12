package com.szs.sungsu.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RefundResponse {

    @JsonProperty("이름")
    private String name;
    @JsonProperty("결정세액")
    private String determinedTax;
    @JsonProperty("퇴직연금세액공제")
    private String retirement;
}

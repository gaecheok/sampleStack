package com.szs.sungsu.api.request;

import lombok.Data;

@Data
public class SignupRequest {

    private String userId;
    private String password;
    private String name;
    private String regNo;

}

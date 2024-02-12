package com.szs.sungsu.api.request;

import lombok.Data;

@Data
public class LoginRequest {

    private String userId;
    private String password;
}

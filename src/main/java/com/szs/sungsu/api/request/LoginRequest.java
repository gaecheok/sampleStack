package com.szs.sungsu.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {

    @NotBlank
    @Size(min=3, max=20)
    private String userId;
    @NotBlank
    @Size(min=3, max=20)
    private String password;
}

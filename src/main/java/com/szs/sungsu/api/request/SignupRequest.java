package com.szs.sungsu.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignupRequest {

    @NotBlank
    @Size(min=3, max=20)
    private String userId;
    @NotBlank
    @Size(min=3, max=20)
    private String password;
    @NotBlank
    @Size(min=1, max=20)
    private String name;
    @NotBlank
    @Size(min=14, max=14)
    private String regNo;

}

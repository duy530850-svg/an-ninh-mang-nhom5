package com.txt1stparkuor.aes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParameterResponse {
    private String key;
    private String iv;
}

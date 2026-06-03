package com.txt1stparkuor.aes.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CryptoResponse {
    private boolean success;
    private String result;
    private String errorMessage;
}
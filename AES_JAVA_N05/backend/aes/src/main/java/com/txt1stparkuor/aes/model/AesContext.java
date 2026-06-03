package com.txt1stparkuor.aes.model;

import com.txt1stparkuor.aes.enums.CipherMode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AesContext {
    private byte[] data;
    private byte[] key;
    private byte[] iv;
    private CipherMode mode;
    private int keySize;
}

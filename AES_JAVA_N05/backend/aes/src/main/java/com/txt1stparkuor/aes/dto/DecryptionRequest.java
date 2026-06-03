package com.txt1stparkuor.aes.dto;

import com.txt1stparkuor.aes.enums.CipherMode;
import com.txt1stparkuor.aes.enums.DataFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DecryptionRequest {

    @NotBlank(message = "Bản mã (ciphertext) không được để trống.")
    private String ciphertext;

    @NotBlank(message = "Khóa mật mã (key) không được để trống.")
    private String key;

    @NotNull(message = "Định dạng khóa (keyFormat) không được để trống.")
    private DataFormat keyFormat;

    @NotNull(message = "Kích thước khóa (keySize) không được để trống.")
    private Integer keySize;

    @NotNull(message = "Chế độ mã hóa (mode) không được để trống.")
    private CipherMode mode;

    private String iv;

    private DataFormat ivFormat;

    @NotNull(message = "Định dạng đầu vào (inputFormat) không được để trống.")
    private DataFormat inputFormat;
}

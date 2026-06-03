package com.txt1stparkuor.aes.dto;

import com.txt1stparkuor.aes.enums.CipherMode;
import com.txt1stparkuor.aes.enums.DataFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EncryptionRequest {

    @NotBlank(message = "Văn bản rõ (plaintext) không được để trống.")
    private String plaintext;

    @NotBlank(message = "Khóa mật mã (key) không được để trống.")
    private String key;

    @NotNull(message = "Định dạng khóa (keyFormat) không được để trống.")
    private DataFormat keyFormat;

    @NotNull(message = "Kích thước khóa (keySize) không được để trống.")
    private Integer keySize; // Chuyển sang Integer để kiểm tra @NotNull hiệu quả

    @NotNull(message = "Chế độ mã hóa (mode) không được để trống.")
    private CipherMode mode;

    private String iv; // IV có thể null nếu chọn ECB, ta sẽ check thủ công trong Service sau

    private DataFormat ivFormat;

    @NotNull(message = "Định dạng bản mã đầu ra (outputFormat) không được để trống.")
    private DataFormat outputFormat;
}
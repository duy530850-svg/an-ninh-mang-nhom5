package com.txt1stparkuor.aes.dto;

import com.txt1stparkuor.aes.enums.CipherMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileCryptoRequest {

    @NotNull(message = "Vui lòng đính kèm file cần xử lý.")
    private MultipartFile file;

    @NotBlank(message = "Khóa mật mã (key) không được để trống.")
    private String key;

    @NotNull(message = "Kích thước khóa (keySize) không được để trống.")
    private Integer keySize;

    @NotNull(message = "Chế độ mã hóa (mode) không được để trống.")
    private CipherMode mode;

    private String iv;
}

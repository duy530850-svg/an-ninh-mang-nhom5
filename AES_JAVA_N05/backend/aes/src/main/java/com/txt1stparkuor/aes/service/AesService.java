package com.txt1stparkuor.aes.service;

import com.txt1stparkuor.aes.dto.CryptoResponse;
import com.txt1stparkuor.aes.dto.DecryptionRequest;
import com.txt1stparkuor.aes.dto.EncryptionRequest;
import com.txt1stparkuor.aes.engine.AesCore;
import com.txt1stparkuor.aes.enums.CipherMode;
import com.txt1stparkuor.aes.enums.DataFormat;
import com.txt1stparkuor.aes.mapper.AesMapper;
import com.txt1stparkuor.aes.model.AesContext;
import com.txt1stparkuor.aes.utils.AesUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AesService {

    private final AesMapper aesMapper;

    public CryptoResponse encrypt(EncryptionRequest request) {
        try {
            // 1. Ánh xạ dữ liệu sang byte array bằng MapStruct
            AesContext context = aesMapper.toAesContext(request);

            // 2. Kiểm tra tính hợp lệ của Khóa và IV
            validateParameters(context);

            // 3. Thực hiện thuật toán mã hóa tự viết
            byte[] cipherBytes = AesCore.encrypt(
                    context.getData(),
                    context.getKey(),
                    context.getIv(),
                    context.getMode(),
                    context.getKeySize()
            );

            // 4. Định dạng đầu ra thành Hex hoặc Base64 theo yêu cầu
            String result = AesUtil.convertToString(cipherBytes, request.getOutputFormat());
            return new CryptoResponse(true, result, null);

        } catch (Exception e) {
            return new CryptoResponse(false, null, e.getMessage());
        }
    }

    public CryptoResponse decrypt(DecryptionRequest request) {
        try {
            AesContext context = aesMapper.toAesContext(request);

            validateParameters(context);

            // Thực hiện giải mã
            byte[] plainBytes = AesCore.decrypt(
                    context.getData(),
                    context.getKey(),
                    context.getIv(),
                    context.getMode(),
                    context.getKeySize()
            );

            // Giải mã xong mặc định chuyển về dạng text UTF-8
            String result = AesUtil.convertToString(plainBytes, DataFormat.PLAIN_TEXT);
            return new CryptoResponse(true, result, null);

        } catch (Exception e) {
            return new CryptoResponse(false, null, e.getMessage());
        }
    }

    /**
     * Xác thực độ dài của Khóa và véc-tơ IV.
     */
    private void validateParameters(AesContext context) {
        int expectedKeyLength = context.getKeySize() / 8;
        if (context.getKey() == null || context.getKey().length != expectedKeyLength) {
            throw new IllegalArgumentException(String.format(
                    "Độ dài khóa không hợp lệ cho AES-%d. Yêu cầu %d bytes (Hex: %d ký tự).",
                    context.getKeySize(), expectedKeyLength, expectedKeyLength * 2
            ));
        }

        if (context.getMode() == CipherMode.CBC) {
            if (context.getIv() == null || context.getIv().length != 16) {
                throw new IllegalArgumentException("Chế độ CBC yêu cầu véc-tơ khởi tạo IV dài đúng 16 bytes (32 ký tự Hex).");
            }
        }
    }
}
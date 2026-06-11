package com.txt1stparkuor.aes.service;

import com.txt1stparkuor.aes.dto.CryptoResponse;
import com.txt1stparkuor.aes.dto.DecryptionRequest;
import com.txt1stparkuor.aes.dto.EncryptionRequest;
import com.txt1stparkuor.aes.dto.ParameterResponse;
import com.txt1stparkuor.aes.engine.AesCore;
import com.txt1stparkuor.aes.enums.CipherMode;
import com.txt1stparkuor.aes.enums.DataFormat;
import com.txt1stparkuor.aes.mapper.AesMapper;
import com.txt1stparkuor.aes.model.AesContext;
import com.txt1stparkuor.aes.utils.AesUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AesService {

    private final AesMapper aesMapper;
    private final SecureRandom secureRandom = new SecureRandom();

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
    public ParameterResponse generateParams(int keySize, boolean needIv, String format) {
        int keyByteLength = keySize / 8;
        String actualKey;
        String actualIv = "";

        if ("PLAIN_TEXT".equals(format)) {
            actualKey = generateRandomReadableString(keyByteLength);
            if (needIv) {
                actualIv = generateRandomReadableString(16);
            }
        }
        else if ("BASE64".equals(format)) {
            byte[] randomKeyBytes = new byte[keyByteLength];
            secureRandom.nextBytes(randomKeyBytes);
            actualKey = Base64.getEncoder().encodeToString(randomKeyBytes);

            if (needIv) {
                byte[] randomIvBytes = new byte[16];
                secureRandom.nextBytes(randomIvBytes);
                actualIv = Base64.getEncoder().encodeToString(randomIvBytes);
            }
        }
        else {
            // Mặc định là HEX
            actualKey = AesUtil.generateRandomHex(keyByteLength);
            if (needIv) {
                actualIv = AesUtil.generateRandomHex(16);
            }
        }

        return new ParameterResponse(actualKey, actualIv);
    }

    /**
     * Hàm phụ trợ sinh chuỗi ký tự ngẫu nhiên đọc được (Dành riêng cho Plaintext)
     */
    private String generateRandomReadableString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return sb.toString();
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
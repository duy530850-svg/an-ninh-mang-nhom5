package com.txt1stparkuor.aes.utils;

import com.txt1stparkuor.aes.enums.DataFormat;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AesUtil {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * Chuyển đổi chuỗi đầu vào thành mảng byte dựa trên định dạng yêu cầu.
     */
    public static byte[] convertToBytes(String input, DataFormat format) {
        if (input == null || input.isEmpty()) {
            return new byte[0];
        }
        return switch (format) {
            case PLAIN_TEXT -> input.getBytes(StandardCharsets.UTF_8);
            case HEX -> hexToBytes(input);
            case BASE64 -> Base64.getDecoder().decode(input.trim());
        };
    }

    /**
     * Chuyển đổi mảng byte thành chuỗi đầu ra theo định dạng yêu cầu.
     */
    public static String convertToString(byte[] bytes, DataFormat format) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        return switch (format) {
            case PLAIN_TEXT -> new String(bytes, StandardCharsets.UTF_8);
            case HEX -> bytesToHex(bytes);
            case BASE64 -> Base64.getEncoder().encodeToString(bytes);
        };
    }

    /**
     * Hàm nội bộ chuyển Hex sang Byte Array.
     */
    public static byte[] hexToBytes(String hex) {
        // Loại bỏ khoảng trắng hoặc ký tự không hợp lệ nếu có
        String cleanHex = hex.replaceAll("\\s", "").toUpperCase();
        if (cleanHex.length() % 2 != 0) {
            throw new IllegalArgumentException("Độ dài chuỗi Hex phải là số chẵn.");
        }
        int len = cleanHex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(cleanHex.charAt(i), 16);
            int low = Character.digit(cleanHex.charAt(i + 1), 16);
            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("Chuỗi chứa ký tự Hex không hợp lệ.");
            }
            data[i / 2] = (byte) ((high << 4) + low);
        }
        return data;
    }

    /**
     * Hàm nội bộ chuyển Byte Array sang Hex.
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Sinh chuỗi ngẫu nhiên bảo mật cao dạng Hex.
     */
    public static String generateRandomHex(int byteLength) {
        byte[] bytes = new byte[byteLength];
        secureRandom.nextBytes(bytes);
        return bytesToHex(bytes);
    }
}
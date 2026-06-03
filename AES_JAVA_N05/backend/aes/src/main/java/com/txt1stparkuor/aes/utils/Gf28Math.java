package com.txt1stparkuor.aes.utils;

public class Gf28Math {

    /**
     * Phép cộng trên trường hữu hạn GF(2^8) tương đương với phép toán XOR (^).
     */
    public static byte add(byte a, byte b) {
        return (byte) (a ^ b);
    }

    /**
     * Phép nhân một phần tử với x (ký hiệu là {02} trong hệ thập lục phân).
     * Đây là phép toán dịch trái 1 bit, nếu bit cao nhất bằng 1 thì XOR với đa thức tối giản 0x1B.
     */
    public static byte xtime(byte b) {
        int unsignedB = b & 0xFF;
        int result = unsignedB << 1;
        // Nếu bit thứ 8 (MSB) bằng 1, thực hiện phép khử modulo với đa thức bất khả quy
        if ((unsignedB & 0x80) != 0) {
            result ^= 0x1B; // Đa thức m(x) = x^8 + x^4 + x^3 + x + 1 (bỏ bit thứ 9 do đã tràn)
        }
        return (byte) (result & 0xFF);
    }

    /**
     * Phép nhân hai phần tử bất kỳ trên trường hữu hạn GF(2^8).
     * Áp dụng thuật toán nhân dịch chuyển liên tiếp (Russian Peasant Multiplication).
     */
    public static byte multiply(byte a, byte b) {
        int unsignedA = a & 0xFF;
        int unsignedB = b & 0xFF;
        int result = 0;
        int temp = unsignedA;

        for (int i = 0; i < 8; i++) {
            // Nếu bit hiện tại của b là 1, XOR kết quả với temp
            if ((unsignedB & 1) != 0) {
                result ^= temp;
            }
            // Dịch chuyển temp (nhân với x) cho vòng tiếp theo
            temp = xtime((byte) temp) & 0xFF;
            // Dịch phải b để xét bit tiếp theo
            unsignedB >>= 1;
        }
        return (byte) (result & 0xFF);
    }
}
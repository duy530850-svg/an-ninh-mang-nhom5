package com.txt1stparkuor.aes.engine;

public class AesKeyExpansion {

    // Hằng số vòng Rcon tiêu chuẩn của AES (Chỉ lấy phần tử đầu tiên của từ 32-bit)
    private static final int[] RCON = {
            0x00, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1B, 0x36
    };

    /**
     * Mở rộng khóa gốc thành chuỗi các khóa vòng liên tiếp.
     * @param key Khóa gốc đầu vào (16, 24, hoặc 32 byte)
     * @param keySize Kích thước khóa (128, 192, hoặc 256)
     * @return Mảng byte phẳng chứa toàn bộ các khóa vòng đã sinh
     */
    public static byte[] expandKey(byte[] key, int keySize) {
        int nk = keySize / 32;         // Số từ (word) của khóa gốc (4, 6, hoặc 8)
        int nr = nk + 6;               // Số vòng lặp tương ứng (10, 12, hoặc 14)
        int totalWords = 4 * (nr + 1); // Tổng số từ 32-bit của khóa mở rộng

        byte[] expandedKey = new byte[totalWords * 4];

        // Bước 1: Sao chép nguyên vẹn khóa gốc vào các vị trí đầu tiên của khóa mở rộng
        System.arraycopy(key, 0, expandedKey, 0, key.length);

        byte[] temp = new byte[4];

        // Bước 2: Sinh tuần tự các từ khóa tiếp theo
        for (int i = nk; i < totalWords; i++) {
            // Lấy từ 32-bit liền trước: expandedKey[i-1]
            System.arraycopy(expandedKey, (i - 1) * 4, temp, 0, 4);

            if (i % nk == 0) {
                rotWord(temp);
                subWord(temp);
                temp[0] ^= RCON[i / nk]; // XOR byte đầu tiên với hằng số vòng Rcon
            } else if (nk > 6 && (i % nk == 4)) {
                // Điều kiện đặc biệt chỉ áp dụng cho AES-256 (Nk = 8)
                subWord(temp);
            }

            // Từ hiện tại = Từ cách đó Nk vị trí XOR với từ tạm thời (temp)
            int prevNkIndex = (i - nk) * 4;
            int currentIndex = i * 4;
            for (int b = 0; b < 4; b++) {
                expandedKey[currentIndex + b] = (byte) (expandedKey[prevNkIndex + b] ^ temp[b]);
            }
        }

        return expandedKey;
    }

    /**
     * Dịch chuyển vòng trái 1 byte trong từ 4-byte (RotWord).
     */
    private static void rotWord(byte[] word) {
        byte t = word[0];
        word[0] = word[1];
        word[1] = word[2];
        word[2] = word[3];
        word[3] = t;
    }

    /**
     * Thay thế từng byte trong từ 4-byte qua bảng thế S-Box (SubWord).
     */
    private static void subWord(byte[] word) {
        for (int i = 0; i < 4; i++) {
            word[i] = (byte) AesBlockCipher.S_BOX[word[i] & 0xFF];
        }
    }
}
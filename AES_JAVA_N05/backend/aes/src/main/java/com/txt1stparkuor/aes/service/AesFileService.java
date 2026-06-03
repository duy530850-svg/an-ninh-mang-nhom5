package com.txt1stparkuor.aes.service;

import com.txt1stparkuor.aes.engine.AesCore;
import com.txt1stparkuor.aes.enums.CipherMode;
import com.txt1stparkuor.aes.utils.AesUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class AesFileService {

    public void processFile(MultipartFile file, String keyHex, int keySize, String modeStr, String ivHex, boolean isEncrypt, HttpServletResponse response) throws Exception {

        // 1. Chuẩn hóa Khóa và IV (Bắt buộc dùng HEX đối với File)
        byte[] key = AesUtil.hexToBytes(keyHex);
        CipherMode mode = CipherMode.valueOf(modeStr.toUpperCase());
        byte[] iv = null;

        if (mode == CipherMode.CBC) {
            if (ivHex == null || ivHex.isEmpty()) {
                throw new IllegalArgumentException("Chế độ CBC yêu cầu cung cấp véc-tơ khởi tạo (IV).");
            }
            iv = AesUtil.hexToBytes(ivHex);
        }

        // 2. Kiểm tra tính hợp lệ
        if (key.length != keySize / 8) {
            throw new IllegalArgumentException("Độ dài khóa không khớp với kích thước AES-" + keySize + " đã chọn.");
        }
        if (mode == CipherMode.CBC && iv.length != 16) {
            throw new IllegalArgumentException("Véc-tơ khởi tạo (IV) phải dài đúng 32 ký tự Hex (16 byte).");
        }

        // 3. Xử lý tên file an toàn (hỗ trợ tiếng Việt)
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) originalFilename = "unknown_file";

        String outputFilename;
        if (isEncrypt) {
            outputFilename = originalFilename + ".enc";
        } else {
            // Tự động cắt bỏ đuôi .enc nếu có khi giải mã
            outputFilename = originalFilename.endsWith(".enc")
                    ? originalFilename.substring(0, originalFilename.length() - 4)
                    : "decrypted_" + originalFilename;
        }

        // Mã hóa URL để trình duyệt hiểu được tên file có dấu tiếng Việt
        String encodedFilename = URLEncoder.encode(outputFilename, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");

        // 4. Cấu hình HTTP Response để trình duyệt tự động bắt đầu tải xuống
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);

        // 5. Mở luồng trực tiếp (Streaming) từ Upload -> Thuật toán -> Download
        try (InputStream in = file.getInputStream();
             OutputStream out = response.getOutputStream()) {

            if (isEncrypt) {
                AesCore.encryptStream(in, out, key, iv, mode, keySize);
            } else {
                AesCore.decryptStream(in, out, key, iv, mode, keySize);
            }
            out.flush();
        }
    }
}

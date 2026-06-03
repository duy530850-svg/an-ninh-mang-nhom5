package com.txt1stparkuor.aes;

import com.txt1stparkuor.aes.engine.AesCore;
import com.txt1stparkuor.aes.enums.CipherMode;
import com.txt1stparkuor.aes.utils.AesUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;

@SpringBootTest
class AesApplicationTests {

	@Test
	public void testAes128EcbLoop() {
		String originalText = "Chào mừng bạn đến với đồ án An ninh mạng!";
		byte[] plaintext = originalText.getBytes(StandardCharsets.UTF_8);
		byte[] key = AesUtil.hexToBytes("00112233445566778899AABBCCDDEEFF"); // 16 bytes
		byte[] iv = null;

		// Mã hóa
		byte[] ciphertext = AesCore.encrypt(plaintext, key, iv, CipherMode.ECB, 128);
		Assertions.assertNotEquals(0, ciphertext.length);

		// Giải mã
		byte[] decryptedBytes = AesCore.decrypt(ciphertext, key, iv, CipherMode.ECB, 128);
		String decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);

		// Kiểm tra tính nguyên bản
		Assertions.assertEquals(originalText, decryptedText);
	}

	@Test
	public void testAes256CbcLoop() {
		String originalText = "Kiểm tra hệ thống mã hóa AES-256 tự viết với chế độ CBC.";
		byte[] plaintext = originalText.getBytes(StandardCharsets.UTF_8);

		// Khóa gốc 256-bit (32 bytes) và IV (16 bytes)
		byte[] key = AesUtil.hexToBytes("603DEB1015CA71BE2B73AEF0857D77811F352C073B6108D72D9810A30914DFF4");
		byte[] iv = AesUtil.hexToBytes("000102030405060708090A0B0C0D0E0F");

		// Mã hóa
		byte[] ciphertext = AesCore.encrypt(plaintext, key, iv, CipherMode.CBC, 256);

		// Giải mã
		byte[] decryptedBytes = AesCore.decrypt(ciphertext, key, iv, CipherMode.CBC, 256);
		String decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);

		Assertions.assertEquals(originalText, decryptedText);
	}

}

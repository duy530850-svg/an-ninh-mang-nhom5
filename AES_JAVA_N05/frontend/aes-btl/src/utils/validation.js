import * as z from 'zod';

// Hàm kiểm tra logic ràng buộc chéo của Khóa
const refineCryptoRequest = (data) => {
  const keyBytesNeeded = data.keySize / 8;
  if (data.keyFormat === 'HEX' && data.key.length !== keyBytesNeeded * 2) return false;
  if (data.keyFormat === 'PLAIN_TEXT' && data.key.length !== keyBytesNeeded) return false;
  return true;
};

// Hàm kiểm tra logic ràng buộc chéo của véc-tơ IV (CBC)
const refineIvRequest = (data) => {
  if (data.mode === 'CBC') {
    if (!data.iv || data.iv.trim() === '') return false;
    if (data.ivFormat === 'HEX' && data.iv.length !== 32) return false;
    if (data.ivFormat === 'PLAIN_TEXT' && data.iv.length !== 16) return false;
  }
  return true;
};

// 1. Schema độc lập cho Bảng Cấu hình (Cột trái)
export const configSchema = z.object({
  keySize: z.number().refine(val => [128, 192, 256].includes(val), { message: "Kích thước khóa không hợp lệ." }),
  mode: z.enum(['ECB', 'CBC']),
  keyFormat: z.enum(['PLAIN_TEXT', 'HEX', 'BASE64']),
  key: z.string().min(1, { message: "Khóa mật mã không được để trống." }),
  ivFormat: z.enum(['PLAIN_TEXT', 'HEX', 'BASE64']).optional(),
  iv: z.string().optional()
})
.refine(refineCryptoRequest, { message: "Độ dài khóa không hợp lệ (Hãy check lại kích thước).", path: ["key"] })
.refine(refineIvRequest, { message: "Chế độ CBC yêu cầu IV dài đúng 16 byte.", path: ["iv"] });

// 2. Schema độc lập cho Tab Mã hóa
export const encryptSchema = z.object({
  plaintext: z.string().min(1, { message: "Văn bản rõ không được để trống." }),
  outputFormat: z.enum(['HEX', 'BASE64'])
});

// 3. Schema độc lập cho Tab Giải mã
export const decryptSchema = z.object({
  ciphertext: z.string().min(1, { message: "Bản mã không được để trống." }),
  inputFormat: z.enum(['HEX', 'BASE64'])
});
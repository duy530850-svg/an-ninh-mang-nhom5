package com.txt1stparkuor.aes.controller;

import com.txt1stparkuor.aes.dto.CryptoResponse;
import com.txt1stparkuor.aes.dto.DecryptionRequest;
import com.txt1stparkuor.aes.dto.EncryptionRequest;
import com.txt1stparkuor.aes.dto.ParameterResponse;
import com.txt1stparkuor.aes.service.AesService;
import com.txt1stparkuor.aes.utils.AesUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Hỗ trợ React gọi API không bị lỗi CORS
public class AesController {

    private final AesService aesService;

    @PostMapping("/encrypt")
    public ResponseEntity<CryptoResponse> encrypt(@Valid @RequestBody EncryptionRequest request) {
        CryptoResponse response = aesService.encrypt(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/decrypt")
    public ResponseEntity<CryptoResponse> decrypt(@Valid @RequestBody DecryptionRequest request) {
        CryptoResponse response = aesService.decrypt(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/generate-params")
    public ResponseEntity<ParameterResponse> generateParams(
            @RequestParam int keySize,
            @RequestParam boolean needIv) {

        int keyByteLength = keySize / 8;
        String actualKey = AesUtil.generateRandomHex(keyByteLength);
        String actualIv = needIv ? AesUtil.generateRandomHex(16) : "";

        return ResponseEntity.ok(new ParameterResponse(actualKey, actualIv));
    }
}
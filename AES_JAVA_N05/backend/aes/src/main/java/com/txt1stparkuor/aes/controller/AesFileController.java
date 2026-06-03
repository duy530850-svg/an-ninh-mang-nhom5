package com.txt1stparkuor.aes.controller;


import com.txt1stparkuor.aes.dto.FileCryptoRequest;
import com.txt1stparkuor.aes.service.AesFileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aes/file")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AesFileController {

    private final AesFileService aesFileService;

    @PostMapping("/encrypt")
    public void encryptFile(
            @Valid @ModelAttribute FileCryptoRequest request,
            HttpServletResponse response) throws Exception {
        aesFileService.processFile(
                request.getFile(),
                request.getKey(),
                request.getKeySize(),
                request.getMode().name(),
                request.getIv(),
                true,
                response
        );
    }

    @PostMapping("/decrypt")
    public void decryptFile(
            @Valid @ModelAttribute FileCryptoRequest request,
            HttpServletResponse response) throws Exception {
        aesFileService.processFile(
                request.getFile(),
                request.getKey(),
                request.getKeySize(),
                request.getMode().name(),
                request.getIv(),
                false,
                response
        );
    }
}
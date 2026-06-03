package com.txt1stparkuor.aes.exception;

import com.txt1stparkuor.aes.dto.CryptoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CryptoResponse> handleIllegalArgument(IllegalArgumentException e) {
        CryptoResponse response = new CryptoResponse(false, null, "Yêu cầu không hợp lệ: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(CryptoException.class)
    public ResponseEntity<CryptoResponse> handleCryptoException(CryptoException e) {
        CryptoResponse response = new CryptoResponse(false, null, "Lỗi mã hóa/giải mã: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CryptoResponse> handleGenericException(Exception e) {
        CryptoResponse response = new CryptoResponse(false, null, "Lỗi hệ thống không mong muốn: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CryptoResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(" "));

        CryptoResponse response = new CryptoResponse(false, null, "Dữ liệu không hợp lệ: " + errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}

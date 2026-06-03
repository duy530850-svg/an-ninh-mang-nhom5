package com.txt1stparkuor.aes.mapper;

import com.txt1stparkuor.aes.dto.DecryptionRequest;
import com.txt1stparkuor.aes.dto.EncryptionRequest;
import com.txt1stparkuor.aes.enums.DataFormat;
import com.txt1stparkuor.aes.model.AesContext;
import com.txt1stparkuor.aes.utils.AesUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AesMapper {

    @Mapping(target = "data", source = "plaintext", qualifiedByName = "plainTextToBytes")
    @Mapping(target = "key", source = "request", qualifiedByName = "requestKeyToBytes")
    @Mapping(target = "iv", source = "request", qualifiedByName = "requestIvToBytes")
    AesContext toAesContext(EncryptionRequest request);

    @Mapping(target = "data", source = "request", qualifiedByName = "requestCiphertextToBytes")
    @Mapping(target = "key", source = "request", qualifiedByName = "requestKeyToBytes")
    @Mapping(target = "iv", source = "request", qualifiedByName = "requestIvToBytes")
    AesContext toAesContext(DecryptionRequest request);

    @Named("plainTextToBytes")
    default byte[] plainTextToBytes(String plaintext) {
        return AesUtil.convertToBytes(plaintext, DataFormat.PLAIN_TEXT);
    }

    @Named("requestKeyToBytes")
    default byte[] requestKeyToBytes(EncryptionRequest request) {
        return AesUtil.convertToBytes(request.getKey(), request.getKeyFormat());
    }

    @Named("requestKeyToBytes")
    default byte[] requestKeyToBytes(DecryptionRequest request) {
        return AesUtil.convertToBytes(request.getKey(), request.getKeyFormat());
    }

    @Named("requestIvToBytes")
    default byte[] requestIvToBytes(EncryptionRequest request) {
        if (request.getIv() == null || request.getIv().isEmpty()) {
            return null;
        }
        return AesUtil.convertToBytes(request.getIv(), request.getIvFormat());
    }

    @Named("requestIvToBytes")
    default byte[] requestIvToBytes(DecryptionRequest request) {
        if (request.getIv() == null || request.getIv().isEmpty()) {
            return null;
        }
        return AesUtil.convertToBytes(request.getIv(), request.getIvFormat());
    }

    @Named("requestCiphertextToBytes")
    default byte[] requestCiphertextToBytes(DecryptionRequest request) {
        return AesUtil.convertToBytes(request.getCiphertext(), request.getInputFormat());
    }
}
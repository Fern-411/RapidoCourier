package com.rapidocourier.servicio_envios.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;
    
    @Value("${cloudflare.r2.public-url-prefix}")
    private String publicUrlPrefix; // Ejemplo: https://pub-xxxxxxxxxxxxx.r2.dev

    public String subirDocumento(MultipartFile file, String prefix) {
        String fileName = prefix + "-" + UUID.randomUUID() + ".pdf";

        try {
            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putOb, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            // Retorna la URL pública
            return publicUrlPrefix + "/" + fileName;
            
        } catch (S3Exception | IOException e) {
            log.error("Error al subir archivo a R2", e);
            throw new RuntimeException("Error al subir el archivo: " + e.getMessage());
        }
    }
}

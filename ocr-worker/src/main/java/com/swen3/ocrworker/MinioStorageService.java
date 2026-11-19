package com.swen3.ocrworker;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class MinioStorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    // Inject the MinIO client and the configured bucket name.
    public MinioStorageService(
            MinioClient minioClient,
            @Value("${minio.bucket-name}") String bucketName) {

        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    // Download a PDF from the configured MinIO bucket.
    public byte[] downloadPdf(String storedFilename) throws IOException {
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(storedFilename)
                        .build()
        )) {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            throw new IOException("Failed to download PDF from MinIO", e);
        }
    }
}
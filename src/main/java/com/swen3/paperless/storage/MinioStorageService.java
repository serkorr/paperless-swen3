package com.swen3.paperless.storage;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;

import java.io.IOException;

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

    // Initialize the MinIO bucket when the application starts.
    @Bean
    public ApplicationRunner initializeMinioBucket() {
        return args -> createBucketIfMissing();
    }

    // Upload a PDF file to the configured MinIO bucket.
    public void uploadPdf(MultipartFile file, String storedFilename)
            throws IOException {

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storedFilename)
                            .stream(
                                    file.getInputStream(),
                                    file.getSize(),
                                    -1
                            )
                            .contentType("application/pdf")
                            .build()
            );

        } catch (Exception e) {
            throw new IOException("Failed to upload PDF to MinIO", e);
        }
    }

    // Delete a PDF object from the configured MinIO bucket.
    public void deletePdf(String storedFilename) throws IOException {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storedFilename)
                            .build()
            );
        } catch (Exception e) {
            throw new IOException("Failed to delete PDF from MinIO", e);
        }
    }
    // Create the MinIO bucket if it does not already exist.
    public void createBucketIfMissing() throws IOException {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
            }
        } catch (Exception e) {
            throw new IOException("Failed to initialize MinIO bucket", e);
        }
    }
}
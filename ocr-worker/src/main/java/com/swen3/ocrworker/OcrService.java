package com.swen3.ocrworker;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Service
public class OcrService {

    // Convert PDF pages to PNG images using Ghostscript.
    public void convertPdfToImages(Path pdfPath, Path outputDirectory)
            throws IOException, InterruptedException {

        Files.createDirectories(outputDirectory);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "gs",
                "-dSAFER",
                "-dBATCH",
                "-dNOPAUSE",
                "-sDEVICE=pnggray",
                "-r300",
                "-sOutputFile=" + outputDirectory.resolve("page-%03d.png"),
                pdfPath.toString()
        );

        processBuilder.inheritIO();

        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException(
                    "Ghostscript failed with exit code: " + exitCode
            );
        }
    }

    // Extract text from a PNG image using Tesseract.
    public String extractTextFromImage(Path imagePath)
            throws IOException, InterruptedException {

        ProcessBuilder processBuilder = new ProcessBuilder(
                "tesseract",
                imagePath.toString(),
                "stdout",
                "-l",
                "eng"
        );

        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process process = processBuilder.start();

        String extractedText;

        try (var inputStream = process.getInputStream()) {
            extractedText = new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException(
                    "Tesseract failed with exit code: " + exitCode
            );
        }

        return extractedText;
    }


    // Extract text from every page of a PDF document.
    public String extractTextFromPdf(byte[] pdfBytes)
            throws IOException, InterruptedException {

        Path tempDirectory = Files.createTempDirectory("paperless-ocr-");

        try {
            Path pdfPath = tempDirectory.resolve("document.pdf");
            Path imageDirectory = tempDirectory.resolve("images");

            // Save the downloaded PDF temporarily.
            Files.write(pdfPath, pdfBytes);

            // Convert all PDF pages into PNG images.
            convertPdfToImages(pdfPath, imageDirectory);

            List<Path> imagePaths;
            try (Stream<Path> files = Files.list(imageDirectory)) {
                imagePaths = files
                        .filter(path -> path.toString().endsWith(".png"))
                        .sorted()
                        .toList();
            }

            // Read each page and combine the extracted text.
            StringBuilder text = new StringBuilder();

            for (Path imagePath : imagePaths) {
                text.append(extractTextFromImage(imagePath));
                text.append(System.lineSeparator());
            }

            return text.toString();

        } finally {
            // Remove temporary PDF and image files after OCR.
            try (Stream<Path> files = Files.walk(tempDirectory)) {
                for (Path path : files.sorted(java.util.Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }
}

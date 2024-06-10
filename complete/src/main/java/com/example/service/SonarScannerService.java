package com.example.service;

import com.example.entity.ScannerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * @author ishrat.jahan
 * @since 06/08,2024
 */
@Service
public class SonarScannerService {

    @Autowired
    private ScannerResultService resultService;

    public CompletableFuture<Void> runSonarScannerAsync(String projectName, String directoryPath) {
        return CompletableFuture.runAsync(() -> {
            try {

                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("sonar-scanner");
                processBuilder.directory(new File(directoryPath));
                Process process = processBuilder.start();

                StringBuilder terminalOutput = new StringBuilder();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        terminalOutput.append(line).append("\n");
                    }
                }

                int exitCode = process.waitFor();
                terminalOutput.append("SonarScanner exited with code: ").append(exitCode);

                // Save output to a temporary file
                Path outputFile = Files.createTempFile("sonar-output", ".txt");
                Files.write(outputFile, terminalOutput.toString().getBytes());
                byte[] fileContent = Files.readAllBytes(outputFile);

                ScannerResult result = new ScannerResult();
                result.setProjectName(projectName);
                result.setFileContent(fileContent);
                result.setFileName(outputFile.getFileName().toString() + "-" + result.getFormattedTimestamp());

                resultService.saveResult(result);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }
}

package com.example.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.zip.ZipInputStream;

/**
 * @author ishrat.jahan
 * @since 06/08,2024
 */
public class FileUtils {

    private FileUtils() {
    }

    public static boolean isZipFile(MultipartFile file) {

        try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {

            return Objects.nonNull(zipInputStream.getNextEntry());

        } catch (IOException e) {
            return false;
        }
    }

}

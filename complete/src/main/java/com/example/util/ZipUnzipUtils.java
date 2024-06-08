package com.example.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author ishrat.jahan
 * @since 06/08,2024
 */
public class ZipUnzipUtils {

    private ZipUnzipUtils() {
    }

    public static void unzip(String zipFilePath, String unzipDirectoryPath) throws IOException {

        Path destDir = Paths.get(unzipDirectoryPath);
        if (!Files.exists(destDir)) {
            Files.createDirectories(destDir);
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {

            ZipEntry zipEntry = zis.getNextEntry();
            while (Objects.nonNull(zipEntry)) {

                Path newPath = zipSlipProtect(zipEntry, destDir);

                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newPath);

                } else {

                    if (newPath.getParent() != null) {
                        if (Files.notExists(newPath.getParent())) {
                            Files.createDirectories(newPath.getParent());
                        }
                    }

                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newPath.toFile()))) {
                        byte[] bytesIn = new byte[1024];
                        int read;
                        while ((read = zis.read(bytesIn)) != -1) {
                            bos.write(bytesIn, 0, read);
                        }
                    }
                }

                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir) throws IOException {

        Path targetDirResolved = targetDir.resolve(zipEntry.getName());
        Path normalizePath = targetDirResolved.normalize();

        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }

        return normalizePath;
    }
}

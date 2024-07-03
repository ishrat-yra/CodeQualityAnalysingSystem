package com.example.util;

/**
 * @author ishrat.jahan
 * @since 07/02,2024
 */
public class CommonUtils {

    private CommonUtils() {
    }

    public static boolean isLinux() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("linux") || osName.contains("nix") || osName.contains("aix");
    }

    public static boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("win");
    }

    public static boolean isMac() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("mac");
    }
}

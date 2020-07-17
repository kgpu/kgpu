package io.github.kgpu.wgpuj.util;

public class Platform {

    public static final boolean isWindows = System.getProperty("os.name").contains("Windows");
    public static final boolean isLinux = System.getProperty("os.name").contains("Linux");
    public static final boolean isMac = System.getProperty("os.name").contains("Mac");

}
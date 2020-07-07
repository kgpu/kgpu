package com.noahcharlton.wgpuj.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

/**
 * Extracts native libraries from the classpath and stores them temporarily.
 *
 * @author mzechner
 * @author Nathan Sweet
 * @author Noah Charlton
 */
public class SharedLibraryLoader {

    private static final boolean isWindows = System.getProperty("os.name").contains("Windows");
    private static final boolean isLinux = System.getProperty("os.name").contains("Linux");
    private static final boolean isMac = System.getProperty("os.name").contains("Mac");

    private String crc(InputStream input) {
        if(input == null) throw new IllegalArgumentException("input cannot be null.");
        CRC32 crc = new CRC32();
        byte[] buffer = new byte[4096];
        try {
            while(true) {
                int length = input.read(buffer);
                if(length == -1) break;
                crc.update(buffer, 0, length);
            }
        } catch(Exception ex) {
        } finally {
            closeQuietly(input);
        }
        return Long.toString(crc.getValue(), 16);
    }

    private String mapLibraryName(String libraryName) {
        if(isWindows) return libraryName + ".dll";
        if(isLinux) return "lib" + libraryName + ".so";
        if(isMac) return "lib" + libraryName + ".dylib";

        return libraryName;
    }

    /**
     * Extracts the source file and calls System.load.
     */
    public File load(String libraryName) {
        String platformName = mapLibraryName(libraryName);


        try {
            var file = loadFile(platformName);

            return file;
        } catch(Throwable ex) {
            throw new RuntimeException("Couldn't load shared library '" + platformName + "' for target: "
                    + System.getProperty("os.name"), ex);
        }
    }

    private InputStream readFile(String path) {
            InputStream input = SharedLibraryLoader.class.getResourceAsStream("/" + path);
            if(input == null) throw new RuntimeException("Unable to read file for extraction: " + path);
            return input;

    }

    private File extractFile(String sourcePath, String sourceCrc, File extractedFile) {
        String extractedCrc = null;
        if(extractedFile.exists()) {
            try {
                extractedCrc = crc(new FileInputStream(extractedFile));
            } catch(FileNotFoundException ignored) {
            }
        }

        // If file doesn't exist or the CRC doesn't match, extract it to the temp dir.
        if(extractedCrc == null || !extractedCrc.equals(sourceCrc)) {
            InputStream input = null;
            FileOutputStream output = null;
            try {
                input = readFile(sourcePath);
                extractedFile.getParentFile().mkdirs();
                output = new FileOutputStream(extractedFile);
                byte[] buffer = new byte[4096];
                while(true) {
                    int length = input.read(buffer);
                    if(length == -1) break;
                    output.write(buffer, 0, length);
                }
            } catch(IOException ex) {
                throw new RuntimeException("Error extracting file: " + sourcePath + "\nTo: " + extractedFile.getAbsolutePath(),
                        ex);
            } finally {
                closeQuietly(input);
                closeQuietly(output);
            }
        }

        return extractedFile;
    }

    private void closeQuietly(Closeable closeable){
        if(closeable == null)
            return;

        try{
            closeable.close();
        } catch(IOException e) {
            System.err.println("Failed to close dll: " + e);
        }
    }

    private File loadFile(String sourcePath) {
        String sourceCrc = crc(readFile(sourcePath));

        String fileName = new File(sourcePath).getName();

        // Temp directory with username in path.
        File file = new File(System.getProperty("java.io.tmpdir") + "/wgpuj/" + System.getProperty("user.name") + "/" + sourceCrc,
                fileName);
        Throwable ex = loadFile(sourcePath, sourceCrc, file);
        if(ex == null) return file;

        // System provided temp directory.
        try {
            file = File.createTempFile(sourceCrc, null);
            if(file.delete() && loadFile(sourcePath, sourceCrc, file) == null) return file;
        } catch(Throwable ignored) {
        }

        // User home.
        file = new File(System.getProperty("user.home") + "/.wgpuj/" + sourceCrc, fileName);
        if(loadFile(sourcePath, sourceCrc, file) == null) return file;

        // Relative directory.
        file = new File(".temp/" + sourceCrc, fileName);
        if(loadFile(sourcePath, sourceCrc, file) == null) return file;

        // Fallback to java.library.path location, eg for applets.
        file = new File(System.getProperty("java.library.path"), sourcePath);
        if(file.exists()) {
            System.load(file.getAbsolutePath());
            return file;
        }

        throw new RuntimeException(ex);
    }

    private Throwable loadFile(String sourcePath, String sourceCrc, File extractedFile) {
        try {
            System.load(extractFile(sourcePath, sourceCrc, extractedFile).getAbsolutePath());
            return null;
        } catch(Throwable ex) {
            return ex;
        }
    }
}

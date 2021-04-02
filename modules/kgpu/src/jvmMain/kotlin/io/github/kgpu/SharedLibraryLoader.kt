package io.github.kgpu

import java.io.*
import java.lang.Exception
import java.util.zip.CRC32

/**
 * Extracts native libraries from the classpath and stores them temporarily.
 *
 * @author mzechner
 * @author Nathan Sweet
 * @author Noah Charlton
 */
internal class SharedLibraryLoader {
    private fun crc(input: InputStream?): String {
        if (input == null) throw IllegalArgumentException("input cannot be null.")
        val crc = CRC32()
        val buffer = ByteArray(4096)
        try {
            while (true) {
                val length = input.read(buffer)
                if (length == -1) break
                crc.update(buffer, 0, length)
            }
        } catch (ex: Exception) {
        } finally {
            closeQuietly(input)
        }
        return crc.value.toString(16)
    }

    private fun mapLibraryName(libraryName: String): String {
        if (Platform.isWindows) return "$libraryName.dll"
        if (Platform.isLinux) return "lib$libraryName.so"
        return if (Platform.isMac) "lib$libraryName.dylib" else libraryName
    }

    fun load(libraryName: String): File {
        val platformName = mapLibraryName(libraryName)
        try {
            return loadFile(platformName)
        } catch (ex: Throwable) {
            throw RuntimeException(
                "Couldn't load shared library '"
                        + platformName
                        + "' for target: "
                        + System.getProperty("os.name"),
                ex
            )
        }
    }

    private fun readFile(path: String): InputStream {
        return SharedLibraryLoader::class.java.getResourceAsStream("/$path")
            ?: throw RuntimeException("Unable to read file for extraction: $path")
    }

    private fun extractFile(sourcePath: String, sourceCrc: String, extractedFile: File): File {
        var extractedCrc: String? = null
        if (extractedFile.exists()) {
            try {
                extractedCrc = crc(FileInputStream(extractedFile))
            } catch (ignored: FileNotFoundException) {
            }
        }

        // If file doesn't exist or the CRC doesn't match, extract it to the temp dir.
        if (extractedCrc == null || extractedCrc != sourceCrc) {
            var input: InputStream? = null
            var output: FileOutputStream? = null
            try {
                input = readFile(sourcePath)
                extractedFile.parentFile.mkdirs()
                output = FileOutputStream(extractedFile)
                val buffer = ByteArray(4096)
                while (true) {
                    val length = input.read(buffer)
                    if (length == -1) break
                    output.write(buffer, 0, length)
                }
            } catch (ex: IOException) {
                throw RuntimeException(
                    "Error extracting file: "
                            + sourcePath
                            + "\nTo: "
                            + extractedFile.absolutePath,
                    ex
                )
            } finally {
                closeQuietly(input)
                closeQuietly(output)
            }
        }
        return extractedFile
    }

    private fun closeQuietly(closeable: Closeable?) {
        if (closeable == null) return
        try {
            closeable.close()
        } catch (e: IOException) {
            System.err.println("Failed to close dll: $e")
        }
    }

    private fun loadFile(sourcePath: String): File {
        val sourceCrc = crc(readFile(sourcePath))
        val fileName = File(sourcePath).name

        // Temp directory with username in path.
        var file = File(
            (System.getProperty("java.io.tmpdir")
                    + "/wgpuj/"
                    + System.getProperty("user.name")
                    + "/"
                    + sourceCrc),
            fileName
        )
        val ex = loadFile(sourcePath, sourceCrc, file) ?: return file

        // System provided temp directory.
        try {
            file = File.createTempFile(sourceCrc, null)
            if (file.delete() && loadFile(sourcePath, sourceCrc, file) == null) return file
        } catch (ignored: Throwable) {
        }

        // User home.
        file = File(System.getProperty("user.home") + "/.wgpuj/" + sourceCrc, fileName)
        if (loadFile(sourcePath, sourceCrc, file) == null) return file

        // Relative directory.
        file = File(".temp/$sourceCrc", fileName)
        if (loadFile(sourcePath, sourceCrc, file) == null) return file

        // Fallback to java.library.path location, eg for applets.
        file = File(System.getProperty("java.library.path"), sourcePath)
        if (file.exists()) {
            System.load(file.absolutePath)
            return file
        }
        throw RuntimeException(ex)
    }

    private fun loadFile(sourcePath: String, sourceCrc: String, extractedFile: File): Throwable? {
        return try {
            System.load(extractFile(sourcePath, sourceCrc, extractedFile).absolutePath)
            null
        } catch (ex: Throwable) {
            ex
        }
    }
}
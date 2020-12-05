package io.github.kgpu

expect object KgpuFiles {

    /**
     * Loads a file into bytes
     *
     * @param path path to the file. On the JS backend, the path is relative to the HTML page
     * currently loaded. On the JVM backend, the path is relative to the root of the classpath
     *
     * @see loadInternalUtf8
     */
    suspend fun loadInternal(path: String): ByteArray

    suspend fun loadInternalUtf8(path: String): String
}

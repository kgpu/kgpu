package io.github.kgpu.wgpuj;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class LibraryVerificationTests {

    @ParameterizedTest
    @ValueSource(strings = {
            "/libwgpu_native.dylib",
            "/libwgpu_native.so",
            "/wgpu_native.dll"
    })
    void linuxLibraryTest(String path) {
        var url = LibraryVerificationTests.class.getResource(path);

        Assertions.assertNotNull(url, "Failed to find file: " + path);
    }
}

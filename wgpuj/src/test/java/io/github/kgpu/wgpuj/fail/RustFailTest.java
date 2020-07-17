package io.github.kgpu.wgpuj.fail;

import io.github.kgpu.wgpuj.WgpuNativeTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RustFailTest extends WgpuNativeTest {

    @Test
    void rustFailTest() {
        Assertions.assertThrows(RustTestException.class, () -> wgpuTest.rust_fails_test());
    }
}

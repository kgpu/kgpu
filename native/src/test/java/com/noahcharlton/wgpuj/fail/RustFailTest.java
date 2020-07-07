package com.noahcharlton.wgpuj.fail;

import com.noahcharlton.wgpuj.WgpuNativeTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RustFailTest extends WgpuNativeTest {

    @Test
    void rustFailTest() {
        Assertions.assertThrows(RustTestException.class, () -> wgpuTest.rust_fails_test());
    }
}

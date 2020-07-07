package com.noahcharlton.wgpuj;

import com.noahcharlton.wgpuj.util.RustCString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PrimitiveTests extends WgpuNativeTest {

    @Test
    void rustReturnsTrue() {
        Assertions.assertTrue(wgpuTest.rust_returns_true());
    }

    @Test
    void rustReturnsFalse() {
        Assertions.assertFalse(wgpuTest.rust_returns_false());
    }

    @Test
    void rustReturnsFoobarString() {
        var pointer = wgpuTest.rust_returns_foobar_string();
        Assertions.assertEquals("foobar", RustCString.fromPointer(pointer));
    }

    @Test
    void javaGivesRustFoobarString() {
        var pointer = RustCString.toPointer("foobar");

        wgpuTest.java_gives_foobar_string(pointer);
    }
}

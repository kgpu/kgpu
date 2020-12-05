package io.github.kgpu.wgpuj.jni;

import io.github.kgpu.wgpuj.util.RustCString;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.Delegate;

public interface LogCallback {

    @Delegate
    void log(WgpuLogLevel level, Pointer message);

    static LogCallback createDefault() {
        return (level, pointer) -> {
            String message = RustCString.fromPointer(pointer);

            System.out.printf("%s: %s\n", level, message);
        };
    }
}

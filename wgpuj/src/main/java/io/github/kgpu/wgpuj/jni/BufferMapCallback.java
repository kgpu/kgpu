package io.github.kgpu.wgpuj.jni;

import jnr.ffi.Pointer;
import jnr.ffi.annotations.Delegate;

public interface BufferMapCallback {

    @Delegate
    void callback(WgpuBufferMapAsyncStatus status, Pointer userdata);
}

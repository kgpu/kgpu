package com.noahcharlton.wgpuj.jni;

import jnr.ffi.Pointer;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.types.u_int64_t;

public interface RequestAdapterCallback {

    @Delegate
    void request_adapter_callback(@u_int64_t long received, Pointer userData);
}

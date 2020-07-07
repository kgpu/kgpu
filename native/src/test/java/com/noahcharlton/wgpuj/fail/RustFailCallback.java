package com.noahcharlton.wgpuj.fail;

import com.noahcharlton.wgpuj.util.RustCString;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.Delegate;

public interface RustFailCallback {

    @Delegate
    void fail(Pointer msg);

    class RustFailCallbackImpl implements RustFailCallback{

        @Override
        public void fail(Pointer msg) {
            String message = RustCString.fromPointer(msg);

            throw new RustTestException("Failure from Rust Code: " + message);
        }
    }
}

package com.noahcharlton.wgpuj;

import com.noahcharlton.wgpuj.fail.RustFailCallback;
import com.noahcharlton.wgpuj.util.SharedLibraryLoader;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;

public class WgpuNativeTest {

    private static final RustFailCallback callback = new RustFailCallback.RustFailCallbackImpl();

    protected static WgpuTest wgpuTest;

    static {
        try{
            var file = new SharedLibraryLoader().load("wgpu_test");

            wgpuTest = LibraryLoader.create(WgpuTest.class).load(file.getAbsolutePath());
            WgpuJava.setRuntime(Runtime.getRuntime(wgpuTest));

            wgpuTest.set_fail_callback(callback);
        }catch(Exception e){
            System.err.println("Failed to initialize wgpu test library!");

            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }

    protected static Pointer longAsPointer(long address) {
        return Pointer.wrap(WgpuJava.getRuntime(), address);
    }
}

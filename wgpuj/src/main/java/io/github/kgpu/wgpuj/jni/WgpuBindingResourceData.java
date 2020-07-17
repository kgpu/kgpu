package io.github.kgpu.wgpuj.jni;

import io.github.kgpu.wgpuj.WgpuJava;
import jnr.ffi.Union;

public class WgpuBindingResourceData extends Union {

    private final Unsigned64 samplerId = new Unsigned64();
    private final WgpuBufferBinding binding = inner(WgpuBufferBinding.createHeap());
    private final Unsigned64 textureViewId = new Unsigned64();

    protected WgpuBindingResourceData() {
        super(WgpuJava.getRuntime());
    }

    public void setSamplerId(long id){
        samplerId.set(id);
    }

    public void setTextureViewId(long id){
        textureViewId.set(id);
    }

    public WgpuBufferBinding getBinding() {
        return binding;
    }

    public Unsigned64 getSamplerId() {
        return samplerId;
    }
}

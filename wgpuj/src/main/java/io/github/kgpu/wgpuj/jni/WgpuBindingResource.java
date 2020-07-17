package io.github.kgpu.wgpuj.jni;

import io.github.kgpu.wgpuj.util.WgpuJavaStruct;
import jnr.ffi.Struct;

public class WgpuBindingResource extends WgpuJavaStruct {

    private final Struct.Enum<WgpuBindingResourceTag> tag = new Struct.Enum<>(WgpuBindingResourceTag.class);
    private final WgpuBindingResourceData id = inner(new WgpuBindingResourceData());

    public static WgpuBindingResource createHeap() {
        return new WgpuBindingResource();
    }

    public void setTag(WgpuBindingResourceTag tag){
        this.tag.set(tag);
    }

    public WgpuBindingResourceData getData() {
        return id;
    }
}

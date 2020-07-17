package io.github.kgpu.wgpuj.jni;

import io.github.kgpu.wgpuj.WgpuJava;
import io.github.kgpu.wgpuj.util.WgpuJavaStruct;
import jnr.ffi.Struct;

public class WgpuRenderPassDescriptor extends WgpuJavaStruct {

    private final Struct.StructRef<WgpuRenderPassColorDescriptor> colorAttachments =
            new Struct.StructRef<>(WgpuRenderPassColorDescriptor.class);
    private final Struct.UnsignedLong colorAttachmentsLength = new Struct.UnsignedLong();
    private final Struct.Pointer depthStencilAttachment = new Struct.Pointer();



    public WgpuRenderPassDescriptor(WgpuRenderPassDepthStencilDescriptor depthStencilDescriptor,
                                    WgpuRenderPassColorDescriptor... colorAttachments) {
        this(true);

        this.colorAttachments.set(colorAttachments);
        this.colorAttachmentsLength.set(colorAttachments.length);
        this.depthStencilAttachment.set(WgpuJava.createNullPointer());

        if(depthStencilDescriptor != null)
            throw new UnsupportedOperationException("Depth stencil not implemented!");
    }

    public WgpuRenderPassDescriptor(boolean useDirect) {
        if(useDirect)
            useDirectMemory();
    }

    public WgpuRenderPassDescriptor() {
        this(true);
    }

    public StructRef<WgpuRenderPassColorDescriptor> getColorAttachments() {
        return colorAttachments;
    }

    public UnsignedLong getColorAttachmentsLength() {
        return colorAttachmentsLength;
    }
}

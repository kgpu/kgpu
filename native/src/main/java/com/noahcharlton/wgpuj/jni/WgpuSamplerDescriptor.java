package com.noahcharlton.wgpuj.jni;

import com.noahcharlton.wgpuj.util.WgpuJavaStruct;
import com.noahcharlton.wgpuj.util.CStrPointer;
import com.noahcharlton.wgpuj.util.RustCString;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class WgpuSamplerDescriptor extends WgpuJavaStruct {

    /**
     * This is a pointer because of unknown bug that is crashing for
     * self referencing structs.
     *
     * See https://github.com/DevOrc/wgpu-java/issues/24
     */
    private final Struct.Pointer nextInChain = new Struct.Pointer();
    private final @CStrPointer Struct.Pointer label = new Struct.Pointer();
    private final Struct.Enum<WgpuAddressMode> addressModeU = new Struct.Enum<>(WgpuAddressMode.class);
    private final Struct.Enum<WgpuAddressMode> addressModeV = new Struct.Enum<>(WgpuAddressMode.class);
    private final Struct.Enum<WgpuAddressMode> addressModeW = new Struct.Enum<>(WgpuAddressMode.class);
    private final Struct.Enum<WgpuFilterMode> magFilter = new Struct.Enum<>(WgpuFilterMode.class);
    private final Struct.Enum<WgpuFilterMode> minFilter = new Struct.Enum<>(WgpuFilterMode.class);
    private final Struct.Enum<WgpuFilterMode> mipmapFilter = new Struct.Enum<>(WgpuFilterMode.class);
    private final Struct.Float lodMinClamp = new Struct.Float();
    private final Struct.Float lodMaxClamp = new Struct.Float();
    private final Struct.Enum<WgpuCompareFunction> compare = new Struct.Enum<>(WgpuCompareFunction.class);

    private WgpuSamplerDescriptor(){}

    @Deprecated
    public WgpuSamplerDescriptor(Runtime runtime){
        super(runtime);
    }

    public static WgpuSamplerDescriptor createHeap(){
        return new WgpuSamplerDescriptor();
    }

    public static WgpuSamplerDescriptor createDirect(){
        var struct = new WgpuSamplerDescriptor();
        struct.useDirectMemory();
        return struct;
    }

    public void setNextInChain(jnr.ffi.Pointer value){
        nextInChain.set(value);
    }

    public Pointer getNextInChain() {
        return nextInChain;
    }

    public java.lang.String getLabel(){
        return RustCString.fromPointer(label.get());
    }

    public void setLabel(java.lang.String x){
        this.label.set(RustCString.toPointer(x));
    }

    public WgpuAddressMode getAddressModeU(){
        return addressModeU.get();
    }

    public void setAddressModeU(WgpuAddressMode x){
        this.addressModeU.set(x);
    }

    public WgpuAddressMode getAddressModeV(){
        return addressModeV.get();
    }

    public void setAddressModeV(WgpuAddressMode x){
        this.addressModeV.set(x);
    }

    public WgpuAddressMode getAddressModeW(){
        return addressModeW.get();
    }

    public void setAddressModeW(WgpuAddressMode x){
        this.addressModeW.set(x);
    }

    public WgpuFilterMode getMagFilter(){
        return magFilter.get();
    }

    public void setMagFilter(WgpuFilterMode x){
        this.magFilter.set(x);
    }

    public WgpuFilterMode getMinFilter(){
        return minFilter.get();
    }

    public void setMinFilter(WgpuFilterMode x){
        this.minFilter.set(x);
    }

    public WgpuFilterMode getMipmapFilter(){
        return mipmapFilter.get();
    }

    public void setMipmapFilter(WgpuFilterMode x){
        this.mipmapFilter.set(x);
    }

    public float getLodMinClamp(){
        return lodMinClamp.get();
    }

    public void setLodMinClamp(float x){
        this.lodMinClamp.set(x);
    }

    public float getLodMaxClamp(){
        return lodMaxClamp.get();
    }

    public void setLodMaxClamp(float x){
        this.lodMaxClamp.set(x);
    }

    public WgpuCompareFunction getCompare(){
        return compare.get();
    }

    public void setCompare(WgpuCompareFunction x){
        this.compare.set(x);
    }

}
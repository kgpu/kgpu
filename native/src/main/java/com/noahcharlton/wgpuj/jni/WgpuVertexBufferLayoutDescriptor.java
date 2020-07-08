package com.noahcharlton.wgpuj.jni;

import com.noahcharlton.wgpuj.WgpuJava;
import com.noahcharlton.wgpuj.util.WgpuJavaStruct;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

/** NOTE: THIS FILE WAS PRE-GENERATED BY JNR_GEN! */
public class WgpuVertexBufferLayoutDescriptor extends WgpuJavaStruct {

    private final Struct.Unsigned64 arrayStride = new Struct.Unsigned64();
    private final Struct.Enum<WgpuInputStepMode> stepMode = new Struct.Enum<>(WgpuInputStepMode.class);
    private final DynamicStructRef<WgpuVertexAttributeDescriptor> attributes = new DynamicStructRef<>(WgpuVertexAttributeDescriptor.class);
    private final Struct.Unsigned64 attributesLength = new Struct.Unsigned64();

    protected WgpuVertexBufferLayoutDescriptor(boolean direct){
         if(direct){
             useDirectMemory();
        }
    }

    @Deprecated
    public WgpuVertexBufferLayoutDescriptor(Runtime runtime){
        super(runtime);
    }

    /**
    * Creates this struct on the java heap.
    * In general, this should <b>not</b> be used because these structs
    * cannot be directly passed into native code. 
    */
    public static WgpuVertexBufferLayoutDescriptor createHeap(){
        return new WgpuVertexBufferLayoutDescriptor(false);
    }

    /**
    * Creates this struct in direct memory.
    * This is how most structs should be created (unless, they
    * are members of a nothing struct)
    * 
    * @see WgpuJavaStruct#useDirectMemory
    */
    public static WgpuVertexBufferLayoutDescriptor createDirect(){
        return new WgpuVertexBufferLayoutDescriptor(true);
    }


    public long getArrayStride(){
        return arrayStride.get();
    }

    public void setArrayStride(long x){
        this.arrayStride.set(x);
    }

    public WgpuInputStepMode getStepMode(){
        return stepMode.get();
    }

    public void setStepMode(WgpuInputStepMode x){
        this.stepMode.set(x);
    }

    public DynamicStructRef<WgpuVertexAttributeDescriptor> getAttributes(){
        return attributes;
    }

    public void setAttributes(WgpuVertexAttributeDescriptor... x){
        if(x.length == 0 || x[0] == null){
            this.attributes.set(WgpuJava.createNullPointer());
        } else {
            this.attributes.set(x);
        }
    }

    public long getAttributesLength(){
        return attributesLength.get();
    }

    public void setAttributesLength(long x){
        this.attributesLength.set(x);
    }

}
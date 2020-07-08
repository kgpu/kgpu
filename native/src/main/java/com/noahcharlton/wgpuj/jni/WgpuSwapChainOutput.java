package com.noahcharlton.wgpuj.jni;

import com.noahcharlton.wgpuj.util.WgpuJavaStruct;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

/** NOTE: THIS FILE WAS PRE-GENERATED BY JNR_GEN! */
public class WgpuSwapChainOutput extends WgpuJavaStruct {

    private final Struct.Enum<WgpuSwapChainStatus> status = new Struct.Enum<>(WgpuSwapChainStatus.class);
    private final Struct.Unsigned64 viewId = new Struct.Unsigned64();

    protected WgpuSwapChainOutput(boolean direct){
         if(direct){
             useDirectMemory();
        }
    }

    @Deprecated
    public WgpuSwapChainOutput(Runtime runtime){
        super(runtime);
    }

    /**
    * Creates this struct on the java heap.
    * In general, this should <b>not</b> be used because these structs
    * cannot be directly passed into native code. 
    */
    public static WgpuSwapChainOutput createHeap(){
        return new WgpuSwapChainOutput(false);
    }

    /**
    * Creates this struct in direct memory.
    * This is how most structs should be created (unless, they
    * are members of a nothing struct)
    * 
    * @see WgpuJavaStruct#useDirectMemory
    */
    public static WgpuSwapChainOutput createDirect(){
        return new WgpuSwapChainOutput(true);
    }


    public WgpuSwapChainStatus getStatus(){
        return status.get();
    }

    public void setStatus(WgpuSwapChainStatus x){
        this.status.set(x);
    }

    public long getViewId(){
        return viewId.get();
    }

    public void setViewId(long x){
        this.viewId.set(x);
    }

}
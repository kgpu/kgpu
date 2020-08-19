package io.github.kgpu.wgpuj.jni;

import io.github.kgpu.wgpuj.WgpuJava;
import io.github.kgpu.wgpuj.util.WgpuJavaStruct;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

/** NOTE: THIS FILE WAS PRE-GENERATED BY JNR_GEN! */
public class WgpuRenderPipelineDescriptor extends WgpuJavaStruct {

    private final Struct.Unsigned64 layout = new Struct.Unsigned64();
    private final WgpuProgrammableStageDescriptor vertexStage = inner(WgpuProgrammableStageDescriptor.createHeap());
    private final DynamicStructRef<WgpuProgrammableStageDescriptor> fragmentStage = new DynamicStructRef<>(WgpuProgrammableStageDescriptor.class);
    private final Struct.Enum<WgpuPrimitiveTopology> primitiveTopology = new Struct.Enum<>(WgpuPrimitiveTopology.class);
    private final DynamicStructRef<WgpuRasterizationStateDescriptor> rasterizationState = new DynamicStructRef<>(WgpuRasterizationStateDescriptor.class);
    private final DynamicStructRef<WgpuColorStateDescriptor> colorStates = new DynamicStructRef<>(WgpuColorStateDescriptor.class);
    private final Struct.Unsigned64 colorStatesLength = new Struct.Unsigned64();
    private final DynamicStructRef<WgpuDepthStencilStateDescriptor> depthStencilState = new DynamicStructRef<>(WgpuDepthStencilStateDescriptor.class);
    private final WgpuVertexStateDescriptor vertexState = inner(WgpuVertexStateDescriptor.createHeap());
    private final Struct.Unsigned32 sampleCount = new Struct.Unsigned32();
    private final Struct.Unsigned32 sampleMask = new Struct.Unsigned32();
    private final Struct.Boolean alphaToCoverageEnabled = new Struct.Boolean();

    protected WgpuRenderPipelineDescriptor(boolean direct){
         if(direct){
             useDirectMemory();
        }
    }

    @Deprecated
    public WgpuRenderPipelineDescriptor(Runtime runtime){
        super(runtime);
    }

    /**
    * Creates this struct on the java heap.
    * In general, this should <b>not</b> be used because these structs
    * cannot be directly passed into native code. 
    */
    public static WgpuRenderPipelineDescriptor createHeap(){
        return new WgpuRenderPipelineDescriptor(false);
    }

    /**
    * Creates this struct in direct memory.
    * This is how most structs should be created (unless, they
    * are members of a nothing struct)
    * 
    * @see WgpuJavaStruct#useDirectMemory
    */
    public static WgpuRenderPipelineDescriptor createDirect(){
        return new WgpuRenderPipelineDescriptor(true);
    }


    public long getLayout(){
        return layout.get();
    }

    public void setLayout(long x){
        this.layout.set(x);
    }

    public WgpuProgrammableStageDescriptor getVertexStage(){
        return vertexStage;
    }

    public DynamicStructRef<WgpuProgrammableStageDescriptor> getFragmentStage(){
        return fragmentStage;
    }

    public void setFragmentStage(WgpuProgrammableStageDescriptor... x){
        if(x.length == 0 || x[0] == null){
            this.fragmentStage.set(WgpuJava.createNullPointer());
        } else {
            this.fragmentStage.set(x);
        }
    }

    public WgpuPrimitiveTopology getPrimitiveTopology(){
        return primitiveTopology.get();
    }

    public void setPrimitiveTopology(WgpuPrimitiveTopology x){
        this.primitiveTopology.set(x);
    }

    public DynamicStructRef<WgpuRasterizationStateDescriptor> getRasterizationState(){
        return rasterizationState;
    }

    public void setRasterizationState(WgpuRasterizationStateDescriptor... x){
        if(x.length == 0 || x[0] == null){
            this.rasterizationState.set(WgpuJava.createNullPointer());
        } else {
            this.rasterizationState.set(x);
        }
    }

    public DynamicStructRef<WgpuColorStateDescriptor> getColorStates(){
        return colorStates;
    }

    public void setColorStates(WgpuColorStateDescriptor... x){
        if(x.length == 0 || x[0] == null){
            this.colorStates.set(WgpuJava.createNullPointer());
        } else {
            this.colorStates.set(x);
        }
    }

    public long getColorStatesLength(){
        return colorStatesLength.get();
    }

    public void setColorStatesLength(long x){
        this.colorStatesLength.set(x);
    }

    public DynamicStructRef<WgpuDepthStencilStateDescriptor> getDepthStencilState(){
        return depthStencilState;
    }

    public void setDepthStencilState(WgpuDepthStencilStateDescriptor... x){
        if(x.length == 0 || x[0] == null){
            this.depthStencilState.set(WgpuJava.createNullPointer());
        } else {
            this.depthStencilState.set(x);
        }
    }

    public WgpuVertexStateDescriptor getVertexState(){
        return vertexState;
    }

    public long getSampleCount(){
        return sampleCount.get();
    }

    public void setSampleCount(long x){
        this.sampleCount.set(x);
    }

    public long getSampleMask(){
        return sampleMask.get();
    }

    public void setSampleMask(long x){
        this.sampleMask.set(x);
    }

    public boolean getAlphaToCoverageEnabled(){
        return alphaToCoverageEnabled.get();
    }

    public void setAlphaToCoverageEnabled(boolean x){
        this.alphaToCoverageEnabled.set(x);
    }

}
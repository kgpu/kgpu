package com.noahcharlton.wgpuj;

import com.noahcharlton.wgpuj.jni.*;
import com.noahcharlton.wgpuj.util.RustCString;
import jnr.ffi.Pointer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class WgpuTypeTests extends WgpuNativeTest {

    @Test
    void wgpuColorTest() {
        WgpuColor color = WgpuColor.createDirect();
        color.setR(1.0);
        color.setG(.75);
        color.setB(.5);
        color.setA(.25);

        Pointer output = wgpuTest.color_to_string(color.getPointerTo());

        String actual = RustCString.fromPointer(output);
        String expected = "Color { r: 1.0, g: 0.75, b: 0.5, a: 0.25 }";

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void bindGroupLayoutDescriptorNameTest() {
        var descriptor = WgpuBindGroupLayoutDescriptor.createDirect();
        descriptor.setLabel("foobar9876");

        wgpuTest.bind_group_layout_descriptor_test(descriptor.getPointerTo());
    }

    @Test
    void bindGroupEntryBindingTest() {
        var entry = WgpuBindGroupEntry.createDirect();
        entry.setBinding(654321);

        wgpuTest.bind_group_entry_test_binding(entry.getPointerTo());
    }

    @Test
    void origin3dTest() {
        var origin = WgpuOrigin3d.createDirect();
        origin.setX(123);
        origin.setY(456);
        origin.setZ(789);

        wgpuTest.wgpu_origin_3d_test(origin.getPointerTo());
    }

    @Test
    void extent3dTest() {
        var extent = WgpuExtent3d.createDirect();
        extent.setWidth(147);
        extent.setHeight(258);
        extent.setDepth(369);

        wgpuTest.wgpu_extent_3d_test(extent.getPointerTo());
    }

    @ParameterizedTest
    @MethodSource("getBindGroupEntryStringInputs")
    void bindGroupEntryDataStringTest(Consumer<WgpuBindingResourceData> data, WgpuBindingResourceTag tag,
                                      String expected) {
        var entry = WgpuBindGroupEntry.createDirect();
        entry.getResource().setTag(tag);
        data.accept(entry.getResource().getData());

        Pointer output = wgpuTest.bind_group_entry_resource_to_string(entry.getPointerTo());
        String actual = RustCString.fromPointer(output);

        Assertions.assertEquals(expected, actual);
    }

    private static Stream<Arguments> getBindGroupEntryStringInputs() {
        return Stream.of(
                Arguments.of((Consumer<WgpuBindingResourceData>) data -> data.setSamplerId(745),
                        WgpuBindingResourceTag.SAMPLER, "Sampler((745, 0, Empty))"),
                Arguments.of((Consumer<WgpuBindingResourceData>) data -> data.setTextureViewId(66),
                        WgpuBindingResourceTag.TEXTURE_VIEW, "TextureView((66, 0, Empty))"),
                Arguments.of((Consumer<WgpuBindingResourceData>) data -> {
                            data.getBinding().setSize(26);
                            data.getBinding().setOffset(45);
                            data.getBinding().setBuffer(31);
                        },
                        WgpuBindingResourceTag.BUFFER,
                        "Buffer(BufferBinding { buffer: (31, 0, Empty), offset: 45, size: BufferSize(26) })")
        );
    }

//  --- Blocked by wgpu-native having a private field ---
//  @Test
    void cLimitsMaxBindGroupsTest() {
        var cLimits = WgpuCLimits.createDirect();
        cLimits.setMaxBindGroups(951);

        wgpuTest.wgpu_climits_test(cLimits.getPointerTo());
    }

    @Test
    void blendDescriptorTest() {
        var desc = WgpuBlendDescriptor.createDirect();
        desc.setSrcFactor(WgpuBlendFactor.ZERO);
        desc.setDstFactor(WgpuBlendFactor.SRC_COLOR);
        desc.setOperation(WgpuBlendOperation.SUBTRACT);

        wgpuTest.wgpu_blend_descriptor_test(desc.getPointerTo());
    }

    @Test
    void bufferDescriptorTest() {
        var desc = WgpuBufferDescriptor.createDirect();
        desc.setLabel("ZebraBuffer");
        desc.setSize(98);
        desc.setUsage(Wgpu.BufferUsage.UNIFORM);
        desc.setMappedAtCreation(true);

        wgpuTest.wgpu_buffer_descriptor_test(desc.getPointerTo());
    }

    @Test
    void bufferCopyViewTest() {
        var view = WgpuBufferCopyView.createDirect();

        view.setBuffer(123);
        view.getLayout().setOffset(55);
        view.getLayout().setBytesPerRow(66);
        view.getLayout().setRowsPerImage(77);

        wgpuTest.wgpu_buffer_copy_view_test(view.getPointerTo());
    }

    @Test
    void bindGroupDescriptorTest() {
        var bindGroup = WgpuBindGroupDescriptor.createDirect();
        bindGroup.setLabel("RhinoBindGroup");
        bindGroup.setLayout(31);
        bindGroup.getEntries().set(32);
        bindGroup.setEntriesLength(33);

        wgpuTest.wgpu_bind_group_descriptor_test(bindGroup.getPointerTo());
    }

    @Test
    void colorStateDescriptorTest() {
        var colorState = WgpuColorStateDescriptor.createDirect();
        colorState.setFormat(WgpuTextureFormat.RGBA16_UINT);
        colorState.setWriteMask(Wgpu.ColorWrite.GREEN);
        colorState.getColorBlend().setSrcFactor(WgpuBlendFactor.SRC_ALPHA_SATURATED);
        colorState.getAlphaBlend().setDstFactor(WgpuBlendFactor.ONE_MINUS_SRC_COLOR);

        wgpuTest.wgpu_color_state_descriptor_test(colorState.getPointerTo());
    }

    @Test
    void commandBufferDescriptorTest() {
        var desc = WgpuCommandBufferDescriptor.createDirect();
        desc.setTodo(123);

        wgpuTest.wgpu_command_buffer_descriptor_test(desc.getPointerTo());
    }

    @Test
    void commandEncoderDescriptorTest() {
        var desc = WgpuCommandEncoderDescriptor.createDirect();
        desc.setLabel("LionCommandEncoder");

        wgpuTest.wgpu_command_encoder_descriptor_test(desc.getPointerTo());
    }

    @Test
    void computePipelineDescriptor() {
        var desc = WgpuComputePipelineDescriptor.createDirect();
        desc.setLayout(104);
        desc.getComputeStage().setModule(44);
        desc.getComputeStage().setEntryPoint("WhaleEntryPoint");

        wgpuTest.wgpu_compute_pipeline_descriptor_test(desc.getPointerTo());
    }

    @Test
    void depthStencilStateDescriptorTest() {
        var desc = WgpuDepthStencilStateDescriptor.createDirect();
        desc.setFormat(WgpuTextureFormat.RG8_SNORM);
        desc.setDepthWriteEnabled(true);
        desc.setDepthCompare(WgpuCompareFunction.ALWAYS);
        desc.getStencilFront().setFailOp(WgpuStencilOperation.INVERT);
        desc.getStencilBack().setPassOp(WgpuStencilOperation.REPLACE);
        desc.setStencilReadMask(654);
        desc.setStencilWriteMask(749);

        wgpuTest.wgpu_depth_stencil_state_descriptor(desc.getPointerTo());
    }

    @Test
    void pipelineLayoutDescriptor() {
        var desc = WgpuPipelineLayoutDescriptor.createDirect();
        desc.setBindGroupLayouts(longAsPointer(764));
        desc.setBindGroupLayoutsLength(44);

        wgpuTest.wgpu_pipeline_layout_descriptor(desc.getPointerTo());
    }

    @Test
    void computePassDescriptorTest() {
        var desc = WgpuComputePassDescriptor.createDirect();
        desc.setTodo(554);

        wgpuTest.wgpu_compute_pass_descriptor(desc.getPointerTo());
    }

    @Test
    void rasterizationStateDescriptor() {
        var desc = WgpuRasterizationStateDescriptor.createDirect();
        desc.setFrontFace(WgpuFrontFace.CW);
        desc.setCullMode(WgpuCullMode.FRONT);
        desc.setDepthBias(133);
        desc.setDepthBiasSlopeScale(1.25f);
        desc.setDepthBiasClamp(.75f);

        wgpuTest.wgpu_rasterization_state_descriptor(desc.getPointerTo());
    }

    @Test
    void rawPassTest() {
        var rawPass = WgpuRawPass.createHeap();
        var rawPassPtr = wgpuTest.get_raw_pass_test(55);

        rawPass.useMemory(rawPassPtr);

        Assertions.assertEquals(55, rawPass.getParent());
        Assertions.assertEquals(24, rawPass.getCapacity());
    }

    @Test
    void renderPassTest() {
        var renderPass = new WgpuRenderPassDescriptor();
        renderPass.getColorAttachments().set(123);
        renderPass.getColorAttachmentsLength().set(456);

        wgpuTest.wgpu_render_pass_descriptor_test(renderPass.getPointerTo());
    }

    @Test
    void shaderModuleDescriptorTest() {
        var shaderModule = WgpuShaderModuleDescriptor.createDirect();
        shaderModule.getCode().setBytes(longAsPointer(667));
        shaderModule.getCode().setLength(74);

        wgpuTest.wgpu_shader_module_test(shaderModule.getPointerTo());
    }

    @Test
    void requestAdapterOptionsTest() {
        var options = WgpuRequestAdapterOptions.createDirect();
        options.setPowerPreference(WgpuPowerPreference.LOW_POWER);
        options.setCompatibleSurface(81);

        wgpuTest.wgpu_request_adapter_options_test(options.getPointerTo());
    }

    @Test
    void renderPipelineDescriptorTest() {
        var pipeline = WgpuRenderPipelineDescriptor.createDirect();
        pipeline.setLayout(10);
        pipeline.getVertexStage().setModule(11);
        pipeline.getFragmentStage().set(12);
        pipeline.setPrimitiveTopology(WgpuPrimitiveTopology.POINT_LIST);
        pipeline.getRasterizationState().set(13);
        pipeline.getColorStates().set(14);
        pipeline.setColorStatesLength(15);
        pipeline.getDepthStencilState().set(16);
        pipeline.getVertexState().setVertexBuffersLength(17);
        pipeline.setSampleCount(18);
        pipeline.setSampleMask(19);
        pipeline.setAlphaToCoverageEnabled(true);

        wgpuTest.wgpu_render_pipeline_descriptor_test(pipeline.getPointerTo());
    }

    @Test
    void swapChainOutputTest() {
        var output = WgpuSwapChainOutput.createDirect();
        output.setStatus(WgpuSwapChainStatus.OUT_OF_MEMORY);
        output.setViewId(31);

        wgpuTest.wgpu_swap_chain_output_test(output.getPointerTo());
    }

    @Test
    void swapChainDescriptorTest() {
        var descriptor = WgpuSwapChainDescriptor.createDirect();
        descriptor.setUsage(Wgpu.TextureUsage.STORAGE);
        descriptor.setFormat(WgpuTextureFormat.DEPTH32_FLOAT);
        descriptor.setWidth(43);
        descriptor.setHeight(34);
        descriptor.setPresentMode(WgpuPresentMode.MAILBOX);

        wgpuTest.wgpu_swap_chain_descriptor_test(descriptor.getPointerTo());
    }

    @Test
    void textureViewDescriptorTest() {
        var descriptor = WgpuTextureViewDescriptor.createDirect();
        descriptor.setLabel("CheetahTextureView");
        descriptor.setFormat(WgpuTextureFormat.R8_UNORM);
        descriptor.setDimension(WgpuTextureViewDimension.CUBE_ARRAY);
        descriptor.setAspect(WgpuTextureAspect.STENCIL_ONLY);
        descriptor.setBaseMipLevel(124);
        descriptor.setLevelCount(125);
        descriptor.setBaseArrayLayer(126);
        descriptor.setArrayLayerCount(127);

        wgpuTest.wgpu_texture_view_descriptor_test(descriptor.getPointerTo());
    }

    @Test
    void textureDescriptorTest() {
        var descriptor = WgpuTextureDescriptor.createDirect();
        descriptor.setLabel("PenguinTexture");
        descriptor.getSize().setWidth(123);
        descriptor.getSize().setHeight(456);
        descriptor.getSize().setDepth(789);
        descriptor.setMipLevelCount(150);
        descriptor.setSampleCount(151);
        descriptor.setDimension(WgpuTextureDimension.D3);
        descriptor.setFormat(WgpuTextureFormat.RG11B10_FLOAT);
        descriptor.setUsage(Wgpu.TextureUsage.SAMPLED);

        wgpuTest.wgpu_texture_descriptor_test(descriptor.getPointerTo());
    }

    @Test
    void textureCopyViewTest() {
        var descriptor = WgpuTextureCopyView.createDirect();
        descriptor.setTexture(200);
        descriptor.setMipLevel(201);
        descriptor.getOrigin().setX(741);
        descriptor.getOrigin().setY(852);
        descriptor.getOrigin().setZ(963);

        wgpuTest.wgpu_texture_copy_view_test(descriptor.getPointerTo());
    }

    @Test
    void samplerDescriptorTest() {
        var descriptor = WgpuSamplerDescriptor.createDirect();
        descriptor.setNextInChain(WgpuChainedStruct.createDirect().getPointerTo());
        descriptor.setLabel("OtterSampler");
        descriptor.setAddressModeU(WgpuAddressMode.REPEAT);
        descriptor.setAddressModeV(WgpuAddressMode.CLAMP_TO_EDGE);
        descriptor.setAddressModeW(WgpuAddressMode.MIRROR_REPEAT);
        descriptor.setMagFilter(WgpuFilterMode.NEAREST);
        descriptor.setMinFilter(WgpuFilterMode.NEAREST);
        descriptor.setMipmapFilter(WgpuFilterMode.LINEAR);
        descriptor.setLodMinClamp(23f);
        descriptor.setLodMaxClamp(24f);
        descriptor.setCompare(WgpuCompareFunction.LESS_EQUAL);

        wgpuTest.wgpu_sampler_descriptor_test(descriptor.getPointerTo());
    }

    @Test
    void renderPassDepthStencilDescriptor() {
        var descriptor = WgpuRenderPassDepthStencilDescriptor.createDirect();
        descriptor.setAttachment(123);
        descriptor.setDepthLoadOp(WgpuLoadOp.LOAD);
        descriptor.setDepthStoreOp(WgpuStoreOp.STORE);
        descriptor.setClearDepth(10f);
        descriptor.setDepthReadOnly(true);
        descriptor.setStencilLoadOp(WgpuLoadOp.LOAD);
        descriptor.setStencilStoreOp(WgpuStoreOp.CLEAR);
        descriptor.setClearStencil(11);
        descriptor.setStencilReadOnly(true);

        wgpuTest.wgpu_render_pass_depth_stencil_descriptor(descriptor.getPointerTo());
    }
}

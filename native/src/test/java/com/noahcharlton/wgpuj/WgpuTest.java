package com.noahcharlton.wgpuj;

import com.noahcharlton.wgpuj.fail.RustFailCallback;
import com.noahcharlton.wgpuj.jni.*;
import jnr.ffi.Pointer;

public interface WgpuTest {

    boolean rust_returns_true();

    boolean rust_returns_false();

    Pointer rust_returns_foobar_string();

    void java_gives_foobar_string(Pointer value);

    void rust_fails_test();

    void set_fail_callback(RustFailCallback callback);

    Pointer color_to_string(Pointer color);

    void bind_group_layout_descriptor_test(Pointer bindGroupLayoutDescriptor);

    void bind_group_entry_test_binding(Pointer bindGroupEntry);

    void wgpu_origin_3d_test(Pointer origin3d);

    void wgpu_extent_3d_test(Pointer extent3d);

    void wgpu_climits_test(Pointer cLimits);

    void wgpu_blend_descriptor_test(Pointer blendDesc);

    void wgpu_buffer_descriptor_test(Pointer bufferDesc);

    void wgpu_buffer_copy_view_test(Pointer copyView);

    void wgpu_bind_group_descriptor_test(Pointer bindGroup);

    void wgpu_color_state_descriptor_test(Pointer colorStateDesc);

    void wgpu_command_buffer_descriptor_test(Pointer cmdBufferDesc);

    void wgpu_command_encoder_descriptor_test(Pointer cmdEncoderDesc);

    void wgpu_compute_pipeline_descriptor_test(Pointer computePipelineDesc);

    void wgpu_depth_stencil_state_descriptor(Pointer depthStencilStateDesc);

    void wgpu_pipeline_layout_descriptor(Pointer pipelineLayoutDesc);

    void wgpu_compute_pass_descriptor(Pointer computePasDesc);

    void wgpu_rasterization_state_descriptor(Pointer rasterizationStateDesc);

    void wgpu_render_pass_descriptor_test(Pointer renderPassDesc);

    void wgpu_shader_module_test(Pointer module);

    void wgpu_request_adapter_options_test(Pointer adapter);

    void wgpu_render_pipeline_descriptor_test(Pointer pipeline);

    void wgpu_swap_chain_output_test(Pointer output);

    void wgpu_swap_chain_descriptor_test(Pointer desc);

    void wgpu_texture_view_descriptor_test(Pointer textureView);

    void wgpu_texture_descriptor_test(Pointer textureDesc);

    void wgpu_texture_copy_view_test(Pointer copyView);

    void wgpu_sampler_descriptor_test(Pointer descriptor);

    void wgpu_render_pass_depth_stencil_descriptor(Pointer descriptor);

    Pointer get_raw_pass_test(long cmdEncoderId);

    Pointer bind_group_entry_resource_to_string(Pointer bindGroupEntry);

    Pointer get_power_preference_name(WgpuPowerPreference preference);

    Pointer get_primitive_topology_name(WgpuPrimitiveTopology topology);

    Pointer get_present_mode_name(WgpuPresentMode mode);

    Pointer get_load_op_name(WgpuLoadOp op);

    Pointer get_store_op_name(WgpuStoreOp op);

    Pointer get_blend_factor_name(WgpuBlendFactor factor);

    Pointer get_blend_operation_name(WgpuBlendOperation operation);

    Pointer get_cull_mode_name(WgpuCullMode mode);

    Pointer get_index_format_name(WgpuIndexFormat format);

    Pointer get_front_face_name(WgpuFrontFace face);

    Pointer get_log_level_name(WgpuLogLevel level);

    Pointer get_swap_chain_status_name(WgpuSwapChainStatus status);

    Pointer get_texture_format_name(WgpuTextureFormat name);

    Pointer get_wgpu_binding_type_name(WgpuBindingType type);

    Pointer get_wgpu_buffer_map_async_status_name(WgpuBufferMapAsyncStatus status);

    Pointer get_wgpu_texture_view_dimension_name(WgpuTextureViewDimension dimension);

    Pointer get_wgpu_texture_component_type_name(WgpuTextureComponentType type);

    Pointer get_wgpu_input_step_mode_name(WgpuInputStepMode mode);

    Pointer get_wgpu_vertex_format_name(WgpuVertexFormat format);

    Pointer get_wgpu_texture_dimension_name(WgpuTextureDimension dimension);

    Pointer get_wgpu_texture_aspect_name(WgpuTextureAspect aspect);

    Pointer get_wgpu_stencil_operation_name(WgpuStencilOperation operation);

    Pointer get_wgpu_compare_function_name(WgpuCompareFunction function);

    Pointer get_address_mode_name(WgpuAddressMode mode);

    Pointer get_filter_mode_name(WgpuFilterMode mode);
}

use std::ffi::{CStr, CString};
use std::os::raw::c_char;

#[macro_use]
mod fail;
mod wgpu_enum;

#[no_mangle]
pub unsafe extern "C" fn rust_fails_test() {
    fail::fail_test("test 1234".to_string());
}

#[no_mangle]
pub extern "C" fn rust_returns_true() -> bool {
    true
}

#[no_mangle]
pub extern "C" fn rust_returns_false() -> bool {
    false
}

#[no_mangle]
pub unsafe extern "C" fn rust_returns_foobar_string() -> *const c_char {
    CString::new("foobar").unwrap().into_raw()
}

#[no_mangle]
pub unsafe extern "C" fn java_gives_foobar_string(input: *mut c_char) {
    let c_str = CString::from_raw(input);

    assert_ffi!("foobar".to_string(), c_str.into_string().unwrap());
}

#[no_mangle]
pub extern "C" fn color_to_string(color: &wgt::Color) -> *const c_char {
    CString::new(format!("{:?}", color)).unwrap().into_raw()
}

#[no_mangle]
pub extern "C" fn bind_group_layout_descriptor_test(
    desc: &wgc::binding_model::BindGroupLayoutDescriptor,
) {
    let label = unsafe { CStr::from_ptr(desc.label).to_str().unwrap() };

    assert_ffi!(label, "foobar9876");
}

#[no_mangle]
pub extern "C" fn bind_group_entry_test_binding(desc: &wgc::binding_model::BindGroupEntry) {
    assert_ffi!(desc.binding, 654321);
}

#[no_mangle]
pub extern "C" fn bind_group_entry_resource_to_string(
    desc: &wgc::binding_model::BindGroupEntry,
) -> *const c_char {
    CString::new(format!("{:?}", desc.resource))
        .unwrap()
        .into_raw()
}

#[no_mangle]
pub extern "C" fn wgpu_origin_3d_test(origin: &wgt::Origin3d) {
    assert_ffi!(123, origin.x);
    assert_ffi!(456, origin.y);
    assert_ffi!(789, origin.z);
}

#[no_mangle]
pub extern "C" fn wgpu_extent_3d_test(origin: &wgt::Extent3d) {
    assert_ffi!(147, origin.width);
    assert_ffi!(258, origin.height);
    assert_ffi!(369, origin.depth);
}

#[no_mangle]
pub extern "C" fn wgpu_climits_test(limits: &wgn::CLimits) {
    //Disabled -> limits.max_bind_groups is private!
    //assert_ffi!("951", format!("");
}

#[no_mangle]
pub extern "C" fn wgpu_blend_descriptor_test(blend: &wgt::BlendDescriptor) {
    use wgt::{BlendFactor, BlendOperation};

    assert_ffi!(BlendFactor::Zero, blend.src_factor);
    assert_ffi!(BlendFactor::SrcColor, blend.dst_factor);
    assert_ffi!(BlendOperation::Subtract, blend.operation);
}

#[no_mangle]
pub unsafe extern "C" fn wgpu_buffer_descriptor_test(
    buffer: &wgt::BufferDescriptor<wgc::device::Label>,
) {
    let label = CStr::from_ptr(buffer.label);

    assert_ffi!("ZebraBuffer", label.to_str().unwrap());
    assert_ffi!(98, buffer.size);
    assert_ffi!(wgt::BufferUsage::UNIFORM, buffer.usage);
    assert_ffi!(true, buffer.mapped_at_creation);
}

#[no_mangle]
pub extern "C" fn wgpu_buffer_copy_view_test(copy_view: &wgc::command::BufferCopyView) {
    assert_ffi!("(123, 0, Empty)", format!("{:?}", copy_view.buffer));
    assert_ffi!(55, copy_view.layout.offset);
    assert_ffi!(66, copy_view.layout.bytes_per_row);
    assert_ffi!(77, copy_view.layout.rows_per_image);
}

#[no_mangle]
pub unsafe extern "C" fn wgpu_bind_group_descriptor_test(
    bind_group: &wgc::binding_model::BindGroupDescriptor,
) {
    let label = CStr::from_ptr(bind_group.label);

    assert_ffi!("RhinoBindGroup", label.to_str().unwrap());
    assert_ffi!("(31, 0, Empty)", format!("{:?}", bind_group.layout));
    assert_ffi!(32, bind_group.entries as usize);
    assert_ffi!(33, bind_group.entries_length);
}

#[no_mangle]
pub extern "C" fn wgpu_color_state_descriptor_test(color_state: &wgt::ColorStateDescriptor) {
    assert_ffi!(wgt::TextureFormat::Rgba16Uint, color_state.format);
    assert_ffi!(
        wgt::BlendFactor::SrcAlphaSaturated,
        color_state.color_blend.src_factor
    );
    assert_ffi!(
        wgt::BlendFactor::OneMinusSrcColor,
        color_state.alpha_blend.dst_factor
    );
    assert_ffi!(wgt::ColorWrite::GREEN, color_state.write_mask);
}

#[no_mangle]
pub extern "C" fn wgpu_command_buffer_descriptor_test(desc: &wgt::CommandBufferDescriptor) {
    assert_ffi!(123, desc.todo);
}

#[no_mangle]
pub unsafe extern "C" fn wgpu_command_encoder_descriptor_test(
    desc: &wgt::CommandEncoderDescriptor,
) {
    let label = CStr::from_ptr(desc.label);

    assert_ffi!("LionCommandEncoder", label.to_str().unwrap());
}

#[no_mangle]
pub unsafe extern "C" fn wgpu_compute_pipeline_descriptor_test(
    desc: &wgc::pipeline::ComputePipelineDescriptor,
) {
    let entry_point = CStr::from_ptr(desc.compute_stage.entry_point);

    assert_ffi!("(104, 0, Empty)", format!("{:?}", desc.layout));
    assert_ffi!("(44, 0, Empty)", format!("{:?}", desc.compute_stage.module));
    assert_ffi!("WhaleEntryPoint", entry_point.to_str().unwrap());
}

#[no_mangle]
pub extern "C" fn wgpu_depth_stencil_state_descriptor(desc: &wgt::DepthStencilStateDescriptor) {
    assert_ffi!(wgt::TextureFormat::Rg8Snorm, desc.format);
    assert_ffi!(true, desc.depth_write_enabled);
    assert_ffi!(wgt::CompareFunction::Always, desc.depth_compare);
    assert_ffi!(wgt::StencilOperation::Invert, desc.stencil_front.fail_op);
    assert_ffi!(wgt::StencilOperation::Replace, desc.stencil_back.pass_op);
    assert_ffi!(654, desc.stencil_read_mask);
    assert_ffi!(749, desc.stencil_write_mask);
}

#[no_mangle]
pub extern "C" fn wgpu_pipeline_layout_descriptor(
    desc: &wgc::binding_model::PipelineLayoutDescriptor,
) {
    assert_ffi!(764, desc.bind_group_layouts as usize);
    assert_ffi!(44, desc.bind_group_layouts_length);
}

#[no_mangle]
pub extern "C" fn wgpu_compute_pass_descriptor(desc: &wgc::command::ComputePassDescriptor) {
    assert_ffi!(554, desc.todo);
}

#[no_mangle]
pub extern "C" fn wgpu_rasterization_state_descriptor(desc: &wgt::RasterizationStateDescriptor) {
    assert_ffi!(wgt::FrontFace::Cw, desc.front_face);
    assert_ffi!(wgt::CullMode::Front, desc.cull_mode);
    assert_ffi!(133, desc.depth_bias);
    assert_ffi!(1.25, desc.depth_bias_slope_scale);
    assert_ffi!(0.75, desc.depth_bias_clamp);
}

#[no_mangle]
pub unsafe extern "C" fn get_raw_pass_test(
    id: wgc::id::CommandEncoderId,
) -> *const wgc::command::RawPass {
    let pass = wgc::command::RawPass::new_compute(id);

    return Box::into_raw(Box::new(pass));
}

#[no_mangle]
pub extern "C" fn wgpu_render_pass_descriptor_test(desc: &wgc::command::RenderPassDescriptor) {
    assert_ffi!(123, desc.color_attachments as usize);
    assert_ffi!(456, desc.color_attachments_length);
    assert_ffi!(true, desc.depth_stencil_attachment.is_none())
}

#[no_mangle]
pub extern "C" fn wgpu_shader_module_test(module: &wgc::pipeline::ShaderModuleDescriptor) {
    assert_ffi!(667, module.code.bytes as usize);
    assert_ffi!(74, module.code.length)
}

#[no_mangle]
pub extern "C" fn wgpu_request_adapter_options_test(
    options: &wgc::instance::RequestAdapterOptions,
) {
    assert_ffi!(wgt::PowerPreference::LowPower, options.power_preference);
    assert_ffi!(
        "(81, 0, Empty)",
        format!("{:?}", options.compatible_surface.unwrap())
    )
}

#[no_mangle]
pub extern "C" fn wgpu_render_pipeline_descriptor_test(
    options: &wgc::pipeline::RenderPipelineDescriptor,
) {
    assert_ffi!("(10, 0, Empty)", format!("{:?}", options.layout));
    assert_ffi!(
        "(11, 0, Empty)",
        format!("{:?}", options.vertex_stage.module)
    );
    assert_ffi!(12, options.fragment_stage as usize);
    assert_ffi!(
        wgt::PrimitiveTopology::PointList,
        options.primitive_topology
    );
    assert_ffi!(13, options.rasterization_state as usize);
    assert_ffi!(14, options.color_states as usize);
    assert_ffi!(15, options.color_states_length);
    assert_ffi!(16, options.depth_stencil_state as usize);
    assert_ffi!(17, options.vertex_state.vertex_buffers_length);
    assert_ffi!(18, options.sample_count);
    assert_ffi!(19, options.sample_mask);
    assert_ffi!(true, options.alpha_to_coverage_enabled);
}

#[no_mangle]
pub extern "C" fn wgpu_swap_chain_output_test(output: &wgc::swap_chain::SwapChainOutput) {
    assert_ffi!("OutOfMemory", format!("{:?}", output.status));
    assert_ffi!("(31, 0, Empty)", format!("{:?}", output.view_id.unwrap()));
}

#[no_mangle]
pub extern "C" fn wgpu_swap_chain_descriptor_test(desc: &wgt::SwapChainDescriptor) {
    assert_ffi!(wgt::TextureUsage::STORAGE, desc.usage);
    assert_ffi!(wgt::TextureFormat::Depth32Float, desc.format);
    assert_ffi!(43, desc.width);
    assert_ffi!(34, desc.height);
    assert_ffi!(wgt::PresentMode::Mailbox, desc.present_mode);
}

#[no_mangle]
pub unsafe extern "C" fn wgpu_texture_view_descriptor_test(
    desc: &wgt::TextureViewDescriptor<wgc::device::Label>,
) {
    let label = CStr::from_ptr(desc.label).to_str().unwrap();

    assert_ffi!("CheetahTextureView", label);
    assert_ffi!(wgt::TextureFormat::R8Unorm, desc.format);
    assert_ffi!(wgt::TextureViewDimension::CubeArray, desc.dimension);
    assert_ffi!(wgt::TextureAspect::StencilOnly, desc.aspect);
    assert_ffi!(124, desc.base_mip_level);
    assert_ffi!(125, desc.level_count);
    assert_ffi!(126, desc.base_array_layer);
    assert_ffi!(127, desc.array_layer_count);
}

#[no_mangle]
pub unsafe extern "C" fn wgpu_texture_descriptor_test(
    desc: &wgt::TextureDescriptor<wgc::device::Label>,
) {
    let label = CStr::from_ptr(desc.label).to_str().unwrap();
    let expected_size = wgt::Extent3d {
        width: 123,
        height: 456,
        depth: 789,
    };

    assert_ffi!("PenguinTexture", label);
    assert_ffi!(expected_size, desc.size);
    assert_ffi!(150, desc.mip_level_count);
    assert_ffi!(151, desc.sample_count);
    assert_ffi!(wgt::TextureDimension::D3, desc.dimension);
    assert_ffi!(wgt::TextureFormat::Rg11b10Float, desc.format);
    assert_ffi!(wgt::TextureUsage::SAMPLED, desc.usage);
}

#[no_mangle]
pub extern "C" fn wgpu_texture_copy_view_test(desc: &wgc::command::TextureCopyView) {
    let expected_origin = wgt::Origin3d {
        x: 741,
        y: 852,
        z: 963,
    };

    assert_ffi!("(200, 0, Empty)", format!("{:?}", desc.texture));
    assert_ffi!(201, desc.mip_level);
    assert_ffi!(expected_origin, desc.origin);
}

#[no_mangle]
pub unsafe extern "C" fn wgpu_sampler_descriptor_test(
    sampler: &wgn::SamplerDescriptor){
    let label = CStr::from_ptr(sampler.label).to_str().unwrap();

    assert_ffi!(true, sampler.next_in_chain.is_some());
    assert_ffi!("OtterSampler", label);
    assert_ffi!(wgt::AddressMode::Repeat, sampler.address_mode_u);
    assert_ffi!(wgt::AddressMode::ClampToEdge, sampler.address_mode_v);
    assert_ffi!(wgt::AddressMode::MirrorRepeat, sampler.address_mode_w);
    assert_ffi!(wgt::FilterMode::Nearest, sampler.mag_filter);
    assert_ffi!(wgt::FilterMode::Nearest, sampler.min_filter);
    assert_ffi!(wgt::FilterMode::Linear, sampler.mipmap_filter);
    assert_ffi!(23.0, sampler.lod_min_clamp);
    assert_ffi!(24.0, sampler.lod_max_clamp);
    assert_ffi!(wgt::CompareFunction::LessEqual, sampler.compare);
}

#[no_mangle]
pub extern "C" fn wgpu_render_pass_depth_stencil_descriptor(
    pass: &wgt::RenderPassDepthStencilAttachmentDescriptorBase<wgc::id::TextureViewId>
){
    assert_ffi!("(123, 0, Empty)", format!("{:?}", pass.attachment));
    assert_ffi!(wgt::LoadOp::Load, pass.depth_load_op);
    assert_ffi!(wgt::StoreOp::Store, pass.depth_store_op);
    assert_ffi!(10.0, pass.clear_depth);
    assert_ffi!(true, pass.depth_read_only);
    assert_ffi!(wgt::LoadOp::Load, pass.stencil_load_op);
    assert_ffi!(wgt::StoreOp::Clear, pass.stencil_store_op);
    assert_ffi!(11, pass.clear_stencil);
    assert_ffi!(true, pass.stencil_read_only);
}
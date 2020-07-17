/// Used to generate functions to test enums between
/// wgpu-java/native and the corresponding rust wgpu type.
///
/// ```
/// enum_name_test!(method_name, type_path)
/// ```
/// For instance the line `enum_name_test!(get_power_preference_name, wgt::PowerPreference)`
/// produces the following function:
/// ```
///#[no_mangle]
///pub extern fn get_power_preference_name(x : wgt::PowerPreference) -> *const std::os::raw::c_char{
///    std::ffi::CString::new(format!("{:?}", x)).unwrap().into_raw()
///}
/// ```
macro_rules! enum_name_test {
    ($func_name:ident, $enum_type:ty) => {
        enum_name_test!($func_name, $enum_type, x, format!("{:?}", x));
    };
    ($func_name:ident, $enum_type:ty, $format_input:ident, $format:expr) => {
        #[no_mangle]
        pub extern "C" fn $func_name($format_input: $enum_type) -> *const std::os::raw::c_char {
            std::ffi::CString::new($format)
                .unwrap()
                .into_raw()
        }
    };
}

enum_name_test!(get_power_preference_name, wgt::PowerPreference);
enum_name_test!(get_primitive_topology_name, wgt::PrimitiveTopology);
enum_name_test!(get_present_mode_name, wgt::PresentMode);
enum_name_test!(get_store_op_name, wgt::StoreOp);
enum_name_test!(get_load_op_name, wgt::LoadOp);
enum_name_test!(get_blend_factor_name, wgt::BlendFactor);
enum_name_test!(get_blend_operation_name, wgt::BlendOperation);
enum_name_test!(get_cull_mode_name, wgt::CullMode);
enum_name_test!(get_index_format_name, wgt::IndexFormat);
enum_name_test!(get_front_face_name, wgt::FrontFace);
enum_name_test!(get_log_level_name, wgn::LogLevel);
enum_name_test!(get_swap_chain_status_name, wgt::SwapChainStatus);
enum_name_test!(get_texture_format_name, wgt::TextureFormat);
enum_name_test!(get_wgpu_binding_type_name, wgc::binding_model::BindingType);
enum_name_test!(
    get_wgpu_buffer_map_async_status_name,
    wgc::resource::BufferMapAsyncStatus
);
enum_name_test!(
    get_wgpu_texture_view_dimension_name,
    wgt::TextureViewDimension
);
enum_name_test!(
    get_wgpu_texture_component_type_name,
    wgt::TextureComponentType
);
enum_name_test!(get_wgpu_input_step_mode_name, wgt::InputStepMode);
enum_name_test!(get_wgpu_vertex_format_name, wgt::VertexFormat);
enum_name_test!(get_wgpu_texture_dimension_name, wgt::TextureDimension);
enum_name_test!(get_wgpu_texture_aspect_name, wgt::TextureAspect);
enum_name_test!(get_wgpu_compare_function_name, wgt::CompareFunction);
enum_name_test!(get_wgpu_stencil_operation_name, wgt::StencilOperation);
enum_name_test!(get_address_mode_name, wgt::AddressMode);
enum_name_test!(get_filter_mode_name, wgt::FilterMode);
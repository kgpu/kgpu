# Wgpuj
Java bindings for [Wgpu](https://github.com/gfx-rs/wgpu) based on
[Wgpu-native](https://github.com/gfx-rs/wgpu-native). 

This module is written in Java and has the FFI code for kgpu. It uses the [jnr-ffi](https://github.com/jnr/jnr-ffi) library,
which creates JNI bindings at runtime. Most of the code in this module is generated by jnr-gen

#### Requirements:
- Rust 
- Java JDK 11+

#### Old Version:
There is an archived version of this library at
 [kgpu/wgpu-java](https://github.com/kgpu/wgpu-java)
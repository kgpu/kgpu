# Modules

## kgpu

The core of kgpu. It provides the following APIs:

- WebGPU Bindings
  
  - JVM/Desktop: Native Bindings to wgpu
  - JS/Web: Javascript Bindings
  
- Window API

  - JVM/Desktop: GLFW Bindings via LWJGL
  - JS/Web: Canvas API via Web Browser

- Image Loading API:

  - JVM/Desktop: AWT Image Loading
  - JS/Web: Off-screen Canvas Image Loading

## kcgmath

Kotlin Computer Graphics Math Library

A cross platform graphics library for Kotlin based
on the Rust crate [cgmath](https://crates.io/crates/cgmath)

__Note:__ This library is meant to be used as a basic computer graphics library for WebGPU. If you are using OpenGL or want something more complex,
you may want to consider one of the following:

- [JOML](https://joml-ci.github.io/JOML/): JVM Only, More Complex, Faster
- [Korma](https://github.com/korlibs/korma/blob/master/korma/src/commonMain/kotlin/com/soywiz/korma/geom/Matrix3D.kt): Built for OpenGL

## kshader

A library to help compile GLSL to SPIR-V

On the JVM/Desktop it uses [Shaderc](https://github.com/LWJGL/lwjgl3/tree/master/modules/lwjgl/shaderc) via LWJGL.

For JS/Web it uses the [glslang](https://www.npmjs.com/package/@webgpu/glslang) library.

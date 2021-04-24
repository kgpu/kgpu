# Kgpu

A Cross Platform Graphics API For Kotlin JVM/JS based on WebGPU and WebGPU Native

__Warning__: Because WebGPU is under active development, kgpu is very unstable! Once the specification is more 
finalized, this will not be an issue. 

__Rewrite__: Currently the project's native backend is being 
rewritten to support the JVM's new FFI API ([Project Panama](https://openjdk.java.net/projects/panama/)). 
To access the old version, see the [jnr branch](https://github.com/kgpu/kgpu/tree/jnr)

 __Requirements:__

- JDK 17+

 __Supported Platforms:__

- Windows 10
- MacOS
- Linux
- Chrome Canary
- Firefox Nightly

## Links

[__Kgpu Book__](https://kgpu.github.io/kgpu)

[__Documentation__](https://kgpu.github.io/kgpu/dokka/-modules.html)

[__Live Example__](https://kgpu.github.io/kgpu/examples/index.html)

## Modules

kgpu is split into multiple modules:

- __kgpu:__ The core of this library (Kotlin bindings to WebGPU)
- __kcgmath:__  A cross platform graphics library for Kotlin based
on the Rust crate [cgmath](https://crates.io/crates/cgmath)
- __kshader:__ A library to help compile GLSL to SPIR-V

[__More Info__](https://kgpu.github.io/kgpu/modules.html)

## Images

![Earth Example](docs/src/images/earth.png)

## Examples

To run the examples on Desktop:

```bash
gradlew runTriangleExample
gradlew runCubeExample
gradlew runTextureExample
gradlew runEarthExample
```

To run the examples on the Web:

```bash
gradlew buildWeb startWebServer
```

Then navigate to [http://localhost:8080/index.html](http://localhost:8080/index.html)

## Getting Started
To get started, see the [Getting Started Page](https://kgpu.github.io/kgpu/getting_started.html)
in the Kgpu book

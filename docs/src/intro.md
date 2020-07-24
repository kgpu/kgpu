# Introduction
Kgpu is cross platform library that exposes the WebGPU api for Kotlin Javascript, and for Kotlin on the JVM. 
This allows users to write code that can be run on both web browsers and natively on their computers. 

### What is WebGPU?
According to the W3C website, WebGPU is,
> an interface between the Web Platform and modern 3D graphics and computation capabilities present
> in native system platforms. The goal is to design a new Web API that exposes these modern technologies
> in a performant, powerful and safe manner. It should work with existing platform APIs such as Direct3D 12
> from Microsoft, Metal from Apple, and Vulkan from the Khronos Group. This API will also expose the generic 
>computational facilities available in today's GPUs to the Web, and investigate shader languages to produce a
> cross-platform solution.

### What is Wgpu?
Wgpu is Mozilla's Rust implementation of the WebGPU specification. It is similar to Dawn, Google's WebGPU implementation 
for Chromium. 
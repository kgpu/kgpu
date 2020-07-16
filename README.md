# Kgpu
A Cross Platform Graphics API For Kotlin JVM/JS
 
 __Requirements:__
 - JDK 11 
 - Rust Stable (temporary, see [Issue #5](https://github.com/kgpu/kgpu/issues/5))
 
 __Supported Platforms:__
- Windows 10 
- MacOS (See [Issue #1](https://github.com/kgpu/kgpu/issues/1))
- Linux
- Chrome Canary
- Firefox Nightly

### Links

##### [Website (documentation)](https://kgpu.github.io/kgpu)
##### [Live Examples](https://kgpu.github.io/kgpu/examples/index.html)

### Images
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
```
gradlew buildWeb startWebServer
```
Then navigate to [http://localhost:8080/index.html](http://localhost:8080/index.html)
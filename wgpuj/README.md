# Wgpuj
The bindings have been moved to kgpu-jvm module. This module simply contains the natives

### Requirements:
- Rust 
- Java JDK 11+

### Old Version:
There is an archived version of this library at
 [kgpu/wgpu-java](https://github.com/kgpu/wgpu-java)

### How to add to Gradle (Kotlin DSL)
First you need to add the snapshots repository:
```kotlin
repositories {
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
}
```

Then you can add the dependency:
```kotlin
dependencies {
    implementation("io.github.kgpu:wgpuj:0.1.0-SNAPSHOT")
}
```
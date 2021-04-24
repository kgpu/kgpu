include("wgpuj-natives")
include("examples")
include("modules:kcgmath")
include("modules:kgpu")

project(":wgpuj-natives").projectDir = file("wgpuj")

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}

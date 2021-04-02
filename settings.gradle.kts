include("wgpuj-natives")
include("examples")
include("modules:kshader")
include("modules:kcgmath")
include("modules:kgpu")

project(":wgpuj-natives").projectDir = file("wgpuj")

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}

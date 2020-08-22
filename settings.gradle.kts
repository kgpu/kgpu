rootProject.name = "kgpu"
include("wgpuj")
include("wgpuj:jnrgen")
include("examples")
include("modules:kshader")
include("modules:kcgmath")

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}
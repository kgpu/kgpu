import org.gradle.internal.os.OperatingSystem
import org.jetbrains.dokka.gradle.DokkaTask
import org.gradle.plugins.javascript.envjs.http.simple.SimpleHttpFileServerFactory

plugins {
    kotlin("multiplatform") version "1.3.72"
    id("org.jetbrains.dokka") version "0.10.1"
}

repositories {
    mavenCentral()
    jcenter()
}

group "io.github.kgpu"
version "0.1.0"

val natives = when(OperatingSystem.current()){
    OperatingSystem.LINUX -> "natives-linux"
    OperatingSystem.MAC_OS -> "natives-macos"
    OperatingSystem.WINDOWS -> "natives-windows"
    else -> throw RuntimeException("Unsupported operating system.")
}

kotlin {
    jvm()
    js().browser()

    sourceSets{
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.7")
            }
        }

        jvm().compilations["main"].defaultSourceSet{
            dependencies{
                implementation(kotlin("stdlib-jdk8"))
                api(project(":native"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
                api("org.joml:joml:1.9.25")

                val lwjglVersion: String by extra
                implementation("org.lwjgl:lwjgl:$lwjglVersion")
                implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
                implementation("org.lwjgl:lwjgl-shaderc:$lwjglVersion")
                runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:$natives")
                runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:$natives")
                runtimeOnly("org.lwjgl:lwjgl-shaderc:$lwjglVersion:$natives")
            }
        }

        js().compilations["main"].defaultSourceSet{
            dependencies{
                implementation(kotlin("stdlib-js"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.7")
                api(npm("gl-matrix", "3.3.0"))
            }
        }
    }
}

tasks {
    val dokka by getting(DokkaTask::class) {
        outputDirectory = "$rootDir/docs/book"
        outputFormat = "html"

        multiplatform {
            val global by creating {
                includes = listOf("docs/packages.md")
            }

            val jvm by creating {
                subProjects = listOf("native")
            }

            val js by creating{

            }
        }
    }

    register("startDocServer"){
        val port = 8000
        val path = "$rootDir/docs/book"

        doLast {
            val server = SimpleHttpFileServerFactory().start(File(path), port)

            println("Server started in directory " + server.getContentRoot())
            println("Link: http://localhost:" + server.getPort() + "/index.html\n\n")
        }
    }

    register("copyExamplesToBook", Copy::class){
        dependsOn("examples:buildWebExample")
        from("$rootDir/examples/build/distributions")
        into("$rootDir/docs/book/examples")
    }

    register("generateBook", Exec::class){
        workingDir("${rootDir}/docs")
        commandLine("mdbook", "build")
    }
}
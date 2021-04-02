import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    id("maven-publish")
}

repositories {
    mavenCentral()
    jcenter()
}

group = rootProject.extra["projectGroup"]!!
version = rootProject.extra["projectVersion"]!!

kotlin {
    jvm().withJava()
    jvm().compilations.all {
        kotlinOptions {
            jvmTarget = "11"
        }

        compileJavaTaskProvider?.get()?.options?.compilerArgs?.add("--add-modules")
        compileJavaTaskProvider?.get()?.options?.compilerArgs?.add("jdk.incubator.foreign")
    }
    js().browser()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.7")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-annotations-common"))
            }
        }

        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                // Needed or else build fails
                implementation(kotlin("stdlib-jdk8"))
                api(project(":wgpuj-natives"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")

                val lwjglVersion = rootProject.extra["lwjglVersion"]
                implementation("org.lwjgl:lwjgl:$lwjglVersion")
                implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")

                runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:natives-windows")
                runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:natives-macos")
                runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:natives-linux")

                runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-windows")
                runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-macos")
                runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-linux")
            }
        }

        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(project(":modules:kshader"))
                implementation(kotlin("test-junit"))
            }
        }

        js().compilations["main"].defaultSourceSet {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.7")
            }
        }

        js().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

tasks.withType<DokkaTask>().configureEach {
    outputDirectory = "$rootDir/docs/book/dokka/kgpu"

    dokkaSourceSets {
        configureEach {
            includeNonPublic = false
        }

        register("commonMain") {
            displayName = "Common"
            platform = "common"
        }

        register("jvmMain") {
            displayName = "Desktop"
            platform = "jvm"
        }

        register("jsMain") {
            displayName = "Web"
            platform = "js"
        }
    }
}

publishing {
    repositories {
        maven {
            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)

            credentials {
                username = "DevOrc"
                password = System.getenv("sonatypePassword") ?: ""
            }
        }
    }
}

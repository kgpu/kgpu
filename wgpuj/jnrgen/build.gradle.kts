plugins {
    id("java")
}

group = rootProject.extra["projectGroup"]
version = rootProject.extra["projectVersion"]

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

tasks {
    register("deletePreviousBindings", Delete::class) {
        delete("$buildDir/jnr-gen")
    }

    register("generateBindings", JavaExec::class) {
        dependsOn("deletePreviousBindings")
        dependsOn("classes")
        classpath = sourceSets["main"].runtimeClasspath
        main = "io.github.kgpu.wgpuj.jnrgen.JNRGenerator"
        args = listOf("$buildDir")
    }
}

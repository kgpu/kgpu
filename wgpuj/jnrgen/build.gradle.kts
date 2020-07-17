plugins {
    id("java")
}

group "com.noahcharlton.wgpuj"
version "0.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

tasks{
    register("deletePreviousBindings", Delete::class){
        delete("${buildDir}/jnr-gen")
    }
    
    register("generateBindings", JavaExec::class){
        dependsOn("deletePreviousBindings")
        dependsOn("classes")
        classpath = sourceSets["main"].runtimeClasspath
        main = "com.noahcharlton.wgpuj.jnrgen.JNRGenerator"
        args = listOf("${buildDir}")
    }
}
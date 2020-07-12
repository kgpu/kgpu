import com.diffplug.gradle.spotless.SpotlessExtension;
import org.gradle.api.tasks.testing.logging.TestExceptionFormat;

plugins {
    id("java-library")
    id("kotlin")
    id("com.diffplug.gradle.spotless") version "4.4.0"
}

group "io.github.kgpu"
version "0.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.github.jnr:jnr-ffi:2.1.15")
    api("com.github.jnr:jffi:1.2.23")

    testImplementation(project(":native:jnrgen"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("skipped", "failed")

        info{
            events("skipped", "failed", "passed")
        }

        exceptionFormat = TestExceptionFormat.FULL
        showCauses = true
        showStackTraces = true
    }

    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun beforeTest(testDescriptor: TestDescriptor) {}
        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
            //Fail on skipped tests because sometimes it will skip the test if the JVM crashes in
            //native code
            when(result.resultType){
                TestResult.ResultType.SKIPPED -> throw RuntimeException("Tests cannot be skipped!")
            }
        }
        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            if (suite.parent == null) { // root suite
                println("\n\nTests Ran: ${result.testCount}")
                println("Passes: ${result.successfulTestCount}")
                println("Failures: ${result.failedTestCount}")
                println("Skipped: ${result.skippedTestCount}")
                println("Result: ${result.resultType}\n\n")
            }
        }
    })
}

configure<SpotlessExtension> {
    java {
        removeUnusedImports()
        indentWithSpaces()
    }
}

tasks{
    register("updateBindings", Copy::class){
        dependsOn("jnrgen:generateBindings")
        from("${projectDir}/jnrgen/build/jnr-gen")
        into("${projectDir}/src/main/java/com/noahcharlton/wgpuj/jni")

        finalizedBy("spotlessApply")
    }

    register("installNativeLibraries", Copy::class){
        dependsOn("compileWgpuNative")

        from("${projectDir}/wgpu-native/target/debug"){
            include("wgpu_native.dll")
            include("libwgpu_native.so")
            include("libwgpu_native.dylib")
        }
        into("${projectDir}/src/main/resources")
    }

    register("compileWgpuNative", Exec::class){
        workingDir("${projectDir}/wgpu-native")
        commandLine("cargo", "build")
    }

    register("installTestNativeLibraries", Copy::class){
        dependsOn("compileWgpuTest")

        from("${projectDir}/wgpu-test/target/debug"){
            include("wgpu_test.dll")
            include("libwgpu_test.so")
            include("libwgpu_test.dylib")
        }
        into("${projectDir}/src/test/resources")
    }

    register("compileWgpuTest", Exec::class){
        workingDir("${projectDir}/wgpu-test")
        commandLine("cargo", "build")
    }

    val compileJava by getting {
        dependsOn("installNativeLibraries")
    }

    val test by getting {
        dependsOn("cleanTest")
    }

    val compileTestJava by getting{
        dependsOn("installTestNativeLibraries")
    }
}
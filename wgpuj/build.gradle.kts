import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("java-library")
    id("de.undercouch.download")
    id("maven-publish")
}

group = rootProject.extra["projectGroup"]
version = rootProject.extra["projectVersion"]

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    api("com.github.jnr:jnr-ffi:2.1.15")
    api("com.github.jnr:jffi:1.2.23")

    testImplementation(project("jnrgen"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("skipped", "failed")

        info {
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
            // Fail on skipped tests because sometimes it will skip the test if the JVM crashes in
            // native code
            when (result.resultType) {
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

tasks {
    register("updateBindings", Copy::class) {
        dependsOn("jnrgen:generateBindings")
        from("$projectDir/jnrgen/build/jnr-gen")
        into("$projectDir/src/main/java/io/github/kgpu/wgpuj/jni")

        finalizedBy("spotlessApply")
    }

    register("downloadWgpuNative", Download::class) {
        val wgpuNativeSHA: String by rootProject.extra
        val baseUrl = "https://github.com/kgpu/wgpu-native-builds/releases/download/V.$wgpuNativeSHA/"
        onlyIfModified(true)

        src(listOf(
            "${baseUrl}wgpu-linux-64-release.zip",
            "${baseUrl}wgpu-macos-64-release.zip",
            "${baseUrl}wgpu-windows-64-release.zip"
        ))
        dest("$buildDir")
    }

    register("installWgpuNative", Copy::class) {
        dependsOn("downloadWgpuNative")
        // Only include the library files from the releases
        from(zipTree("$buildDir/wgpu-linux-64-release.zip")) {
            include("**.so")
        }
        from(zipTree("$buildDir/wgpu-macos-64-release.zip")) {
            include("**.dylib")
        }
        from(zipTree("$buildDir/wgpu-windows-64-release.zip")) {
            include("**.dll")
        }

        into("$projectDir/src/main/resources")
    }

    val compileJava by getting {
        dependsOn("installWgpuNative")
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

    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.extra["projectGroup"] as String
            artifactId = "wgpuj"
            version = rootProject.extra["projectVersion"] as String

            from(components["java"])
        }
    }
}

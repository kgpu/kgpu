import org.jetbrains.dokka.gradle.DokkaTask
import org.gradle.plugins.javascript.envjs.http.simple.SimpleHttpFileServerFactory
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("multiplatform") version "1.4.0"
    id("org.jetbrains.dokka") version "0.10.1"
    id("maven-publish")
}

repositories {
    mavenCentral()
    jcenter()
}

group = rootProject.extra["projectGroup"]
version = rootProject.extra["projectVersion"]

kotlin {
    jvm()
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
                //Needed or else build fails
                implementation(kotlin("stdlib-jdk8"))
                api(project(":wgpuj"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
                api("org.joml:joml:1.9.25")

                val lwjglVersion = rootProject.extra["lwjglVersion"]
                implementation("org.lwjgl:lwjgl:$lwjglVersion")
                implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
                implementation("org.lwjgl:lwjgl-shaderc:$lwjglVersion")

                runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:natives-windows")
                runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:natives-macos")
                runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:natives-linux")

                runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-windows")
                runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-macos")
                runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-linux")

                runtimeOnly("org.lwjgl:lwjgl-shaderc:$lwjglVersion:natives-windows")
                runtimeOnly("org.lwjgl:lwjgl-shaderc:$lwjglVersion:natives-macos")
                runtimeOnly("org.lwjgl:lwjgl-shaderc:$lwjglVersion:natives-linux")
            }
        }

        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        js().compilations["main"].defaultSourceSet {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.7")
                api(npm("gl-matrix", "3.3.0"))
            }
        }

        js().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

tasks.withType<AbstractTestTask>().all {
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
        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}

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
    val dokka by getting(DokkaTask::class) {
        outputDirectory = "$rootDir/docs/book"
        outputFormat = "html"

        multiplatform {
            val global by creating {
                includeNonPublic = false
                includes = listOf("docs/packages.md")
            }

            val jvm by creating {
                subProjects = listOf("wgpuj")
            }

            val js by creating {

            }
        }
    }

    register("startDocServer") {
        val port = 8000
        val path = "$rootDir/docs/book"

        doLast {
            val server = SimpleHttpFileServerFactory().start(File(path), port)

            println("Server started in directory " + server.getContentRoot())
            println("Link: http://localhost:" + server.getPort() + "/index.html\n\n")
        }
    }

    register("copyExamplesToBook", Copy::class) {
        dependsOn("examples:buildWebExample")
        from("$rootDir/examples/build/distributions")
        into("$rootDir/docs/book/examples")
    }

    register("generateBook", Exec::class) {
        workingDir("${rootDir}/docs")
        commandLine("mdbook", "build")
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
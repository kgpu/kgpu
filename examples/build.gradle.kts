import org.gradle.plugins.javascript.envjs.http.simple.SimpleHttpFileServerFactory

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
    jcenter()
}

group = rootProject.extra["projectGroup"]!!
version = rootProject.extra["projectVersion"]!!

kotlin {
    jvm() {
        withJava()
        tasks {
            register<Jar>("jvmFatJar") {
                dependsOn("jvmJar")

                manifest {
                    attributes["Main-Class"] = "DesktopExampleKt"
                }
                archiveBaseName.set("${project.name}-fat")
                from(configurations.getByName("runtimeClasspath").map { if (it.isDirectory) it else zipTree(it) },
                    compilations.getByName("main").output.classesDirs,
                    compilations.getByName("main").output.resourcesDir
                )
            }
        }
    }
    js().browser() {
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                val korlibVersion = rootProject.extra["korlibVersion"]

                implementation(project(":modules:kgpu"))
                implementation(project(":modules:kshader"))
                implementation(project(":modules:kcgmath"))
                implementation("com.soywiz.korlibs.korim:korim:$korlibVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
            }
        }
        val jsMain by getting {
            dependencies {
            }
        }
    }
}

fun exampleArgs(arg: String): List<String> {
    var startOnFirstThread: String? = null
    if (System.getProperty("os.name").contains("Mac")) {
        startOnFirstThread = "-XstartOnFirstThread"
    }

    return listOfNotNull(
        "java",
        startOnFirstThread,
        "-Dforeign.restricted=permit",
        "--add-modules",
        "jdk.incubator.foreign",
        "-jar",
        "$buildDir/libs/examples-fat-${project.version}.jar",
        arg
    )
}

tasks {
    register("buildWebExample") {
        dependsOn("jsBrowserDistribution")
        dependsOn("jsBrowserWebpack")
    }

    register("startWebServer") {
        val port = 8080
        val path = "$buildDir/distributions"

        doLast {
            val server = SimpleHttpFileServerFactory().start(File(path), port)

            println("Server started in directory " + server.contentRoot)
            println("Link: http://localhost:" + server.port + "/index.html\n\n")
        }
    }

    register("runTriangleExample", Exec::class) {
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine(exampleArgs("-triangle"))
    }

    register("runCubeExample", Exec::class) {
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine(exampleArgs("-cube"))
    }

    register("runTextureExample", Exec::class) {
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine(exampleArgs("-texture"))
    }

    register("runEarthExample", Exec::class) {
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine(exampleArgs("-earth"))
    }

    register("runComputeExample", Exec::class) {
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine(exampleArgs("-compute"))
    }

    register("runMsaaExample", Exec::class) {
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine(exampleArgs("-msaa"))
    }

    register("runCompareExample", Exec::class) {
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine(exampleArgs("-computeCompare"))
    }

    register("runWindowExample", Exec::class) {
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine(exampleArgs("-window"))
    }

    register("runBoidExample", Exec::class) {
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine(exampleArgs("-boid"))
    }
}

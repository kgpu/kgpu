import org.gradle.plugins.javascript.envjs.http.simple.SimpleHttpFileServerFactory

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
    jcenter()
}

group = rootProject.extra["projectGroup"]
version = rootProject.extra["projectVersion"]

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
        commandLine("java", "-XstartOnFirstThread", "-jar", "$buildDir/libs/examples-fat-${project.version}.jar", "-triangle",  "-version")
    }

    register("runCubeExample", Exec::class) {
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine("java", "-XstartOnFirstThread", "-jar", "$buildDir/libs/examples-fat-${project.version}.jar", "-cube")
    }

    register("runTextureExample", Exec::class) {
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine("java", "-XstartOnFirstThread", "-jar", "$buildDir/libs/examples-fat-${project.version}.jar", "-texture")
    }

    register("runEarthExample", Exec::class) {
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine("java", "-XstartOnFirstThread", "-jar", "$buildDir/libs/examples-fat-${project.version}.jar", "-earth")
    }

    register("runComputeExample", Exec::class) {
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine("java", "-XstartOnFirstThread", "-jar", "$buildDir/libs/examples-fat-${project.version}.jar", "-compute")
    }

    register("runMsaaExample", Exec::class) {
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine("java", "-XstartOnFirstThread", "-jar", "$buildDir/libs/examples-fat-${project.version}.jar", "-msaa")
    }

    register("runCompareExample", Exec::class) {
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine("java", "-jar", "$buildDir/libs/examples-fat-${project.version}.jar", "-computeCompare")
    }

    register("runWindowExample", Exec::class) {
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine("java", "-jar", "$buildDir/libs/examples-fat-${project.version}.jar", "-window")
    }

    register("runBoidExample", Exec::class) {
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine("java", "-XstartOnFirstThread", "-jar", "$buildDir/libs/examples-fat-${project.version}.jar", "-boid")
    }
}

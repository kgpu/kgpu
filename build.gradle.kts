import org.gradle.plugins.javascript.envjs.http.simple.SimpleHttpFileServerFactory

plugins {
    kotlin("multiplatform") version "1.4.0"
    id("org.jetbrains.dokka") version "1.4.0-rc"
    id("maven-publish")
    id("com.diffplug.spotless") version "5.8.2"
}

repositories {
    mavenCentral()
    jcenter()
}

kotlin {
    jvm() // Needed to satisfy multiplatform plugin
}

group = rootProject.extra["projectGroup"]!!
version = rootProject.extra["projectVersion"]!!

tasks.dokkaHtmlMultimodule {
    outputDirectory = "$rootDir/docs/book/dokka"
    documentationFileName = "README.md"
}

//configure<com.diffplug.gradle.spotless.SpotlessExtension> {
//    kotlin {
//        val files = project.fileTree(rootDir)
//        files.include("**/*.kt")
//
//        toggleOffOn()
//        target(files)
//        ktfmt("0.18").dropboxStyle()
//    }
// Temporarily disable until Google format is fixed on Java16
//    java {
//        val files = project.fileTree(rootDir)
//        files.include("**/*.java")
//
//        target(files)
//        googleJavaFormat("1.10.0")
//        removeUnusedImports()
//        indentWithSpaces()
//    }
//    kotlinGradle {
//        val files = project.fileTree(rootDir)
//        files.include("**/*.gradle.kts")
//
//        target(files)
//        ktlint()
//    }
//}

tasks {
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
        workingDir("$rootDir/docs")
        commandLine("mdbook", "build")
    }
}

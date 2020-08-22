import org.jetbrains.dokka.gradle.DokkaTask
import org.gradle.plugins.javascript.envjs.http.simple.SimpleHttpFileServerFactory
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("multiplatform") version "1.4.0"
    id("org.jetbrains.dokka") version "1.4.0-rc"
    id("maven-publish")
}

repositories {
    mavenCentral()
    jcenter()
}

kotlin {
    jvm() //Needed to satisfy multiplatform plugin
}

group = rootProject.extra["projectGroup"]
version = rootProject.extra["projectVersion"]

tasks.dokkaHtmlMultimodule {
    outputDirectory = "$rootDir/docs/book/dokka"
    documentationFileName = "README.md"
}

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
        workingDir("${rootDir}/docs")
        commandLine("mdbook", "build")
    }
}
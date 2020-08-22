import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.dokka.gradle.DokkaTask

plugins{
    kotlin("multiplatform")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

repositories {
    mavenCentral()
    jcenter()
}

group = rootProject.extra["projectGroup"]
version = rootProject.extra["projectVersion"]

kotlin {
    jvm()
    js(){
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting {
            dependencies{
            }
        }
        val commonTest by getting {
            dependencies{
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting{
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

tasks.withType<DokkaTask>().configureEach {
    outputDirectory = "$rootDir/docs/book/dokka"

    dokkaSourceSets {
        configureEach {
            includeNonPublic = false 
        }

        register("commonMain"){
            displayName = "Common"
            platform = "common"
        }

        register("jvmMain"){
            displayName = "Desktop"
            platform = "jvm"
        }

        register("jsMain"){
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

tasks.withType<AbstractTestTask>().all {
    testLogging {
        events("skipped", "failed")

        info {
            events("skipped", "failed", "passed")
        }

        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
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

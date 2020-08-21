import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins{
    kotlin("multiplatform")
    id("maven-publish")
}

repositories {
    mavenCentral()
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

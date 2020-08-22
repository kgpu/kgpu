import org.gradle.api.tasks.testing.logging.TestExceptionFormat

subprojects {
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
}
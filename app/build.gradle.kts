@file:Suppress("UnstableApiUsage")

import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.sonarqube)
    jacoco
}

android {
    namespace = "com.androidApp.cricketscorekeeper"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.androidApp.cricketscorekeeper"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }

    testOptions {
        managedDevices {
            localDevices {
                create("pixel6Api33") {
                    device = "Pixel 6"
                    apiLevel = 33
                    systemImageSource = "google"
                }
                create("pixelTabletApi33") {
                    device = "Pixel Tablet"
                    apiLevel = 33
                    systemImageSource = "google"
                }
            }
        }
    }
}

sonar {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectKey", "ntnkeshri")
        property("sonar.organization", "ntnkeshri")
        property("sonar.projectName", "CricketScorekeeper")
        property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Generates JaCoCo code coverage report for debug unit tests."
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*"
    )

    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(layout.buildDirectory.get()) {
        include(
            "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
            "jacoco/testDebugUnitTest.exec"
        )
    })
}

dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.intents)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks.register("generateRtm") {
    group = "documentation"
    description = "Scans source code and test files for @Requirement annotations and generates RTM.md in project root."

    val rootDir = layout.projectDirectory.asFile.parentFile ?: layout.projectDirectory.asFile
    val srcDir = layout.projectDirectory.dir("src").asFile
    val outputFile = File(rootDir, "RTM.md")

    inputs.dir(srcDir)
    outputs.file(outputFile)

    doLast {
        val requirementRegex = Regex(
            """@Requirement\s*\(\s*id\s*=\s*"([^"]+)"\s*,\s*description\s*=\s*"([^"]+)"\s*\)\s*(?:@[^\n]+\n\s*)*(?:private\s+|protected\s+|internal\s+)?(?:fun|class)\s+([a-zA-Z0-9_]+)""",
            RegexOption.DOT_MATCHES_ALL
        )

        class RtmEntry(
            val id: String,
            val description: String,
            val filePath: String,
            val targetName: String
        )

        val entries = mutableListOf<RtmEntry>()

        srcDir.walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { file ->
            val relativePath = file.relativeTo(rootDir).path.replace('\\', '/')
            val text = file.readText()

            requirementRegex.findAll(text).forEach { match ->
                val id = match.groupValues[1]
                val description = match.groupValues[2]
                val targetName = match.groupValues[3]
                entries.add(RtmEntry(id, description, relativePath, targetName))
            }
        }

        entries.sortBy { it.id }

        val markdown = buildString {
            appendLine("# Requirement Traceability Matrix (RTM)")
            appendLine()
            appendLine("Automated requirement traceability matrix mapping system requirements to source code and test implementations.")
            appendLine()
            appendLine("| Requirement ID | Description | File | Target Symbol |")
            appendLine("|---|---|---|---|")
            entries.forEach { entry ->
                appendLine("| `${entry.id}` | ${entry.description} | [`${entry.filePath.substringAfterLast("/")}`](${entry.filePath}) | `${entry.targetName}` |")
            }
            appendLine()
            appendLine("> Generated automatically via `./gradlew generateRtm`")
        }

        outputFile.writeText(markdown)
        println("SUCCESS: Requirement Traceability Matrix generated at ${outputFile.absolutePath}")
    }
}

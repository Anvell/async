@file:Suppress("DSL_SCOPE_VIOLATION")

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.dependency.updates)
}

allprojects {
    apply {
        plugin("org.jmailen.kotlinter")
    }

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }

    group = "io.github.anvell"
    version = "2.0.0"
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        val version = candidate.version.toLowerCaseAsciiOnly()

        listOf("-alpha", "-beta", "-rc")
            .any { it in version }
    }
}

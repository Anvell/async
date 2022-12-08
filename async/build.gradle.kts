@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-library")
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
        )
    }
}

dependencies {
    testImplementation(libs.kotlin.test)
}

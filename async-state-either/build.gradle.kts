@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    id("maven-publish")
}

kotlin {
    explicitApi()

    jvm()
    js(IR) {
        browser()
    }
    iosArm32()
    iosArm64()
    iosX64()
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()
    tvosArm64()
    tvosX64()
    watchosArm32()
    watchosArm64()
    watchosX86()
    watchosX64()

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(project(":async"))
                api(project(":async-state"))

                implementation(libs.coroutines.core)
                implementation(libs.either)
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.coroutines.test)
            }
        }
    }
}

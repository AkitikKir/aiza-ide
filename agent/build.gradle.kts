plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.9.22"
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(project(":config"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
            }
        }
    }
}

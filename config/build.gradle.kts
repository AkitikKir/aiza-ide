plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.github.cdimascio:dotenv-java:3.0.0")
            }
        }
    }
}

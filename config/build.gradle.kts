plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
            }
        }
    }
}

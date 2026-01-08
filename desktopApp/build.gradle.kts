plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":ui"))
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.aiza.MainKt"
        nativeDistributions {
            targetFormats(
                if (System.getProperty("os.name").contains("Mac", ignoreCase = true)) {
                    org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg
                } else if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) {
                    org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                    org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe
                } else {
                    org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
                }
            )
            packageName = "AizaIDE"
            packageVersion = "1.0.0"
        }
    }
}

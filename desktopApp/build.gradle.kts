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
            val os = System.getProperty("os.name").lowercase()
            when {
                os.contains("mac") -> targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg)
                os.contains("windows") -> targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi)
                else -> targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            }
            packageName = "AizaIDE"
            packageVersion = "1.0.0"
        }
    }
}

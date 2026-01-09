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
                implementation(project(":agent"))
                implementation(project(":config"))
                implementation(project(":core"))
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
            linux {
                packageDeps = "libasound2, libpng16-16, libx11-6, libxext6, libxrender1, libxtst6, libfreetype6"
            }
        }
    }
}

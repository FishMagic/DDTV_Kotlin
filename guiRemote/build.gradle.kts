plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization") version "1.6.20"
  id("org.jetbrains.compose") version "1.1.1"
}

kotlin {
  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "15"
    }
  }
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(project(":guiCommon"))
        implementation(project(":middleLayer"))
        implementation(project(":wsClient"))
        implementation(compose.desktop.currentOs)
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.1")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
      }
    }
    val jvmTest by getting
  }
}

compose.desktop {
  application {
    mainClass = "MainKt"
    nativeDistributions {
      packageName = "DDTV Kotlin Client"
      packageVersion = "1.1.0"
    }
  }
}
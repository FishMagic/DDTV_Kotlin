plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization") version "1.6.20"
  application
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
        implementation(project(":cliCommon"))
        implementation(project(":middleLayer"))
        implementation(project(":wsClient"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.1")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
      }
    }
    val jvmTest by getting
  }
}

application {
  mainClass.set("MainKt")
}
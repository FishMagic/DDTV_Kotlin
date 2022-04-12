buildscript {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.20")
    classpath("com.android.tools.build:gradle:7.0.4")
  }
}
group = "me.ftmc.ddtv.kotlin"
version = "1.0"

repositories {
  mavenCentral()
}

allprojects {
  repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
}
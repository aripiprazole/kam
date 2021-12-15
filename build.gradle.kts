@file:Suppress("UNUSED_VARIABLE")

plugins {
  kotlin("multiplatform") version "1.6.0"
  id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
  id("io.gitlab.arturbosch.detekt") version "1.19.0"
}

group = "co.knoten"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

kotlin {
  jvm {
    withJava()

    compilations.all {
      kotlinOptions.jvmTarget = "16"
    }

    testRuns["test"].executionTask.configure {
      useJUnitPlatform()
    }
  }

  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.arrow.core)
        implementation(libs.ktor.server.core)
      }
    }
  }
}

detekt {
  buildUponDefaultConfig = true
  allRules = false

  config = files("$projectDir/config/detekt.yml")
  baseline = file("$projectDir/config/baseline.xml")
}

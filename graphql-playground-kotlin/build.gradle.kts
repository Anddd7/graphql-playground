import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/** -------------- import & apply plugins -------------- */

// import plugins into this project
plugins {
  val kotlinVersion = "1.3.61"

  idea

  kotlin("jvm") version kotlinVersion
}

/** -------------- configure imported plugin -------------- */

idea {
  project {
    jdkName = "11"
  }
  module {
    outputDir = file("$buildDir/idea-compiler/main")
    testOutputDir = file("$buildDir/idea-compiler/test")
  }
}

allprojects {
  /** -------------- project's properties -------------- */

  group = "com.github.anddd7"
  version = "0.0.1-SNAPSHOT"

  repositories {
    mavenCentral()
    jcenter()
  }

  buildscript {
    repositories {
      jcenter()
    }
  }
}

subprojects {
  apply(plugin = "java")
  apply(plugin = "kotlin")

  java.sourceCompatibility = JavaVersion.VERSION_11
  java.targetCompatibility = JavaVersion.VERSION_11

  /** -------------- dependencies management -------------- */

  dependencies {
    /* kotlin */
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test-junit5"))

    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.assertj:assertj-core:3.14.0")

    /* junit5 */
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.0")
  }

  /** -------------- configure tasks -------------- */

  tasks.withType<KotlinCompile>().all {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjsr305=strict")
      jvmTarget = "11"
    }
  }

  tasks.withType<Test> {
    useJUnitPlatform()
  }
}

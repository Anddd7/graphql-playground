pluginManagement {
  val kotlinVersion = "1.3.61"
  val springBootVersion = "2.2.2.RELEASE"
  val springDependencyVersion = "1.0.8.RELEASE"

  plugins {
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("org.springframework.boot") version springBootVersion
    id("io.spring.dependency-management") version springDependencyVersion
  }
}

rootProject.name = "graphql-playground-kotlin"

include("graphql-kotlin-spring")
include("graphql-java-spring")

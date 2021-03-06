plugins {
  kotlin("plugin.spring")

  id("org.springframework.boot")
  id("io.spring.dependency-management")
}

dependencies {
  /* graphql */
  implementation("com.expediagroup:graphql-kotlin-spring-server:2.0.0-RC10")

  /* webflux x reactor */
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(module = "junit")
    exclude(group = "org.mockito")
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
  }
  testImplementation("com.ninja-squad:springmockk:1.1.3")
  testImplementation("io.projectreactor:reactor-test")

  /* kotlin coroutines x reactor */
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
}

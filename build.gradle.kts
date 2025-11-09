plugins {
    id("org.springframework.boot") version "3.5.7" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("nu.studer.jooq") version "9.0" apply false
    kotlin("jvm") version "2.0.21" apply false
    java
}

allprojects {
    group = "com.mezei.aml"
    version = "0.0.1-SNAPSHOT"
    repositories { mavenCentral() }
}

subprojects {
    apply(plugin = "java")
    java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
    tasks.withType<Test>().configureEach { useJUnitPlatform() }
}

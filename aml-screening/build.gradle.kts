plugins {
    id("java")
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.mezei.aml"
version = "0.0.1-SNAPSHOT"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

configurations {
    compileOnly { extendsFrom(configurations.annotationProcessor.get()) }
}

repositories { mavenCentral() }

dependencies {
    implementation(project(":common-lib"))

    implementation("org.springframework.boot:spring-boot-starter-artemis")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // Drools / KIE
    implementation(platform("org.drools:drools-bom:8.44.0.Final"))
    implementation("com.thoughtworks.xstream:xstream:1.4.21")
    implementation("org.kie:kie-api")
    implementation("org.drools:drools-core")
    implementation("org.drools:drools-compiler")
    implementation("org.drools:drools-mvel")
    implementation("org.drools:drools-xml-support")

    // Netty native DNS MacOS ARM (M1/M2/M3/M4)
    implementation("io.netty:netty-resolver-dns-native-macos:4.1.114.Final:osx-aarch_64")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.jar { enabled = false }
tasks.bootJar { enabled = true }

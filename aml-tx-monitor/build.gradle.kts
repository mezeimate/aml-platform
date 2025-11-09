plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("nu.studer.jooq") version "9.0"
}

group = "com.mezei.aml"
version = "0.0.1-SNAPSHOT"
description = "Real-time AML transaction monitoring: rules, scoring, alerts."

java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

configurations {
    compileOnly { extendsFrom(configurations.annotationProcessor.get()) }
}

repositories { mavenCentral() }

dependencies {
    implementation(project(":common-lib"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.kafka:spring-kafka")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // --- jOOQ codegen deps (verziószinkron a runtime-hoz) ---
    jooqGenerator("org.postgresql:postgresql")
    jooqGenerator("org.jooq:jooq-meta:3.19.27")
    jooqGenerator("org.jooq:jooq-codegen:3.19.27")
}

val jooqVersion = "3.19.27"
val jooqGenDir = layout.buildDirectory.dir("generated-src/jooq/main")

jooq {
    version.set(jooqVersion)
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc = org.jooq.meta.jaxb.Jdbc().apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/aml"
                    user = "aml"
                    password = "aml"
                }
                generator = org.jooq.meta.jaxb.Generator().apply {
                    name = "org.jooq.codegen.JavaGenerator"
                    database = org.jooq.meta.jaxb.Database().apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        includes = "alert|alert_status"
                    }
                    generate = org.jooq.meta.jaxb.Generate().apply {
                        isPojos = true
                        isDaos = false
                        isFluentSetters = true
                        isJavaTimeTypes = true
                    }
                    target = org.jooq.meta.jaxb.Target().apply {
                        packageName = "com.mezei.aml.jooq"
                        directory = jooqGenDir.get().asFile.absolutePath
                    }
                }
            }
        }
    }
}

sourceSets {
    main {
        java.srcDir(jooqGenDir)
    }
}

tasks.withType<Test> {
	useJUnitPlatform()
}

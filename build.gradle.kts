import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    jacoco
}

group = "ar.edu.unsam-algo2"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val kotestVersion = "6.1.3"

dependencies {
    implementation("org.uqbar-project:geodds-xtend:1.0.3")
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")
    testImplementation("io.mockk:mockk:1.14.9")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}
tasks.test {
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    jvmArgs("-Xshare:off")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

jacoco {
    toolVersion = "0.8.14"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
}

tasks.register("runOnGitHub") {
    dependsOn("jacocoTestReport")
    group = "custom"
    description = "$ ./gradlew runOnGitHub # runs on GitHub Action"
}


java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
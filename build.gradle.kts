import info.solidsoft.gradle.pitest.PitestPluginExtension

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.6"
	id("io.spring.dependency-management") version "1.1.6"
	jacoco
	id("info.solidsoft.pitest") version "1.15.0"
}

group = "methodo.test"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
	testImplementation("io.kotest:kotest-assertions-core:5.9.1")
	testImplementation("io.kotest:kotest-property:5.9.1")
	testImplementation("io.mockk:mockk:1.13.7")
	testImplementation("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0")
	testImplementation("org.pitest:pitest-junit5-plugin:1.2.1")
	testImplementation("io.kotest.extensions:kotest-extensions-pitest:1.2.0")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

pitest {
	targetClasses.set(setOf("methodo.test.*"))
	targetTests.set(setOf("methodo.test.*"))
	threads.set(4)
	verbose.set(true)
}

configure<PitestPluginExtension> {
	targetClasses.set(listOf("methodo.test.*"))
}

jacoco {
	toolVersion = "0.8.12"
}

tasks.test {
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)

	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}


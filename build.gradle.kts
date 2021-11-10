import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "7.0.0"

}

group = "com.kry"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()

}

val vertxVersion = "4.2.0"
val junitJupiterVersion = "5.7.0"

val mainVerticleName = "com.kry.Insight.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClass.set(launcherClassName)
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-web-client")
  implementation("io.vertx:vertx-web-validation")
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-mysql-client")
  implementation("io.vertx:vertx-service-discovery")
  implementation("io.vertx:vertx-rx-java3")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.11.4")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.4")
  implementation("org.slf4j:slf4j-api:1.7.32")
  implementation("org.slf4j:jul-to-slf4j:1.7.32")
  implementation("org.slf4j:jcl-over-slf4j:1.7.32")
  implementation("org.apache.logging.log4j:log4j-to-slf4j:2.14.1")
  implementation("ch.qos.logback:logback-core:1.2.6")
  implementation("ch.qos.logback:logback-classic:1.2.6")
  implementation("org.projectlombok:lombok:1.18.22")
  annotationProcessor("org.projectlombok:lombok:1.18.22")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

java {
  sourceCompatibility = JavaVersion.VERSION_16
  targetCompatibility = JavaVersion.VERSION_16
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}

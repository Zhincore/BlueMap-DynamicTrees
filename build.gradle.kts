plugins {
  id("java")
  id("maven-publish")
  eclipse
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    maven("https://repo.bluecolored.de/releases")

    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("de.bluecolored:bluemap-core:5.15")
    implementation("de.bluecolored:bluemap-common:5.15")
    implementation("de.bluecolored:bluemap-api:2.7.7")

    compileOnly("com.github.Uiniel:BlueMapModelLoaders:3119e9b")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.processResources {
    val projectVersion = project.version.toString()
    filesMatching("bluemap.addon.json") {
        expand(mapOf("version" to projectVersion))
    }
}

tasks.build {
    dependsOn(tasks.jar)
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}

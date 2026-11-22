import java.time.Duration

// Minimum Java version targeted by the published library
val JAVA_RELEASE = 17

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    alias(libs.plugins.spotless)
    alias(libs.plugins.nexus.publish)
}

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()
}

spotless {
    java {
        googleJavaFormat()
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation", "-Werror"))
    options.release.set(JAVA_RELEASE)
}

dependencies {
    api(platform(libs.jackson.bom))
    api(libs.jackson.core)
    api(libs.jackson.databind)
    api(libs.jackson.datatype.jsr310)
    api(libs.graphql.java)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.hamcrest)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

tasks.javadoc {
    (options as StandardJavadocDocletOptions).apply {
        overview = file("src/overview.html").absolutePath
        addBooleanOption("Xdoclint:all,-missing", true)
    }
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "New Relic, Inc."
            )
        )
    }
}

val useLocalSonatype = project.findProperty("useLocalSonatype") == "true"

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set(project.name)
                description.set("Helpers for integrating the graphql-java library with your JVM application")
                url.set("https://github.com/newrelic/newrelic-graphql-java-core")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("newrelic")
                        name.set("New Relic")
                        email.set("opensource@newrelic.com")
                    }
                }
                scm {
                    url.set("git@github.com:newrelic/newrelic-graphql-java-core.git")
                    connection.set("scm:git:git@github.com:newrelic/newrelic-graphql-java-core.git")
                }
            }
        }
    }

    repositories {
        if (useLocalSonatype) {
            maven {
                val releasesRepoUrl = uri("http://localhost:8081/repository/maven-releases/")
                val snapshotsRepoUrl = uri("http://localhost:8081/repository/maven-snapshots/")
                url = if (project.version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                isAllowInsecureProtocol = true
            }
        }
    }
}

signing {
    val signingKey = project.findProperty("signingKey") as String?
    val signingKeyId = project.findProperty("signingKeyId") as String?
    val signingPassword = project.findProperty("signingPassword") as String?

    if (signingKey != null && signingKeyId != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    }
}

// Only configure nexusPublishing when not using local Sonatype
if (!useLocalSonatype) {
    nexusPublishing {
        repositories {
            sonatype {
                // Sonatype Central Portal's OSSRH-compatible staging API
                nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
                snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))

                // Username and password are a user token generated in the Sonatype Central Portal
                val sonatypeUsername = System.getenv("SONATYPE_USERNAME")
                    ?: (project.findProperty("sonatypeUsername") as String?)
                val sonatypePassword = System.getenv("SONATYPE_PASSWORD")
                    ?: (project.findProperty("sonatypePassword") as String?)

                username.set(sonatypeUsername)
                password.set(sonatypePassword)
            }
        }

        // Configure timeouts for large uploads
        connectTimeout.set(Duration.ofMinutes(3))
        clientTimeout.set(Duration.ofMinutes(3))
    }
}

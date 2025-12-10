import java.time.Duration

val graphqlVersion = project.findProperty("graphql.version") as String
val jacksonCoreVersion = project.findProperty("jackson-core.version") as String
val jacksonDatabindVersion = project.findProperty("jackson-databind.version") as String
val junitVersion = project.findProperty("junit.version") as String
val mockitoVersion = project.findProperty("mockito.version") as String

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("com.github.sherter.google-java-format") version "0.9"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:unchecked")
    options.compilerArgs.add("-Xlint:deprecation")
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-core:${jacksonCoreVersion}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonDatabindVersion}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonCoreVersion}")
    implementation("com.graphql-java:graphql-java:${graphqlVersion}")

    testImplementation("junit:junit:${junitVersion}")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
}

tasks.withType<Javadoc> {
    options.overview = "${file("src/overview.html")}"
}

tasks {
    val taskScope = this
    val sources = sourceSets
    val sourcesJar by creating(Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        archiveClassifier.set("sources")
        from(sources.main.get().allSource)
    }

    val javadocJar by creating(Jar::class) {
        dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
        archiveClassifier.set("javadoc")
        from(taskScope.javadoc)
    }

    val jar: Jar by taskScope
    jar.apply {
        manifest {
            attributes(mapOf(
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to "New Relic, Inc."
            ))
        }
    }
}

val useLocalSonatype = project.properties["useLocalSonatype"] == "true"

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            pom {
                name.set(project.name)
                description.set("Helpers for integrating the graphql-java library with your JVM application")
                url.set("https://github.com/newrelic/newrelic-graphql-java-core")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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
                val releasesRepoUrl = project.uri("http://localhost:8081/repository/maven-releases/")
                val snapshotsRepoUrl = project.uri("http://localhost:8081/repository/maven-snapshots/")
                url = if (project.version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            }
        }
    }
}

signing {
    val signingKey: String? = project.properties["signingKey"] as String?
    val signingKeyId: String? = project.properties["signingKeyId"] as String?
    val signingPassword: String? = project.properties["signingPassword"] as String?

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
                // Configure for new Sonatype Central Portal
                // See: https://central.sonatype.org/publish/publish-portal-gradle/
                nexusUrl.set(uri("https://central.sonatype.com/api/v1/publisher/"))
                snapshotRepositoryUrl.set(uri("https://central.sonatype.com/api/v1/publisher/"))

                // Authentication: uses user token from Sonatype Central Portal
                // Username is your Sonatype Central username
                // Password is the generated token from the portal (not your account password)
                val sonatypeUsername = System.getenv("SONATYPE_USERNAME")
                    ?: (project.properties["sonatypeUsername"] as String?)
                val sonatypePassword = System.getenv("SONATYPE_PASSWORD")
                    ?: (project.properties["sonatypePassword"] as String?)

                username.set(sonatypeUsername)
                password.set(sonatypePassword)
            }
        }

        // Configure timeouts for large uploads
        connectTimeout.set(Duration.ofMinutes(3))
        clientTimeout.set(Duration.ofMinutes(3))
    }
}

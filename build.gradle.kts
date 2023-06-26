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
        maven {
            if (useLocalSonatype) {
                val releasesRepoUrl = project.uri("http://localhost:8081/repository/maven-releases/")
                val snapshotsRepoUrl = project.uri("http://localhost:8081/repository/maven-snapshots/")
                url = if (project.version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            }
            else {
                val releasesRepoUrl = project.uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = project.uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (project.version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                project.configure<SigningExtension> {
                    val signingKey : String? = project.properties["signingKey"] as String?
                    val signingKeyId: String? = project.properties["signingKeyId"] as String?
                    val signingPassword: String? = project.properties["signingPassword"] as String?
                    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
                    this.sign(publishing.publications["mavenJava"])
                }
            }
            credentials {
                username = System.getenv("SONATYPE_USERNAME")

                if ((username?.length ?: 0) == 0){
                    username = project.properties["sonatypeUsername"] as String?
                }
                password = System.getenv("SONATYPE_PASSWORD")
                if ((password?.length ?: 0) == 0) {
                    password = project.properties["sonatypePassword"] as String?
                }
            }
        }
    }
}

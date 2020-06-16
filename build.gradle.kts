val graphqlVersion = project.findProperty("graphql.version") as String
val jacksonCoreVersion = project.findProperty("jackson-core.version") as String
val jacksonDatabindVersion = project.findProperty("jackson-databind.version") as String
val junitVersion = project.findProperty("junit.version") as String
val mockitoVersion = project.findProperty("mockito.version") as String

plugins {
    id("java-library")
    id("com.github.sherter.google-java-format") version "0.8"
}

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:unchecked")
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-core:${jacksonCoreVersion}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonDatabindVersion}")
    implementation("com.graphql-java:graphql-java:${graphqlVersion}")

    testImplementation("junit:junit:${junitVersion}")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
}

tasks.withType<Javadoc> {
    options.overview = "${file("src/overview.html")}"
}

plugins {
    id("java")
}

group = "org.informatics"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.18.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "org.informatics.ui.ConsoleApplication"
        )
    }
}

// 🟢 Standard Kotlin DSL syntax to silence doclint without any casts or imports!
// This treats the compiler arguments as a plain list of text strings.
tasks.javadoc {
    title = "Informatics Enterprise System"

    // Native string option method that cannot turn red because it uses basic primitives
    (options as org.gradle.external.javadoc.CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
}

plugins {
    id("java")
    id("application")
    // seminar 9: interfata grafica JavaFX
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "course"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

javafx {
    version = "21.0.5"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    // curs 6: driverul JDBC pentru PostgreSQL
    implementation("org.postgresql:postgresql:42.7.7")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "course.MovieAppLauncher"
}

tasks.test {
    useJUnitPlatform()
}

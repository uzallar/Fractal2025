import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    //id("org.jetbrains.compose") version "1.6.11"
}

group = "ru.gr206"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("androidx.lifecycle:lifecycle-viewmodel-desktop:2.9.4")
    implementation(compose.material3)



    //implementation("org.jetbrains.compose.ui:ui-graphics")

    testImplementation(kotlin("test")) // без этого тесты не работали
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")//это тоже
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")


}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Fractal2025"
            packageVersion = "1.0.0"
        }
    }
}
tasks.withType<Test> {
    useJUnitPlatform()   // обязательно для JUnit 5
}
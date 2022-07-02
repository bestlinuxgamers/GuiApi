import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
}

group = "net.bestlinuxgamers"

val versionType: String? by project
val versionCount: String by project
version = "$versionCount-${versionType ?: "SNAPSHOT"}" //TODO add recon

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") //Spigot
}

//dependency version vars
val spigotVersion: String by project

dependencies {
    //Spigot
    compileOnly("org.spigotmc:spigot-api:$spigotVersion")
    //tests
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

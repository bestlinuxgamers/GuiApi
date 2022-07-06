import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
    id("org.ajoberstar.reckon") version "0.16.1"
}

group = "net.bestlinuxgamers"

reckon {
    stages("beta", "rc", "final")

    setScopeCalc(calcScopeFromProp().or(calcScopeFromCommitMessages()))
    setStageCalc(calcStageFromProp())
}

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
    testImplementation("io.mockk:mockk:1.12.4")
    testImplementation(files("./testDependencies/spigot-1.16.5.jar"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

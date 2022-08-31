import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
    id("org.ajoberstar.reckon") version "0.16.1"
    id("org.jetbrains.dokka") version "1.7.10"
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

//task settings

val targetCompatibility = JavaVersion.VERSION_1_8.toString()

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = targetCompatibility
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.current().toString()
    targetCompatibility = targetCompatibility
}

tasks.test {
    useJUnitPlatform()
}

//custom tasks

tasks.register<org.gradle.jvm.tasks.Jar>("javadocJar") {
    group = "documentation"
    archiveClassifier.set("javadoc")
    from(tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>()["dokkaHtml"].outputDirectory)
}

tasks.register<org.gradle.jvm.tasks.Jar>("sourcesJar") {
    group = "documentation"
    archiveClassifier.set("sources")
    from(project.sourceSets["main"].allSource)
}

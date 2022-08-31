import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
    id("org.ajoberstar.reckon") version "0.16.1"
    id("org.jetbrains.dokka") version "1.7.10"
    id("maven-publish")
    signing
}

group = "net.bestlinuxgamers.guiapi"

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

//publish

publishing {
    publications {
        create<MavenPublication>("maven") {
            pom {
                name.set(rootProject.name)
                description.set("Library for creating component-based animated Minecraft inventory GUI's.")
                scm {
                    connection.set("scm:git:https://github.com/bestlinuxgamers/GuiApi.git")
                    developerConnection.set("scm:git:ssh://git@github.com/bestlinuxgamers/GuiApi.git")
                    url.set("https://github.com/bestlinuxgamers/GuiApi")
                }
                licenses {
                    license {
                        name.set("GNU LESSER GENERAL PUBLIC LICENSE, Version 3")
                        url.set("https://www.gnu.org/licenses/lgpl-3.0.txt")
                    }
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/bestlinuxgamers/GuiApi/issues")
                }
                developers {
                    developer {
                        id.set("bestlinuxgamers")
                        name.set("bestlinuxgamers")
                        email.set("52172848+bestlinuxgamers@users.noreply.github.com")
                    }
                }
            }

            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            from(components["kotlin"])
            the<SigningExtension>().sign(this)
        }
    }

    repositories {
        maven {
            name = "github"
            val githubRepoOwner: String by project
            val githubRepo: String by project
            url = uri("https://maven.pkg.github.com/$githubRepoOwner/$githubRepo")

            credentials(PasswordCredentials::class)
        }
        maven {
            name = "gitea"
            val giteaInstance: String by project
            val giteaUsername: String by project
            url = uri("https://$giteaInstance/api/packages/$giteaUsername/maven")

            credentials(HttpHeaderCredentials::class.java) {
                name = "Authorization"
                value = "token ${findProperty("giteaToken")}"
            }

            authentication {
                @Suppress("UNUSED_VARIABLE")
                val header by registering(HttpHeaderAuthentication::class)
            }
        }
    }
}

signing {
    useGpgCmd()
}

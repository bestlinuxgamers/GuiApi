import org.ajoberstar.reckon.core.Scope

plugins {
    kotlin("jvm") version "2.1.20"
    id("org.ajoberstar.reckon") version "0.19.1"
    id("org.jetbrains.dokka") version "2.0.0"
    `maven-publish`
    signing
}

group = "net.bestlinuxgamers.guiapi"

reckon {
    setDefaultInferredScope(Scope.PATCH)
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
    testImplementation("io.mockk:mockk:1.13.2")
    testImplementation(files("./testDependencies/spigot-1.16.5.jar"))
}

//task settings

kotlin {
    jvmToolchain(8)
}

tasks.test {
    useJUnitPlatform()
}

//custom tasks

tasks.register<Jar>("dokkaHtmlJar") {
    group = "documentation"
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-docs")
}

tasks.register<Jar>("javadocJar") {
    group = "documentation"
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

tasks.register<Jar>("sourcesJar") {
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
                description.set("Library for creating component-based animated Minecraft inventory GUIs.")
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
                @Suppress("UNUSED_VARIABLE", "KotlinRedundantDiagnosticSuppress")
                val header by registering(HttpHeaderAuthentication::class)
            }
        }
    }
}

signing {
    useGpgCmd()
}

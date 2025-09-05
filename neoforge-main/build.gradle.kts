@file:Suppress("SpellCheckingInspection")

import org.slf4j.event.Level
import java.text.SimpleDateFormat
import java.util.*

var envVersion: String = System.getenv("VERSION") ?: "9.9.9"
if (envVersion.startsWith("v"))
    envVersion = envVersion.trimStart('v')

val modId: String = "compactmachines"

val coreApi = project(":core-api")

plugins {
    java
    id("idea")
    id("eclipse")
    id("maven-publish")
    alias(neoforged.plugins.moddev)
}

project.evaluationDependsOn(coreApi.path)

base {
    archivesName.set(modId)
    group = "dev.compactmods.compactmachines"
    version = envVersion
}

java {
    toolchain.vendor.set(JvmVendorSpec.JETBRAINS)
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

sourceSets.main {
    java {
        srcDir("src/main/java")
    }

    resources {
        srcDir("src/main/resources")
        srcDir("src/generated/resources")
    }
}

sourceSets.test {
    java {
        srcDir("src/test/java")
    }

    resources {
        srcDir("src/test/resources")
    }
}

neoForge {
    version = neoforged.versions.neoforge.get()

    interfaceInjectionData.from("src/main/resources/interfaces.json")

    val cmMain = this.mods.create(modId) {
        modSourceSets.add(coreApi.sourceSets.main)
        modSourceSets.add(sourceSets.main)

        if(System.getenv().containsKey("CI")) {
            modSourceSets.add(sourceSets.test)
        }
    }

    unitTest {
        enable()
        testedMod = mods.named(modId)
    }

    parchment {
        enabled = true
        mappingsVersion = libs.versions.parchment
        minecraftVersion = libs.versions.parchmentMC
    }

    runs {
        // applies to all the run configs below
        configureEach {
            logLevel.set(Level.DEBUG)
            sourceSet = project.sourceSets.main

            // JetBrains Runtime Hotswap
            if (!System.getenv().containsKey("CI")) {
              jvmArgument("-XX:+AllowEnhancedClassRedefinition")
            }

            systemProperties.put("terminal.ansi", "true")

            var additional = this.additionalRuntimeClasspathConfiguration
            additional.dependencies.add(compactmods.feather.get())
            additional.dependencies.add(libs.jnanoid.get())
        }
        
        create("client") {
            client()
            gameDirectory.set(file("runs/client"))

            // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
            systemProperty("forge.enabledGameTestNamespaces", modId)

            programArguments.addAll("--username", "Nano")
            programArguments.addAll("--width", "1920")
            programArguments.addAll("--height", "1080")
        }

        create("client2") {
            client()
            gameDirectory.set(file("runs/client"))

            // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
            systemProperty("forge.enabledGameTestNamespaces", modId)

            programArguments.addAll("--username", "Nano2")
            programArguments.addAll("--width", "1920")
            programArguments.addAll("--height", "1080")
        }

        create("server") {
            server()
            gameDirectory.set(file("runs/server"))
            programArgument("nogui")
        }

        create("gameTestServer") {
            type = "gameTestServer"
            gameDirectory.set(file("runs/gametest"))

            systemProperty("neoforge.enabledGameTestNamespaces", modId)
            environment.put("CM_TEST_RESOURCES", file("src/test/resources").path)
            environment.put("CM_GAMETEST_ENABLED", "true")

            loadedMods.add(cmMain)

            sourceSet.set(sourceSets.test)
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral {
        name = "Central"
        content {
            includeGroup("com.aventrix.jnanoid")
        }
    }

    maven("https://maven.pkg.github.com/compactmods/feather") {
        name = "Github PKG - Feather"
        content {
            includeGroup("dev.compactmods")
            includeModule("dev.compactmods", "feather")
        }

        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }

    maven("https://maven.pkg.github.com/compactmods/gander") {
        name = "Github PKG - Gander"
        content { includeGroup("dev.compactmods.gander") }
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }

    maven("https://maven.blamejared.com/") {
        // location of the maven that hosts JEI files since January 2023
        name = "Jared's maven"
    }

    maven("https://www.cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }

    maven("https://modmaven.dev") {
        // location of a maven mirror for JEI files, as a fallback
        name = "ModMaven"
    }

    maven("https://maven.theillusivec4.top/") {
        name = "Illusive Soulworks maven (Curios API)"
        content {
            includeGroup("top.theillusivec4.curios")
        }
    }

    // saps.dev Maven (KubeJS and Rhino)
    maven("https://maven.saps.dev/minecraft") {
        content {
            includeGroup("dev.latvian.mods")
        }
    }
}

dependencies {
    // Core Projects and Libraries
    this {
        compileOnly(libs.jnanoid)
        testImplementation(libs.jnanoid)
        jarJar(libs.jnanoid)

        compileOnly(coreApi)
        testCompileOnly(coreApi)
        jarJar(coreApi)
    }

    testImplementation(neoforged.testframework)
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")


    compileOnly(compactmods.feather)
    testImplementation(compactmods.feather)
    jarJar(compactmods.feather) { isTransitive = false }

    implementation(compactmods.spatial)
    testImplementation(compactmods.spatial)
    jarJar(compactmods.spatial) { isTransitive = false }

    // Gander
    implementation(compactmods.bundles.gander)
    accessTransformers(compactmods.ganderRendering)
    jarJar(compactmods.bundles.gander)

    // Curios API
    compileOnly(libs.curios)

    // KubeJS Support
    compileOnly(mods.kubeJS)
    compileOnly(mods.rhino)

    // Jade Support
    compileOnly(mods.jade)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(arrayOf("-Xmaxerrs", "9000"))
}

tasks.withType<Jar> {

    val gitVersion = providers.exec {
        commandLine("git", "rev-parse", "HEAD")
    }.standardOutput.asText.get()

    manifest {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
        attributes(
            mapOf(
                "Implementation-Version" to archiveVersion,
                "Implementation-Timestamp" to now,
                "Minecraft-Version" to mojang.versions.minecraft.get(),
                "NeoForge-Version" to neoforged.versions.neoforge.get(),
                "Main-Commit" to gitVersion.trimEnd()
            )
        )

        from("src/main/resources/META-INF/MANIFEST.MF")
    }
}

tasks.withType<ProcessResources>().configureEach {
    val replaceProperties: Map<String, Any> = mapOf(
        "minecraft_version" to mojang.versions.minecraft.get(),
        "neo_version" to neoforged.versions.neoforge.get(),
        "minecraft_version_range" to mojang.versions.minecraftRange.get(),
        "neo_version_range" to neoforged.versions.neoforgeRange.get(),
        "loader_version_range" to "[1,)",
        "mod_id" to modId,
        "mod_version" to envVersion
    )

    inputs.properties(replaceProperties)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(replaceProperties)
    }
}

val PACKAGES_URL = System.getenv("GH_PKG_URL") ?: "https://maven.pkg.github.com/compactmods/compactmachines"
publishing {
    publications.register<MavenPublication>("compactmachines") {
        artifactId = "$modId-neoforge"
        from(components.getByName("java"))
    }

    repositories {
        // GitHub Packages
        maven(PACKAGES_URL) {
            name = "GitHubPackages"
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
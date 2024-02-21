@file:Suppress("SpellCheckingInspection")

import net.neoforged.gradle.dsl.common.runs.run.Run
import org.ajoberstar.grgit.Grgit
import java.text.SimpleDateFormat
import java.util.*

var envVersion: String = System.getenv("VERSION") ?: "9.9.9"
if (envVersion.startsWith("v"))
    envVersion = envVersion.trimStart('v')

val modId: String = property("mod_id") as String
val isRelease: Boolean = (System.getenv("RELEASE") ?: "false").equals("true", true)

val core = project(":core:core")
val coreApi = project(":core:core-api")
val roomApi = project(":core:room-api")
val roomUpgradeApi = project(":core:room-upgrade-api")

val coreProjects = listOf(core, coreApi, roomApi, roomUpgradeApi)

plugins {
    id("idea")
    id("eclipse")
    id("maven-publish")
    id("java-library")
    id("net.neoforged.gradle.userdev") version ("7.0.93")
    id("org.ajoberstar.grgit") version("5.2.1")
}

coreProjects.forEach {
    project.evaluationDependsOn(it.path)
}

base {
    archivesName.set(modId)
    group = "dev.compactmods.compactmachines"
    version = envVersion
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

jarJar.enable()

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

minecraft {
    modIdentifier.set(modId)
    accessTransformers.file(project.file("src/main/resources/META-INF/accesstransformer.cfg"))
}

runs {
    // applies to all the run configs below
    configureEach {
        // Recommended logging data for a userdev environment
        systemProperty("forge.logging.markers", "") // 'SCAN,REGISTRIES,REGISTRYDUMP'

        // Recommended logging level for the console
        systemProperty("forge.logging.console.level", "debug")

        dependencies {
            runtime("dev.compactmods:feather:${libraries.versions.feather.get()}")
            runtime("com.aventrix.jnanoid:jnanoid:2.0.0")
        }

        if(!System.getenv().containsKey("CI")) {
            // JetBrains Runtime Hotswap
            jvmArgument("-XX:+AllowEnhancedClassRedefinition")
        }

        modSource(sourceSets.main.get())
        coreProjects.forEach {
            modSource(it.sourceSets.main.get())
        }
    }

    create("client") {
        // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
        systemProperty("forge.enabledGameTestNamespaces", modId)

        programArguments("--username", "Nano")
        programArguments("--width", "1920")
        programArguments("--height", "1080")
    }

    create("server") {
        systemProperty("forge.enabledGameTestNamespaces", modId)
        environmentVariables("CM_TEST_RESOURCES", project.file("src/test/resources").path)
        modSource(project.sourceSets.test.get())
    }

    create("gameTestServer") {
        systemProperty("forge.enabledGameTestNamespaces", modId)
        environmentVariable("CM_TEST_RESOURCES", file("src/test/resources").path)
        modSource(project.sourceSets.test.get())
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

    maven("https://maven.pkg.github.com/compactmods/compactmachines-core") {
        name = "Github PKG Core"
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation(libraries.neoforge)

    implementation(libraries.jnanoid)
    jarJar(libraries.jnanoid)

    compileOnly(core)
    compileOnly(coreApi)
    compileOnly(roomApi)
    compileOnly(roomUpgradeApi)

    testCompileOnly(core)
    testCompileOnly(coreApi)
    testCompileOnly(roomApi)
    testCompileOnly(roomUpgradeApi)

    implementation(libraries.feather)
    jarJar(libraries.feather) {
        isTransitive = false
    }
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Jar> {

    val coreGit = Grgit.open {
        currentDir = project.rootDir.resolve("core")
    }

    val mainGit = Grgit.open {
        currentDir = project.rootDir
    }

    manifest {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
        attributes(mapOf(
                "Specification-Title" to "Compact Machines",
                "Specification-Vendor" to "CompactMods",
                "Specification-Version" to "2",
                "Implementation-Title" to "Compact Machines",
                "Implementation-Version" to archiveVersion,
                "Implementation-Vendor" to "CompactMods",
                "Implementation-Timestamp" to now,
                "Minecraft-Version" to libraries.versions.minecraft.get(),
                "NeoForge-Version" to libraries.versions.neoforge.get(),
                "Main-Commit" to mainGit.head().id,
                "Core-Commit" to coreGit.head().id
        ))
    }
}

tasks.jar {
    archiveClassifier.set("slim")
    from(sourceSets.main.get().output)
    coreProjects.forEach {
        from (it.sourceSets.main.get().output)
    }
}

tasks.jarJar {
    archiveClassifier.set("")
    from(sourceSets.main.get().output)
    coreProjects.forEach {
        from (it.sourceSets.main.get().output)
    }
}

val PACKAGES_URL = System.getenv("GH_PKG_URL") ?: "https://maven.pkg.github.com/compactmods/compactmachines"
publishing {
    publications.register<MavenPublication>("compactmachines") {
        artifactId = "$modId-neoforge"
        this.artifact(tasks.jarJar)
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
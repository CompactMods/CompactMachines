
import net.minecraftforge.gradle.userdev.UserDevExtension
import java.text.SimpleDateFormat
import java.util.*

val modVersion: String = System.getenv("CM_VERSION") ?: "9.9.9"

val coreVersion: String = property("core_version") as String
val tunnelsApiVersion: String = property("tunnels_version") as String

plugins {
    id("idea")
    id("eclipse")
    id("maven-publish")
    id("net.minecraftforge.gradle") version ("5.1.+")
    id("org.parchmentmc.librarian.forgegradle") version ("1.+")
}

base {
    group = "dev.compactmods.compactmachines"
    version = modVersion
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

var minecraft_version: String by extra
var forge_version: String by extra
var parchment_version: String by extra

sourceSets {
    named("main") {
        resources {
            //The API has no resources
            setSrcDirs(emptyList<String>())
        }
    }

    named("test") {
        resources {
            //The test module has no resources
            setSrcDirs(emptyList<String>())
        }
    }
}

configure<UserDevExtension> {
    mappings("parchment", "${parchment_version}-${minecraft_version}")
    accessTransformer(file("../forge-main/src/main/resources/META-INF/accesstransformer.cfg"))
}

repositories {
    maven("https://maven.pkg.github.com/compactmods/compactmachines-core") {
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    minecraft ("net.minecraftforge", "forge", version = "${minecraft_version}-${forge_version}")

    implementation("dev.compactmods.compactmachines", "core-api", coreVersion) {
        isTransitive = false
    }

    implementation("dev.compactmods.compactmachines", "tunnels-api", tunnelsApiVersion) {
        isTransitive = false
    }
}

tasks.withType<Jar> {
    manifest {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
        attributes(mapOf(
                "Specification-Title" to "Compact Machines API",
                "Specification-Vendor" to "",
                "Specification-Version" to "1", // We are version 1 of ourselves
                "Implementation-Title" to "Compact Machines API",
                "Implementation-Version" to archiveVersion,
                "Implementation-Vendor" to "",
                "Implementation-Timestamp" to now
        ))
    }
}

tasks.jar {
    archiveClassifier.set("api")
    finalizedBy("reobfJar")
}

tasks.named<Jar>("sourcesJar") {
    archiveClassifier.set("api-sources")
}

artifacts {
    archives(tasks.jar.get())
    archives(tasks.named("sourcesJar").get())
}

val PACKAGES_URL = System.getenv("GH_PKG_URL") ?: "https://maven.pkg.github.com/compactmods/compactmachines"
publishing {
    publications.register<MavenPublication>("forge-tunnels-api") {
        from(components.findByName("java"))
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
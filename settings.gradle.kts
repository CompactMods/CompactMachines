dependencyResolutionManagement {
    addVersionCatalog(this, "neoforged")
    addVersionCatalog(this, "mojang")
    addVersionCatalog(this, "compactmods")
    addVersionCatalog(this, "mods")
}

pluginManagement {
    plugins {
        id("idea")
        id("eclipse")
        id("maven-publish")
    }

    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()

        // maven("https://maven.architectury.dev/")

        maven("https://maven.parchmentmc.org") {
            name = "ParchmentMC"
        }

        maven("https://maven.neoforged.net/releases") {
            name = "NeoForged"
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}

include(":api")
include(":neoforge")
include(":compat")

fun addVersionCatalog(dependencyResolutionManagement: DependencyResolutionManagement, name: String) {
    dependencyResolutionManagement.versionCatalogs.create(name) {
        from(files("./gradle/$name.versions.toml"))
    }
}

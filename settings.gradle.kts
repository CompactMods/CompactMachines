dependencyResolutionManagement {
    versionCatalogs.create("libraries") {
        library("feather", "dev.compactmods", "feather")
                .versionRef("feather")

        library("jnanoid", "com.aventrix.jnanoid", "jnanoid")
                .versionRef("jnanoid")

        library("neoforge", "net.neoforged", "neoforge")
                .versionRef("neoforge")

        version("minecraft", "1.20.4")
        version("feather", "[0.1.8, 2.0)")
        version("jnanoid", "[2.0.0, 3)")
        version("neoforge", "20.4.203")
    }

    versionCatalogs.create("mods") {
        this.library("jei-common", "mezz.jei", "jei-1.20.4-common-api").versionRef("jei")
        this.library("jei-neo", "mezz.jei", "jei-1.20.4-neoforge-api").versionRef("jei");
        this.bundle("jei", listOf("jei-common", "jei-neo"))
        this.version("jei", "17.3.0.49")

        this.library("jade", "curse.maven", "jade-324717").version("5109393")
    }
}

pluginManagement {
    plugins {
        id("idea")
        id("eclipse")
        id("maven-publish")
        id("java-library")
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

        maven("https://repo.spongepowered.org/repository/maven-public/") {
            name = "Sponge Snapshots"
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.8.0")
}

include(":core:core")
include(":core:core-api")
include(":core:room-api")
include(":core:room-upgrade-api")

project(":core:core").projectDir = file("./core/core")
project(":core:core-api").projectDir = file("./core/core-api")
project(":core:room-api").projectDir = file("./core/room-api")
project(":core:room-upgrade-api").projectDir = file("./core/room-upgrade-api")

include("neoforge-main")
include("neoforge-datagen")


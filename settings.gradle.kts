pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.minecraftforge.net")

        maven("https://maven.parchmentmc.org") {
            name = "ParchmentMC"
        }

        maven("https://maven.fabricmc.net") {
            name = "Fabric"
        }
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "net.minecraftforge.gradle") {
                useModule("${requested.id}:ForgeGradle:${requested.version}")
            }
        }
    }

    plugins {
        id("fabric-loom").version(settings.extra["loom_version"] as String)
    }
}

rootProject.name = "Compact Machines"
include("common-api", "common-main")
include("forge-api", "forge-main", "forge-tunnels")

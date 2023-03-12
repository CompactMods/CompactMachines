pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.minecraftforge.net") {
            name = "Minecraft Forge"
        }

        maven("https://maven.parchmentmc.org") {
            name = "ParchmentMC"
        }
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "net.minecraftforge.gradle") {
                useModule("${requested.id}:ForgeGradle:${requested.version}")
            }
        }
    }
}

rootProject.name = "Compact Machines"
include("forge-tunnels-api")
include("forge-main")
include("forge-builtin")

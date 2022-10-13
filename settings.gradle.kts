pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.spongepowered.org/repository/maven-public")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.parchmentmc.org")
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
include("forge-api", "forge-main", "forge-tunnels")
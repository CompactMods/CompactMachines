val coreVersion: String = property("core_version") as String
val tunnelsApiVersion: String = property("tunnels_version") as String

plugins {
    id("net.minecraftforge.gradle") version("5.1.+")
    id("org.parchmentmc.librarian.forgegradle") version("1.+")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

var minecraft_version: String by extra
var forge_version: String by extra
var parchment_version: String by extra

project.evaluationDependsOn(project(":forge-tunnels-api").path)

repositories {
    maven("https://maven.pkg.github.com/compactmods/compactmachines-core") {
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GH_PKG_USER")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GH_PKG_TOKEN")
        }
    }
}


dependencies {
    minecraft (group = "net.minecraftforge", name = "forge", version = "${minecraft_version}-${forge_version}")

    implementation(project(":forge-tunnels-api"))

    implementation("dev.compactmods.compactmachines", "core-api", coreVersion) {
        isTransitive = false
    }

    implementation("dev.compactmods.compactmachines", "tunnels-api", tunnelsApiVersion) {
        isTransitive = false
    }
}

minecraft {
    mappings("parchment", "${parchment_version}-${minecraft_version}")
    accessTransformer(file("../forge-main/src/main/resources/META-INF/accesstransformer.cfg"))
}
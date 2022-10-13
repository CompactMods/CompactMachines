plugins {
    java
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

project.evaluationDependsOn(project(":forge-api").path)

dependencies {
    minecraft (group = "net.minecraftforge", name = "forge", version = "${minecraft_version}-${forge_version}")

    compileOnly(project(":forge-api"))
}

minecraft {
    mappings("parchment", parchment_version)
    accessTransformer(file("../forge-main/src/main/resources/META-INF/accesstransformer.cfg"))
}

tasks.jar {
    finalizedBy("reobfJar")
}
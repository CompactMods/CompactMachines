plugins {
    java
    id("net.minecraftforge.gradle") version ("5.1.+")
    id("org.parchmentmc.librarian.forgegradle") version ("1.+")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

var minecraft_version: String by extra
var forge_version: String by extra
var parchment_version: String by extra

minecraft {
    mappings("parchment", parchment_version)
}

dependencies {
    minecraft("net.minecraftforge", "forge", version = "${minecraft_version}-${forge_version}")
}
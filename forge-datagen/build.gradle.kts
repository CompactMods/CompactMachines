val modVersion: String = System.getenv("CM_VERSION") ?: "9.9.9"

val coreVersion: String = property("core_version") as String
val tunnelsApiVersion: String = property("tunnels_version") as String

var modId = property("mod_id") as String
var minecraft_version: String by extra
var forge_version: String by extra
var parchment_version: String by extra

val forgeMain = project(":forge-main")

plugins {
    id("net.minecraftforge.gradle") version ("5.1.+")
    id("org.parchmentmc.librarian.forgegradle") version ("1.+")
}

base {
    group = "dev.compactmods.compactmachines"
    version = modVersion
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

sourceSets.main {
    resources {
        srcDir(file("src/main/resources"))
    }
}

repositories {
    mavenLocal()

    maven("https://maven.pkg.github.com/compactmods/compactmachines-core") {
        name = "Github PKG Core"
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

val runDepends: List<Project> = listOf(
        project(":forge-tunnels-api"),
        project(":forge-builtin"),
        forgeMain
)

runDepends.forEach {
    project.evaluationDependsOn(it.path)
}

dependencies {
    minecraft("net.minecraftforge", "forge", version = "${minecraft_version}-${forge_version}")

    implementation("dev.compactmods.compactmachines:core-api:$coreVersion")
    implementation("dev.compactmods.compactmachines:core:$coreVersion")
    implementation("dev.compactmods.compactmachines:tunnels-api:$tunnelsApiVersion")

    compileOnly(project(":forge-tunnels-api"))
    compileOnly(project(":forge-builtin"))
    compileOnly(project(":forge-main")) {
        isTransitive = false
    }
}

minecraft {
    mappings("parchment", "${parchment_version}-${minecraft_version}")
    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        all {
            // Recommended logging data for a userdev environment
            property("forge.logging.markers", "") // "SCAN,REGISTRIES,REGISTRYDUMP"

            // Recommended logging level for the console
            property("forge.logging.console.level", "debug")

            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${buildDir}/createSrgToMcp/output.srg")

            ideaModule("Compact_Machines.forge-datagen.main")

            source(sourceSets.main.get())
            mods.create(modId) {
                source(sourceSets.main.get())
                for (p in runDepends)
                    source(p.sourceSets.main.get())
            }
        }

        create("data") {
            taskName("runData")
            workingDirectory(file("run"))
            forceExit(false)

            args("--mod", modId)
            args("--existing", forgeMain.file("src/main/resources"))
            args("--all")
            args("--output", forgeMain.file("src/generated/resources"))
        }
    }
}

tasks.compileJava {
    options.encoding = "UTF-8";
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
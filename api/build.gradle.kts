val versionMain: String = System.getenv("API_VERSION") ?: "9.9.9"

plugins {
    id("java-library")
    id("maven-publish")
    id("cm-module-conventions")
    // moddev applied directly (not only via cm-module-conventions' afterEvaluate
    // hook) so the neoForge { interfaceInjectionData {…} } extension is available
    // at script-evaluation time.
    alias(neoforged.plugins.moddev)
}

base {
    archivesName = "api"
    group = "dev.compactmods.compactmachines"
    version = versionMain
}

// The public API, split into isolated source sets by feature. Each is compiled
// on its own (compile<Name>Java) to enforce boundaries, then srcDir-folded into
// `main` so the module still ships as a single `api` jar.
//   core ← dimension, machines        core ← rooms ← {roomUpgrades, shrinking}
//   dimension ← rooms
val coreSrc = sourceSets.register("core")
val dimensionSrc = sourceSets.register("dimension")
val machinesSrc = sourceSets.register("machines")
val roomsSrc = sourceSets.register("rooms")
val roomUpgradesSrc = sourceSets.register("roomUpgrades")
val shrinkingSrc = sourceSets.register("shrinking")

neoForge {
    listOf(coreSrc, dimensionSrc, machinesSrc, roomsSrc, roomUpgradesSrc, shrinkingSrc)
        .forEach { addModdingDependenciesTo(it.get()) }

    // Interface injection: MinecraftServer gains IForwardingAttachmentHolder /
    // IServerCapabilityHolder (both in the `core` source set). `from` feeds this
    // module's compile; `publish` attaches the metadata to the Maven publication
    // so downstream consumers resolve it transitively.
    interfaceInjectionData {
        from(project.file("interfaces.json"))
        publish(project.file("interfaces.json"))
    }
}

sourceSets.main {
    java {
        srcDir(coreSrc.get().java)
        srcDir(dimensionSrc.get().java)
        srcDir(machinesSrc.get().java)
        srcDir(roomsSrc.get().java)
        srcDir(roomUpgradesSrc.get().java)
        srcDir(shrinkingSrc.get().java)
    }
}

dependencies {
    // Cross-source-set compile deps. Each isolated compile only sees the api
    // surfaces it actually uses (main folds everything for the final jar).
    "dimensionCompileOnly"(coreSrc.get().output)
    "machinesCompileOnly"(coreSrc.get().output)
    "roomsCompileOnly"(coreSrc.get().output)
    "roomsCompileOnly"(dimensionSrc.get().output)
    "roomUpgradesCompileOnly"(coreSrc.get().output)
    "roomUpgradesCompileOnly"(roomsSrc.get().output)
    "shrinkingCompileOnly"(coreSrc.get().output)
    "shrinkingCompileOnly"(roomsSrc.get().output)
}

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Automatic-Module-Name" to "compactmachines.api",
                "Specification-Title" to "Compact Machines - API",
                "Implementation-Title" to "Compact Machines - API"
            )
        )
    }
}

val PACKAGES_URL = System.getenv("GH_PKG_URL") ?: "https://maven.pkg.github.com/compactmods/compactmachines"
publishing {
    publications.register<MavenPublication>("api") {
        from(components.getByName("java"))
    }

    repositories {
        maven(PACKAGES_URL) {
            name = "GitHubPackages"
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

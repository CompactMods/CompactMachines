@file:Suppress("SpellCheckingInspection")

import org.slf4j.event.Level
import java.text.SimpleDateFormat
import java.util.*

var envVersion: String = System.getenv("VERSION") ?: "9.9.9"
if (envVersion.startsWith("v"))
    envVersion = envVersion.trimStart('v')

val modId: String = "compactmachines"

val core = project(":api")

plugins {
    java
    id("idea")
    id("eclipse")
    id("maven-publish")
//    alias(neoforged.plugins.moddev)
}

project.evaluationDependsOn(core.path)

base {
    group = "dev.compactmods"
    version = envVersion
}

java {
//    toolchain.vendor.set(JvmVendorSpec.JETBRAINS)
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

// Feature impls absorbed from the former per-feature modules. Each is an
// isolated source set (compile<Name>Java enforces boundaries) folded into the
// mod's `main` jar via srcDir. Intra-project dependency order:
//   rooms ← shrinking ← machines ;  rooms ← roomUpgrades ← storage
val roomsSrc = sourceSets.register("rooms")
val shrinkingSrc = sourceSets.register("shrinking")
val roomUpgradesSrc = sourceSets.register("roomUpgrades")
val storageSrc = sourceSets.register("storage")
val machinesSrc = sourceSets.register("machines")

// Data-generation sources: their own source set, NOT folded into `main` (the
// generators must not ship in the mod jar). Driven by the `data` run below.
val datagenSrc = sourceSets.register("datagen")

// Cross-cutting glue concerns as isolated source sets (api-only leaves), folded
// into `main`. Each is wired to :api below.
val gameruleSrc = sourceSets.register("gamerule")
val villagerSrc = sourceSets.register("villager")
val featureSrc = sourceSets.register("feature")
val i18nSrc = sourceSets.register("i18n")
val dimensionSrc = sourceSets.register("dimension")

listOf(roomsSrc, shrinkingSrc, roomUpgradesSrc, storageSrc, machinesSrc, datagenSrc,
       gameruleSrc, villagerSrc, featureSrc, i18nSrc, dimensionSrc)
    .forEach { neoForge.addModdingDependenciesTo(it.get()) }

configurations.named(datagenSrc.get().implementationConfigurationName) {
    extendsFrom(configurations.implementation.get())
}

sourceSets.main {
    java {
        srcDir("src/main/java")
        srcDir(roomsSrc.get().java)
        srcDir(shrinkingSrc.get().java)
        srcDir(roomUpgradesSrc.get().java)
        srcDir(storageSrc.get().java)
        srcDir(machinesSrc.get().java)
        srcDir(gameruleSrc.get().java)
        srcDir(villagerSrc.get().java)
        srcDir(featureSrc.get().java)
        srcDir(i18nSrc.get().java)
        srcDir(dimensionSrc.get().java)
    }

    resources {
        srcDir("src/main/resources")
        srcDir("src/generated/resources")
    }
}

sourceSets.test {
    java {
        srcDir("src/test/java")
    }

    resources {
        srcDir("src/test/resources")
    }
}

evaluationDependsOn(core.path)

neoForge {
//    version = neoforged.versions.neoforge.get()

    // Consumer-side interface injection. :core declares
    //   neoForge { interfaceInjectionData { from(…); publish(…) } }
    // pointing at core/interfaces.json — that adds the metadata to its own
    // compile classpath AND attaches it to the Maven publication. For
    // *external* consumers that resolve :core through Maven, moddev picks the
    // metadata up automatically. For *in-source* siblings (this module),
    // moddev's auto-propagation across project deps is unreliable, so the
    // consumer has to explicitly read the file too. Without this, MinecraftServer
    // doesn't appear to implement IForwardingAttachmentHolder /
    // IServerCapabilityHolder on this module's compile classpath and call sites
    // like `server.getCapability(...)` fail to resolve.
    interfaceInjectionData {
        from(core.file("interfaces.json"))
    }

    val cmMain = this.mods.create(modId) {
        this.modSourceSets.add(sourceSets.main)

        // Datagen classes are part of this mod at runtime (only on the `data`
        // run's classpath), so FML attributes their @EventBusSubscribers to it.
        this.modSourceSets.add(datagenSrc)

        if(System.getenv().containsKey("CI")) {
            modSourceSets.add(sourceSets.test)
        }
    }

    unitTest {
        enable()
        testedMod = mods.named(modId)
    }

    runs {
        // applies to all the run configs below
        configureEach {
            logLevel.set(Level.DEBUG)
            sourceSet = project.sourceSets.main

            // JetBrains Runtime Hotswap
            if (!System.getenv().containsKey("CI")) {
              jvmArgument("-XX:+AllowEnhancedClassRedefinition")
            }

            systemProperties.put("terminal.ansi", "true")

//            var additional = this.additionalRuntimeClasspathConfiguration
//            additional.dependencies.add(compactmods.feather.get())
//            additional.dependencies.add(libs.jnanoid.get())
        }
        
        register("client") {
            client()
            gameDirectory.set(file("runs/client"))

            // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
            systemProperty("forge.enabledGameTestNamespaces", modId)

            programArguments.addAll("--username", "Nano")
            programArguments.addAll("--width", "1920")
            programArguments.addAll("--height", "1080")
        }

        register("client2") {
            client()
            gameDirectory.set(file("runs/client"))

            // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
            systemProperty("forge.enabledGameTestNamespaces", modId)

            programArguments.addAll("--username", "Nano2")
            programArguments.addAll("--width", "1920")
            programArguments.addAll("--height", "1080")
        }

        register("server") {
            server()
            gameDirectory.set(file("runs/server"))
            programArgument("nogui")
        }

        register("data") {
            clientData()
            gameDirectory.set(file("runs/data"))

            programArguments.addAll("--mod", modId)
            programArguments.addAll("--all")
            programArguments.addAll("--output", file("src/generated/resources").absolutePath)
            programArguments.addAll("--existing", file("src/main/resources").absolutePath)

            loadedMods.add(cmMain)
            sourceSet.set(sourceSets["datagen"])
        }

        register("gameTestServer") {
            type = "gameTestServer"
            gameDirectory.set(file("runs/gametest"))

            systemProperty("neoforge.enabledGameTestNamespaces", modId)
            environment.put("CM_TEST_RESOURCES", file("src/test/resources").path)
            environment.put("CM_GAMETEST_ENABLED", "true")

            loadedMods.add(cmMain)

            sourceSet.set(sourceSets.test)
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral {
        name = "Central"
        content {
            includeGroup("com.aventrix.jnanoid")
        }
    }

    maven("https://maven.pkg.github.com/compactmods/feather") {
        name = "Github PKG - Feather"
        content {
            includeGroup("dev.compactmods")
            includeModule("dev.compactmods", "feather")
        }

        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }

    maven("https://maven.pkg.github.com/compactmods/gander") {
        name = "Github PKG - Gander"
        content { includeGroup("dev.compactmods.gander") }
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }

    maven("https://maven.blamejared.com/") {
        // location of the maven that hosts JEI files since January 2023
        name = "Jared's maven"
    }

    maven("https://www.cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }

    maven("https://modmaven.dev") {
        // location of a maven mirror for JEI files, as a fallback
        name = "ModMaven"
    }

    maven("https://maven.theillusivec4.top/") {
        name = "Illusive Soulworks maven (Curios API)"
        content {
            includeGroup("top.theillusivec4.curios")
        }
    }

    // saps.dev Maven (KubeJS and Rhino)
    maven("https://maven.saps.dev/minecraft") {
        content {
            includeGroup("dev.latvian.mods")
        }
    }
}

dependencies {
    // Core Projects and Libraries
    this {
        implementation(libs.jnanoid)
        testImplementation(libs.jnanoid)
        jarJar(libs.jnanoid)

        implementation(project(":api"))
        jarJar(project(":api"))
    }

    // Isolated compile deps for the absorbed feature source sets.
    "roomsCompileOnly"(project(":api"))
    "roomsCompileOnly"(gameruleSrc.get().output)
    "roomsCompileOnly"(compactmods.feather)
    "roomsCompileOnly"(compactmods.spatial)
    "roomsCompileOnly"(libs.jnanoid)
    "roomsCompileOnly"(i18nSrc.get().output)   // room subcommands use CommandTranslations
    "roomsCompileOnly"(compactmods.bundles.screenlib) // room preview panel

    "shrinkingCompileOnly"(project(":api"))
    "shrinkingCompileOnly"(roomsSrc.get().output)
    "shrinkingCompileOnly"(compactmods.feather)

    "roomUpgradesCompileOnly"(project(":api"))
    "roomUpgradesCompileOnly"(roomsSrc.get().output)
    "roomUpgradesCompileOnly"(storageSrc.get().output)
    "roomUpgradesCompileOnly"(compactmods.spatial)
    "roomUpgradesCompileOnly"(compactmods.bundles.screenlib)

    "storageCompileOnly"(project(":api"))
    "storageCompileOnly"(roomsSrc.get().output)
    "storageCompileOnly"(compactmods.bundles.screenlib) // tunnel config screen

    "machinesCompileOnly"(project(":api"))
    "machinesCompileOnly"(roomsSrc.get().output)
    "machinesCompileOnly"(shrinkingSrc.get().output)
    "machinesCompileOnly"(compactmods.bundles.screenlib) // machine screen

    // Datagen sees the whole mod (main folds every feature source set). It must
    // also inherit main's `implementation` deps (:api, feather, spatial, jnanoid)
    // so they're on the `data` run's runtime classpath, not just at compile time.
    "datagenImplementation"(sourceSets.main.get().output)

    // api-only glue leaves.
    "gameruleCompileOnly"(project(":api"))
    "villagerCompileOnly"(project(":api"))
    "featureCompileOnly"(project(":api"))
    "i18nCompileOnly"(project(":api"))

    // dimension self-registers its BLOCKS; uses gamerule (OOB rules) + shrinking helper.
    "dimensionCompileOnly"(project(":api"))
    "dimensionCompileOnly"(gameruleSrc.get().output)
    "dimensionCompileOnly"(shrinkingSrc.get().output)

    testImplementation(neoforged.testframework)
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")


    // `implementation` (not `compileOnly`) so feather/spatial are on the dev-run
    // runtime classpath. Before the restructure they arrived transitively via
    // :room-system's implementation(feather); now the feature code is folded in
    // here, so this module must put them on the runtime classpath itself.
    // jarJar still bundles them into the production jar.
    implementation(compactmods.feather)
    jarJar(compactmods.feather) { isTransitive = false }

    implementation(compactmods.spatial)
    jarJar(compactmods.spatial) { isTransitive = false }

    // ScreenLib node graph + UI APIs (from mavenLocal). Not jar-in-jar'd: the APIs are only
    // useful alongside the screenlib mod, which provides their implementations at runtime.
    implementation(compactmods.bundles.screenlib)

    // The machine screen is built through screenlib's UiProvider, so dev runs need the mod itself
    runtimeOnly(compactmods.screenlibMod)
    "datagenRuntimeOnly"(compactmods.screenlibMod)

    // Gander
//    implementation(compactmods.bundles.gander)
//    accessTransformers(compactmods.ganderRendering)
//    jarJar(compactmods.bundles.gander)

    // Just Enough Items
    compileOnly(mods.bundles.jei)

    // Curios API
//    compileOnly(libs.curios)

    // KubeJS Support
//    compileOnly(mods.kubeJS)
//    compileOnly(mods.rhino)

    // Jade Support
    compileOnly(mods.jade)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(arrayOf("-Xmaxerrs", "9000"))
}

tasks.withType<Jar> {

    val gitVersion = providers.exec {
        commandLine("git", "rev-parse", "HEAD")
    }.standardOutput.asText.get()

    manifest {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
        attributes(
            mapOf(
                "Implementation-Version" to archiveVersion,
                "Implementation-Timestamp" to now,
                "Minecraft-Version" to mojang.versions.minecraft.get(),
                "NeoForge-Version" to neoforged.versions.neoforge.get(),
                "Main-Commit" to gitVersion.trimEnd()
            )
        )

        from("src/main/resources/META-INF/MANIFEST.MF")
    }
}

tasks.withType<ProcessResources>().configureEach {
    val replaceProperties: Map<String, Any> = mapOf(
        "minecraft_version" to mojang.versions.minecraft.get(),
        "neo_version" to neoforged.versions.neoforge.get(),
        "minecraft_version_range" to mojang.versions.minecraftRange.get(),
        "neo_version_range" to neoforged.versions.neoforgeRange.get(),
        "loader_version_range" to "[1,)",
        "mod_id" to modId,
        "mod_version" to envVersion
    )

    inputs.properties(replaceProperties)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(replaceProperties)
    }
}

val PACKAGES_URL = System.getenv("GH_PKG_URL") ?: "https://maven.pkg.github.com/compactmods/compactmachines"
publishing {
    publications.register<MavenPublication>("compactmachines") {
        artifactId = modId
        from(components.getByName("java"))
    }

    repositories {
        // GitHub Packages
        maven(PACKAGES_URL) {
            name = "GitHubPackages"
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
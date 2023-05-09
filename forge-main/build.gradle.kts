import java.text.SimpleDateFormat
import java.util.*

val modVersion: String = System.getenv("CM_VERSION") ?: "9.9.9"

val coreVersion: String = property("core_version") as String
val tunnelsApiVersion: String = property("tunnels_version") as String

var modId = property("mod_id") as String
var minecraft_version: String by extra
var forge_version: String by extra
var parchment_version: String by extra

plugins {
    id("maven-publish")
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
        srcDir("src/main/resources")
        srcDir("src/generated/resources")
    }
}

sourceSets.test {
    java.srcDir("src/test/java")
    resources.srcDir("src/test/resources")
}


repositories {
    mavenLocal()

    mavenCentral() {
        name = "Central"
        content {
            includeGroup("com.aventrix.jnanoid")
        }
    }

    maven("https://www.cursemaven.com") {
        name = "Curse Maven"
        content {
            includeGroup("curse.maven")
        }
    }

    // location of the maven that hosts JEI files
    maven("https://maven.blamejared.com") {
        content {
            includeGroup("mezz.jei")
        }
    }

    maven("https://maven.theillusivec4.top/") {
        name = "Illusive"
        content {
            includeGroup("top.theillusivec4.curios")
        }
    }

    maven("https://maven.pkg.github.com/compactmods/compactmachines-core") {
        name = "Github PKG Core"
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

val jei_version: String? by extra
val jei_mc_version: String by extra
val curios_version: String? by extra

val runDepends: List<Project> = listOf(
        project(":forge-tunnels-api"),
        project(":forge-builtin")
)

runDepends.forEach {
    project.evaluationDependsOn(it.path)
}

dependencies {
    minecraft("net.minecraftforge", "forge", version = "${minecraft_version}-${forge_version}")

    implementation("dev.compactmods.compactmachines:core-api:$coreVersion")
    implementation("dev.compactmods.compactmachines:core:$coreVersion")
    implementation("dev.compactmods.compactmachines:tunnels-api:$tunnelsApiVersion")

    jarJar("dev.compactmods.compactmachines", "core", "[$coreVersion]", classifier = "srg") {
        isTransitive = false
    }

    jarJar("dev.compactmods.compactmachines", "core-api", "[$coreVersion]", classifier = "srg") {
        isTransitive = false
    }

    jarJar("dev.compactmods.compactmachines", "tunnels-api", "[$tunnelsApiVersion]", classifier = "srg") {
        isTransitive = false
    }

    implementation(project(":forge-tunnels-api"))
    testImplementation(project(":forge-tunnels-api"))

    implementation(project(":forge-builtin"))
    testImplementation(project(":forge-builtin"))

    minecraftLibrary("com.aventrix.jnanoid", "jnanoid", "2.0.0")
    jarJar("com.aventrix.jnanoid", "jnanoid", "[2.0.0]")

    // JEI
    if (project.extra.has("jei_version") && project.extra.has("jei_mc_version")) {
        compileOnly(fg.deobf("mezz.jei:jei-${jei_mc_version}-common-api:${jei_version}"))
        compileOnly(fg.deobf("mezz.jei:jei-${jei_mc_version}-forge-api:${jei_version}"))
        runtimeOnly(fg.deobf("mezz.jei:jei-${jei_mc_version}-forge:${jei_version}"))
    }

    // The One Probe
    implementation(fg.deobf("curse.maven:theoneprobe-245211:3927520"))

    // Curios
    if (project.extra.has("curios_version")) {
        runtimeOnly(fg.deobf("top.theillusivec4.curios:curios-forge:${curios_version}"))
        compileOnly(fg.deobf("top.theillusivec4.curios:curios-forge:${curios_version}:api"))
    }

    val include_test_mods: String? by project.extra
    if (!System.getenv().containsKey("CI") && include_test_mods.equals("true")) {
        // Nicephore - Screenshots and Stuff
        runtimeOnly(fg.deobf("curse.maven:nicephore-401014:3879841"))

        // Testing Mods - Trash Cans, Pipez, Create, Refined Pipes, Pretty Pipes, Refined Storage
        runtimeOnly(fg.deobf("curse.maven:SuperMartijn642-454372:3910759"))
        runtimeOnly(fg.deobf("curse.maven:trashcans-394535:3871885"))

        // Flywheel/Create - v0.6.8.a / v0.5.0i - Jan 29, 2023
        runtimeOnly(fg.deobf("curse.maven:flywheel-486392:4341471"))
        runtimeOnly(fg.deobf("curse.maven:create-328085:4371809"))

        // 1.18 runtimeOnly(fg.deobf("curse.maven:refinedpipes-370696:3570151"))

        // Pretty Pipes - 1.13.6 - Oct 25, 2022
        runtimeOnly(fg.deobf("curse.maven:prettypipes-376737:4049655"))

        // Refined Storage - 1.11.6 - Mar 30, 2023
        runtimeOnly(fg.deobf("curse.maven:refinedstorage-243076:4465872"))

        // Scalable Cat's Force, BdLib, Advanced Generators
        // 2.13.10-b10 - Oct 13, 2022 / 1.25.0.5 - Nov 20, 2022 / 1.4.0.5 - Nov 22, 2022
        runtimeOnly(fg.deobf("curse.maven:scalable-320926:4028119"))
        runtimeOnly(fg.deobf("curse.maven:bdlib-70496:4100704"))
        runtimeOnly(fg.deobf("curse.maven:advgen-223622:4104739"))

        // Immersive Eng - 7.1.0-145 (Dec 31)
        // runtimeOnly(fg.deobf("curse.maven:immersiveeng-231951:3587149"))

        // FTB Chunks
//        runtimeOnly(fg.deobf("curse.maven:architectury-forge-419699:3781711"))
//        runtimeOnly(fg.deobf("curse.maven:ftb-teams-404468:3725501"))
//        runtimeOnly(fg.deobf("curse.maven:ftblib-404465:3725485"))
//        runtimeOnly(fg.deobf("curse.maven:ftbchunks-314906:3780113"))

        // Mekanism + Mek Generators - Tunnel testing
        // 10.3.8.477 - Feb 7, 2023 / 10.3.8.477 - Feb 7, 2023
        runtimeOnly(fg.deobf("curse.maven:mekanism-268560:4385637"))
        runtimeOnly(fg.deobf("curse.maven:mekanismgenerators-268566:4385639"))

        // Soul Shards (FTB)
        // runtimeOnly(fg.deobf("curse.maven:polylib-576589:3751528"))
        // runtimeOnly(fg.deobf("curse.maven:soulshards-551523:3757202"))

        // Everlasting Abilities
        // runtimeOnly(fg.deobf("curse.maven:cyclopscore-232758:3809427"))
        // runtimeOnly(fg.deobf("curse.maven:everlastabilities-248353:3768481"))
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

            ideaModule("Compact_Machines.forge-main.main")

            if (!System.getenv().containsKey("CI")) {
                // JetBrains Runtime Hotswap
                jvmArg("-XX:+AllowEnhancedClassRedefinition")
            }

            source(sourceSets.main.get())
            mods.create(modId) {
                source(sourceSets.main.get())
                for (p in runDepends)
                    source(p.sourceSets.main.get())
            }
        }

        create("client") {
            taskName("runClient-Nano")
            workingDirectory(file("run/client"))

            args("--username", "Nano")
            args("--width", 1920)
            args("--height", 1080)
        }

        create("gameTestServer") {
            taskName("runGameTestServer")
            workingDirectory(file("run/gametests"))
            ideaModule("Compact_Machines.forge-main.test")

            forceExit(false)
            environment("CM5_TEST_RESOURCES", file("src/test/resources"))

            mods.named(modId) {
                source(sourceSets.test.get())
            }
        }
    }
}

reobf {
    this.create("jarJar")
}

tasks.compileJava {
    options.encoding = "UTF-8";
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Jar> {
    val forgeBuiltin = project(":forge-builtin").tasks.jar.get().archiveFile;
    val forgeTunnelsApi = project(":forge-builtin").tasks.jar.get().archiveFile;
    from(forgeBuiltin.map { zipTree(it) })
    from(forgeTunnelsApi.map { zipTree(it) })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
        attributes(mapOf(
                "Specification-Title" to "Compact Machines",
                "Specification-Vendor" to "",
                "Specification-Version" to "1", // We are version 1 of ourselves
                "Implementation-Title" to "Compact Machines",
                "Implementation-Version" to archiveVersion,
                "Implementation-Vendor" to "",
                "Implementation-Timestamp" to now
        ))
    }
}

tasks.jar {
    archiveClassifier.set("slim")
    finalizedBy("reobfJar")
}

jarJar.enable()
tasks.jarJar {
    archiveClassifier.set("")
    finalizedBy("reobfJarJar")
}

tasks.reobfJarJar {
//    doFirst {
//        println("Reobfuscating JarJar")
//        this.inputs.files.forEach {
//            println(it.path)
//        }
//    }
}

artifacts {
    archives(tasks.jar.get())
    archives(tasks.jarJar.get())
}

val PACKAGES_URL = System.getenv("GH_PKG_URL") ?: "https://maven.pkg.github.com/compactmods/compactcrafting"
publishing {
    publications.register<MavenPublication>("forge") {
        artifactId = "$modId-forge"
        artifact(tasks.getByName("jar"))
        artifact(tasks.getByName("jarJar"))
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
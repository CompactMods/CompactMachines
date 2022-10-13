import java.text.SimpleDateFormat
import java.util.*

val semver: String = System.getenv("CM_SEMVER_VERSION") ?: "9.9.9"
val buildNumber: String = System.getenv("CM_BUILD_NUM") ?: "0"

val nightlyVersion: String = "${semver}+nightly-b${buildNumber}"
val isRelease: Boolean = (System.getenv("CM_RELEASE") ?: "false").equals("true", true)

var mod_id: String by extra
var minecraft_version: String by extra
var forge_version: String by extra
var parchment_version: String by extra

tasks.create("getBuildInfo") {
    println("Mod ID: ${mod_id}")
    println("Version: ${version}")
    println("Semver Version: ${semver}")
    println("Nightly Build: ${nightlyVersion}")
}

plugins {
    id("idea")
    id("eclipse")
    id("maven-publish")
    id("net.minecraftforge.gradle") version ("5.1.+")
    id("org.parchmentmc.librarian.forgegradle") version ("1.+")
}

base {
    archivesName.set(mod_id)
    group = "dev.compactmods"
    version = if(isRelease) semver else nightlyVersion
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

project.evaluationDependsOn(project(":forge-api").path)

repositories {
    mavenLocal()
    mavenCentral() {
        content {
            includeGroup("com.aventrix.jnanoid")
        }
    }

    maven("https://www.cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }

    // location of the maven that hosts JEI files
    maven("https://dvs1.progwml6.com/files/maven") {
        content {
            includeGroup("mezz.jei")
        }
    }

    maven("https://maven.theillusivec4.top/") {
        content {
            includeGroup("top.theillusivec4.curios")
        }
    }
}

val jei_version: String? by extra
val jei_mc_version: String by extra
val curios_version: String? by extra

jarJar.enable()

dependencies {
    minecraft("net.minecraftforge", "forge", version = "${minecraft_version}-${forge_version}")

    implementation(project(":forge-api"))
    testImplementation(project(":forge-api"))

    implementation(project(":forge-tunnels"))
    testImplementation(project(":forge-tunnels"))

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

    minecraftLibrary("com.aventrix.jnanoid", "jnanoid", "2.0.0")
    jarJar("com.aventrix.jnanoid", "jnanoid", "[2.0.0]")

    val include_test_mods: String? by extra
    if (!System.getenv().containsKey("CI") && include_test_mods.equals("true")) {
        // Nicephore - Screenshots and Stuff
        runtimeOnly(fg.deobf("curse.maven:nicephore-401014:3879841"))

        // Testing Mods - Trash Cans, Pipez, Create, Refined Pipes, Pretty Pipes, Refined Storage
        runtimeOnly(fg.deobf("curse.maven:SuperMartijn642-454372:3910759"))
        runtimeOnly(fg.deobf("curse.maven:trashcans-394535:3871885"))

        // runtimeOnly(fg.deobf("curse.maven:flywheel-486392:3871082"))
        // runtimeOnly(fg.deobf("curse.maven:create-328085:3737418"))

        // runtimeOnly(fg.deobf("curse.maven:refinedpipes-370696:3570151"))
        // runtimeOnly(fg.deobf("curse.maven:prettypipes-376737:3573145"))
        // runtimeOnly(fg.deobf("curse.maven:refinedstorage-243076:3623324"))

        // Scalable Cat's Force, BdLib, Advanced Generators
        // runtimeOnly(fg.deobf("curse.maven:scalable-320926:3634756"))
        // runtimeOnly(fg.deobf("curse.maven:bdlib-70496:3663149"))
        // runtimeOnly(fg.deobf("curse.maven:advgen-223622:3665335"))

        // Immersive Eng - 7.1.0-145 (Dec 31)
        // runtimeOnly(fg.deobf("curse.maven:immersiveeng-231951:3587149"))

        // FTB Chunks
        // runtimeOnly(fg.deobf("curse.maven:architectury-forge-419699:3781711"))
        // runtimeOnly(fg.deobf("curse.maven:ftb-teams-404468:3725501"))
        // runtimeOnly(fg.deobf("curse.maven:ftblib-404465:3725485"))
        // runtimeOnly(fg.deobf("curse.maven:ftbchunks-314906:3780113"))

        // Mekanism + Mek Generators - Tunnel testing
        runtimeOnly(fg.deobf("curse.maven:mekanism-268560:3922056"))
        runtimeOnly(fg.deobf("curse.maven:mekanismgenerators-268566:3922058"))

        // Soul Shards (FTB)
        // runtimeOnly(fg.deobf("curse.maven:polylib-576589:3751528"))
        // runtimeOnly(fg.deobf("curse.maven:soulshards-551523:3757202"))

        // Everlasting Abilities
        // runtimeOnly(fg.deobf("curse.maven:cyclopscore-232758:3809427"))
        // runtimeOnly(fg.deobf("curse.maven:everlastabilities-248353:3768481"))
    }
}

val runDepends: List<Project> = listOf(
        project(":forge-api"),
        project(":forge-tunnels")
)

runDepends.forEach {
    project.evaluationDependsOn(it.path)
}

minecraft {
    mappings("parchment", parchment_version)
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
                jvmArg("-XX:HotswapAgent=fatjar")
            }

            source(sourceSets.main.get())
            mods.create(mod_id) {
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

        create("data") {
            taskName("runData")
            workingDirectory(file("run/data"))
            forceExit(false)

            args("--mod", mod_id)
            args("--existing", file("src/main/resources"))
            args("--all")
            args("--output", file("src/generated/resources/"))
        }

        create("gameTestServer") {
            taskName("runGameTestServer")
            workingDirectory(file("run/gametests"))
            ideaModule("Compact_Machines.forge-main.test")

            forceExit(false)
            environment("CM5_TEST_RESOURCES", file("src/test/resources"))

            mods.named(mod_id) {
                source(sourceSets.test.get())
            }
        }
    }
}

reobf {
    jarJar { }
}

tasks.compileJava {
    options.encoding = "UTF-8";
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Jar> {
    // Remove datagen source and cache info
    this.exclude("dev/compactmods/machines/datagen/**")
    this.exclude(".cache/**")

    // TODO - Switch to API jar when JarInJar supports it better
    val api = project(":forge-api").tasks.jar.get().archiveFile;
    from(api.map { zipTree(it) })

    val tunnels = project(":forge-tunnels").tasks.jar.get().archiveFile;
    from(tunnels.map { zipTree(it) })

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
    finalizedBy("reobfJar")
    archiveClassifier.set("slim")
}

tasks.jarJar {
    archiveClassifier.set("")
}

artifacts {
    archives(tasks.jar.get())
    archives(tasks.jarJar.get())
}

publishing {
    publications.register<MavenPublication>("releaseMain") {
        artifactId = mod_id
        groupId = "dev.compactmods"

        artifacts {
            artifact(tasks.jar.get())
            artifact(tasks.jarJar.get())
        }
    }

    repositories {
        // GitHub Packages
        maven("https://maven.pkg.github.com/CompactMods/CompactMachines") {
            name = "GitHubPackages"
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
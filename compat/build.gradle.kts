val versionMain: String = System.getenv("COMPAT_VERSION") ?: "9.9.9"

plugins {
    id("cm-module-conventions")
}

base {
    archivesName = "compat"
    group = "dev.compactmods.compactmachines"
    version = versionMain
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(mapOf("Automatic-Module-Name" to "compactmachines.compat"))
    }
}

// One isolated source set per integrated mod, folded into `main`.
val jeiSrc = sourceSets.register("jei")
val jadeSrc = sourceSets.register("jade")
val curiosSrc = sourceSets.register("curios")
val theoneprobeSrc = sourceSets.register("theoneprobe")

listOf(jeiSrc, jadeSrc, curiosSrc, theoneprobeSrc)
    .forEach { neoForge.addModdingDependenciesTo(it.get()) }

neoForge.interfaceInjectionData.from(project(":api").file("interfaces.json"))

sourceSets.main {
    java {
        srcDir(jeiSrc.get().java)
        srcDir(jadeSrc.get().java)
        srcDir(curiosSrc.get().java)
        srcDir(theoneprobeSrc.get().java)
    }
}

repositories {
    maven("https://maven.blamejared.com/") { name = "Jared's maven" }        // JEI
    maven("https://modmaven.dev") { name = "ModMaven" }                       // JEI mirror
    maven("https://www.cursemaven.com") { content { includeGroup("curse.maven") } } // Jade / TOP
    maven("https://maven.theillusivec4.top/") {                               // Curios
        name = "Illusive Soulworks maven (Curios API)"
        content { includeGroup("top.theillusivec4.curios") }
    }
}

dependencies {
    // Feeds the folded `main` compile.
    compileOnly(project(":api"))
    compileOnly(project(":neoforge"))

    // Per-source-set isolation: each integration sees :api, the mod impl, and
    // its own third-party API. (:neoforge does NOT depend back on :compat, so
    // no project cycle; bundling into the mod jar is wired when a plugin is
    // reactivated — all four are currently dormant stubs.)
    listOf("jei", "jade", "curios", "theoneprobe").forEach {
        add("${it}CompileOnly", project(":api"))
        add("${it}CompileOnly", project(":neoforge"))
    }

    "jeiCompileOnly"(mods.bundles.jei)
    "jadeCompileOnly"(mods.jade)
}

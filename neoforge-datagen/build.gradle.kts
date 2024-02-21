plugins {
    id("java")
    id("eclipse")
    id("idea")
    id("maven-publish")
    id("net.neoforged.gradle.userdev") version ("7.0.93")
}

val mod_id: String by extra
val mainProject: Project = project(":neoforge-main")
evaluationDependsOn(mainProject.path)

val core = project(":core:core")
val coreApi = project(":core:core-api")
val roomApi = project(":core:room-api")
val roomUpgradeApi = project(":core:room-upgrade-api")

val coreProjects = listOf(core, coreApi, roomApi, roomUpgradeApi)

base {
    group = "dev.compactmods.compactmachines"
    archivesName.set(mod_id)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

minecraft {
    modIdentifier.set(mod_id)
}

runs {
    // applies to all the run configs below
    configureEach {
        // Recommended logging data for a userdev environment
        systemProperty("forge.logging.markers", "") // 'SCAN,REGISTRIES,REGISTRYDUMP'

        // Recommended logging level for the console
        systemProperty("forge.logging.console.level", "debug")

        modSource(project.sourceSets.main.get())
        modSource(mainProject.sourceSets.main.get())

        coreProjects.forEach { modSource(it.sourceSets.main.get()) }
    }

    create("data") {
        dataGenerator(true)

        programArguments("--mod", "compactmachines")
        programArguments("--all")
        programArguments("--output", mainProject.file("src/generated/resources").absolutePath)
        programArguments("--existing", mainProject.file("src/main/resources").absolutePath)
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

dependencies {
    implementation(libraries.neoforge.get())
    compileOnly(mainProject)
    coreProjects.forEach {
        compileOnly(it)
    }
}

tasks.compileJava {
    options.encoding = "UTF-8";
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
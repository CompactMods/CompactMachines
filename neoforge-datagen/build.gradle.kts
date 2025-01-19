plugins {
    id("java")
    id("eclipse")
    id("idea")
    id("maven-publish")
    alias(neoforged.plugins.moddev)
}

val modId: String = "compactmachines"

val coreApi = project(":core-api")
val mainProject: Project = project(":neoforge-main")

project.evaluationDependsOn(coreApi.path)
project.evaluationDependsOn(mainProject.path)

java {
    toolchain.vendor.set(JvmVendorSpec.JETBRAINS)
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

neoForge {
    version = neoforged.versions.neoforge

    mods.create(modId) {
        this.sourceSet(coreApi.sourceSets.main.get())
        this.sourceSet(sourceSets.main.get())
        this.sourceSet(mainProject.sourceSets.main.get())
    }

    runs {
        this.create("data") {
            this.data()

            this.gameDirectory.set(file("runs/data"))

            // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
            systemProperty("forge.enabledGameTestNamespaces", modId)

            programArguments.addAll("--mod", modId)
            programArguments.addAll("--all")
            programArguments.addAll("--output", mainProject.file("src/generated/resources").absolutePath)
            programArguments.addAll("--existing", mainProject.file("src/main/resources").absolutePath)
        }
    }
}

repositories {
    mavenLocal()

    maven("https://maven.pkg.github.com/compactmods/feather") {
        name = "Github PKG Core"
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }

    maven("https://maven.theillusivec4.top/") {
        name = "Illusive Soulworks maven (Curios API)"
        content {
            includeGroup("top.theillusivec4.curios")
        }
    }
}

dependencies {
    compileOnly(coreApi)
    compileOnly(mainProject)

    implementation(libs.curios)
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
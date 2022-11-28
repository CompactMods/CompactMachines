val semver: String = System.getenv("CM_SEMVER_VERSION") ?: "9.9.9"
val buildNumber: String = System.getenv("CM_BUILD_NUM") ?: "0"
val nightlyVersion: String = "${semver}.${buildNumber}-nightly"
val isRelease: Boolean = (System.getenv("CM_RELEASE") ?: "false").equals("true", true)

var mod_id: String by extra

repositories {
    mavenCentral() {
        content {
            includeGroup("com.aventrix.jnanoid")
        }
    }

    maven("https://maven.parchmentmc.org") {
        name = "ParchmentMC"
    }
}

plugins {
    id("fabric-loom")
    id("maven-publish")
}

base {
    archivesName.set(mod_id)
    group = "dev.compactmods"
    version = if(isRelease) semver else nightlyVersion
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

sourceSets {
    named("main") {
        resources {
            //The API has no resources
            setSrcDirs(emptyList<String>())
        }
    }

    named("test") {
        resources {
            //The test module has no resources
            setSrcDirs(emptyList<String>())
        }
    }
}

val minecraft_version: String by rootProject.extra
val parchment_version: String by rootProject.extra

val runDepends: List<Project> = listOf(
        project(":common-api")
)

runDepends.forEach {
    project.evaluationDependsOn(it.path)
}

dependencies {
    minecraft("net.minecraft", "minecraft", minecraft_version)

    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${minecraft_version}:${parchment_version}@zip")
    })

    implementation(project(":common-api"))
    compileOnly("com.aventrix.jnanoid", "jnanoid", "2.0.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-proc:none")
}

publishing {
    publications.register<MavenPublication>("common") {
        artifactId = "compactmachines"
        groupId = "dev.compactmods"

        artifacts {
            artifact(tasks.jar.get())
            // artifact(tasks.named("sourcesJar").get())
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
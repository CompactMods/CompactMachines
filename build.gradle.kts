// no-op
plugins {
    id("java")
    id("maven-publish")
}

val mod_id: String by extra
val semver: String = System.getenv("CM_SEMVER_VERSION") ?: "9.9.9"
val buildNumber: String = System.getenv("CM_BUILD_NUM") ?: "0"
val nightlyVersion: String = "${semver}.${buildNumber}-nightly"
val isRelease: Boolean = (System.getenv("CM_RELEASE") ?: "false").equals("true", true)
val aVersion = if(isRelease) semver else nightlyVersion

project.evaluationDependsOn(project(":forge-api").path)
project.evaluationDependsOn(project(":forge-main").path)

publishing {
    publications.register<MavenPublication>("allForge") {
        artifactId = mod_id
        groupId = "dev.compactmods"
        version = aVersion

        this.artifact(project(":forge-api").tasks.jar.get())
        this.artifact(project(":forge-api").tasks.named("sourcesJar").get())
        this.artifact(project(":forge-main").tasks.jar.get())
        this.artifact(project(":forge-main").tasks.named("jarJar").get())
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
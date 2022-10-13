var semver: String = System.getenv("CM_SEMVER_VERSION") ?: "9.9.9"
var buildNumber: String = System.getenv("CM_BUILD_NUM") ?: "0"

var nightlyVersion: String = "${semver}+nightly-b${buildNumber}"
var isRelease: Boolean = (System.getenv("CM_RELEASE") ?: "false").equals("true", true)

var mod_id: String by extra

version = if(isRelease) semver else nightlyVersion
group = "dev.compactmods"

tasks.create("getBuildInfo") {
    println("Mod ID: ${mod_id}")
    println("Version: ${version}")
    println("Semver Version: ${semver}")
    println("Nightly Build: ${nightlyVersion}")
}


//println('Java: ' + System.getProperty('java.version') + ' JVM: ' + System.getProperty('java.vm.version') + '(' + System.getProperty('java.vendor') + ') Arch: ' + System.getProperty('os.arch'))
//minecraft {
//    mappings channel: 'parchment', version: parchment_version
//    // makeObfSourceJar = false // an Srg named sources jar is made by default. uncomment this to disable.
//
//
//
//    // Default run configurations.
//    // These can be tweaked, removed, or duplicated as needed.
//    runs {
//        all {
//            // Recommended logging data for a userdev environment
//            property 'forge.logging.markers', '' // 'SCAN,REGISTRIES,REGISTRYDUMP'
//
//            // Recommended logging level for the console
//            property 'forge.logging.console.level', 'debug'
//
//            property 'mixin.env.remapRefMap', 'true'
//            property 'mixin.env.refMapRemappingFile', "${buildDir}/createSrgToMcp/output.srg"
//
//            if (!System.getenv().containsKey("CI")) {
//                // JetBrains Runtime Hotswap
//                jvmArg '-XX:+AllowEnhancedClassRedefinition'
//                jvmArg '-XX:HotswapAgent=fatjar'
//            } else {
//                forceExit false
//            }
//        }
//
//        client {
//            workingDirectory project.file('run/client')
//
//            args '--username', 'Nano'
//            args '--width', 1920
//            args '--height', 1080
//
//
//            source sourceSets.api
//            mods {
//                compactmachines {
//                    source sourceSets.tunnels
//                    source sourceSets.main
//                }
//            }
//        }
//    }
//}

////
////task apiJar(type: Jar) {
////    from sourceSets.api.output
////    // Sources included because of MinecraftForge/ForgeGradle#369
////    classifier 'api'
////}
//
//artifacts {
//    archives jar // , apiJar
//}
//
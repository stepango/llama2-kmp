plugins {
    kotlin("multiplatform")
}

val arguments = providers.gradleProperty("runArgs")

kotlin {
    jvm()
    mingwX64 {
        binaries {
            executable()
        }
    }
    js {
        nodejs {
            binaries.executable()
            runTask {
                args(arguments.orNull?.split(" ") ?: emptyList<String>())
            }
        }
    }
    sourceSets{
        val commonMain by getting {
            dependencies {
                implementation("com.squareup.okio:okio:3.6.0")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("com.squareup.okio:okio-nodefilesystem:3.6.0")
            }
        }
    }
}



val configureExec: (Exec).() -> Unit = {
    argumentProviders.add(CommandLineArgumentProvider {
        arguments.orNull?.split(" ") ?: emptyList()
    })
}
tasks.named<Exec>("runDebugExecutableMingwX64").configure(configureExec)
tasks.named<Exec>("runReleaseExecutableMingwX64").configure(configureExec)
//tasks.named<Exec>("jsNodeRun").configure(configureExec)

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    mingwX64 {
        binaries {
            executable()
        }
    }
    sourceSets{
        val commonMain by getting {
            dependencies {
                implementation("com.squareup.okio:okio:3.6.0")
            }
        }
    }
}

val configureExec: (Exec).() -> Unit = {
    val arguments = providers.gradleProperty("runArgs")
    argumentProviders.add(CommandLineArgumentProvider {
        arguments.orNull?.split(" ") ?: emptyList()
    })
}
tasks.named<Exec>("runDebugExecutableMingwX64").configure(configureExec)
tasks.named<Exec>("runReleaseExecutableMingwX64").configure(configureExec)

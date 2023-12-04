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
    macosX64 {
        binaries {
            executable()
        }
    }
    linuxX64 {
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

val variants = listOf(
    "Debug",
    "Release"
)

val nativePlatforms = listOf(
    "MingwX64",
    "MacosX64",
    "LinuxX64",
)

variants.forEach { variant ->
    nativePlatforms.forEach { platform ->
        tasks.named<Exec>("run${variant}Executable${platform}").configure(configureExec)
    }
}

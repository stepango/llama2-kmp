import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask

plugins {
    kotlin("multiplatform")
}

val arguments: Provider<String> = providers.gradleProperty("runArgs")

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

    // Still broken
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        nodejs {
            runTask {
                args(arguments.orNull?.split(" ") ?: emptyList<String>())
            }
        }
    }

    // Still broken
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        binaries.executable()
        nodejs()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.squareup.okio:okio:3.9.0")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("com.squareup.okio:okio-nodefilesystem:3.9.0")
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation("com.squareup.okio:okio-nodefilesystem:3.9.0")
            }
        }
    }
}

rootProject.the<NodeJsRootExtension>().apply {
    version = "21.0.0-v8-canary20231019bd785be450"
    downloadBaseUrl = "https://nodejs.org/download/v8-canary"
}

tasks.withType<NodeJsExec>().configureEach {
    args?.add("--experimental-wasm-gc")
}

val configureExec: (Exec).() -> Unit = {
    argumentProviders.add(CommandLineArgumentProvider {
        arguments.orNull?.split(" ") ?: emptyList()
    })
}

import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockStoreTask

plugins {
    kotlin("multiplatform") version "2.0.0-Beta5" apply false
}

tasks.withType<KotlinNpmInstallTask>().configureEach {
    val flag = "--ignore-engines"

    if (!args.contains(flag)) {
        args.add(flag)
    }
}

repositories {
    mavenCentral()
}

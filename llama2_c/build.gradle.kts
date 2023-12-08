import org.jetbrains.kotlin.cli.common.isWindows

plugins {
    `cpp-application`
    `visual-studio`
}

application {
    binaries.configureEach {
        val compileTask = compileTask.get()

        compileTask.source.from(fileTree("src/main/c"))

        @Suppress("UnstableApiUsage")
        if (toolChain is GccCompatibleToolChain) {
            compileTask.compilerArgs.addAll("-Ofast", "-x", "c", "-std=c11")
            if (isWindows) {
                compileTask.compilerArgs.addAll("-D", "WIN32")
            }
        }
    }
}

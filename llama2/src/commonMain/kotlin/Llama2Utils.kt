import okio.FileSystem
import okio.Path.Companion.toPath

class Llama2Utils {
    companion object {
        fun buildLlama2(
            fileSystem: FileSystem,
            checkpoint: String,
            rootDir: String = "../"
        ): Llama2 {
            val (config, weights) = fileSystem.read("$rootDir/$checkpoint".toPath()) {
                val config = ConfigUtil.from(this)
                config to WeightsUtil.from(config, this)
            }
            val state = RunState(config)
            return Llama2(config, weights, state)
        }
    }
}
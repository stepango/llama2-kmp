import okio.FileSystem
import okio.Path.Companion.toPath

class Llama2Utils {
    companion object {
        fun buildLlama2(checkpoint: String): Llama2 {
            val (config, weights) = FileSystem.SYSTEM.read("../$checkpoint".toPath()) {
                val config = ConfigUtil.from(this)
                config to WeightsUtil.from(config, this)
            }
            val state = RunState(config)
            return Llama2(config, weights, state)
        }
    }
}
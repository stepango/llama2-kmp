// Run State class
class RunState(config: Config) {
    val x = FloatArray(config.dim)
    val xb = FloatArray(config.dim)
    val xb2 = FloatArray(config.dim)
    val hb = FloatArray(config.hiddenDim)
    val hb2 = FloatArray(config.hiddenDim)
    val q = FloatArray(config.dim)
    val k = FloatArray(config.dim)
    val v = FloatArray(config.dim)
    val att = FloatArray(config.nHeads * config.seqLen)
    val logits = FloatArray(config.vocabSize)
    val keyCache = FloatArray(config.nLayers * config.seqLen * config.dim)
    val valueCache = FloatArray(config.nLayers * config.seqLen * config.dim)

    private val states = listOf(x, xb, xb2, hb, hb2, q, k, v, att, logits, keyCache, valueCache)

    init {
        // Ensure all memory allocations went fine
        if (states.any { it.isEmpty() }) {
            throw IllegalStateException("Memory allocation failed!")
        }
    }
}
import okio.BufferedSource

class WeightsUtil {
    companion object {
        fun from(config: Config, buffer: BufferedSource): Weights {
            val tokenEmbeddingTable = buffer.readFloatLeArray(config.vocabSize * config.dim)
            return Weights(
                tokenEmbeddingTable = tokenEmbeddingTable,
                rmsAttWeight = buffer.readFloatLeArray(config.nLayers * config.dim),
                wq = buffer.readFloatLeArray(config.nLayers * config.dim * config.dim),
                wk = buffer.readFloatLeArray(config.nLayers * config.dim * config.dim),
                wv = buffer.readFloatLeArray(config.nLayers * config.dim * config.dim),
                wo = buffer.readFloatLeArray(config.nLayers * config.dim * config.dim),
                rmsFfnWeight = buffer.readFloatLeArray(config.nLayers * config.dim),
                w1 = buffer.readFloatLeArray(config.nLayers * config.hiddenDim * config.dim),
                w2 = buffer.readFloatLeArray(config.nLayers * config.dim * config.hiddenDim),
                w3 = buffer.readFloatLeArray(config.nLayers * config.hiddenDim * config.dim),
                rmsFinalWeight = buffer.readFloatLeArray(config.dim),
                freqCisReal = buffer.readFloatLeArray(config.seqLen * config.headSize / 2),
                freqCisImag = buffer.readFloatLeArray(config.seqLen * config.headSize / 2),
                wcls = if (config.sharedWeights) tokenEmbeddingTable else null
            )
        }
    }
}
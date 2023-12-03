// Transformer Configuration class
class Config(
    val dim: Int, // transformer dimension
    val hiddenDim: Int, // for ffn layers
    val nLayers: Int, // number of layers
    val nHeads: Int, // number of query heads
    val nKvHeads: Int, // number of key/value heads (can be < query heads because of multiquery)
    val vocabSize: Int, // vocabulary size, usually 256 (byte-level)
    val seqLen: Int, // max sequence length
) {
    val sharedWeights: Boolean = vocabSize > 0
    val headSize: Int = dim / nHeads
}
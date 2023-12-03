

class Weights(
    val tokenEmbeddingTable: FloatArray, // (vocab_size, dim)
    val rmsAttWeight: FloatArray, // (layer, dim) rmsnorm weights
    val rmsFfnWeight: FloatArray, // (layer, dim)
    val wq: FloatArray, // (layer, dim, dim)
    val wk: FloatArray, // (layer, dim, dim)
    val wv: FloatArray, // (layer, dim, dim)
    val wo: FloatArray, // (layer, dim, dim)
    val w1: FloatArray, // (layer, hidden_dim, dim)
    val w2: FloatArray, // (layer, dim, hidden_dim)
    val w3: FloatArray, // (layer, hidden_dim, dim)
    val rmsFinalWeight: FloatArray, // (dim,)
    val freqCisReal: FloatArray, // (seq_len, dim/2)
    val freqCisImag: FloatArray, // (seq_len, dim/2)
    val wcls: FloatArray?, // (optional) classifier weights for the logits, on the last layer
)
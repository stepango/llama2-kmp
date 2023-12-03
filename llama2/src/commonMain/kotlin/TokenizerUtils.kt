import okio.FileSystem
import okio.Path.Companion.toPath

class TokenizerUtils {
    companion object {
        fun buildTokenizer(
            fileSystem: FileSystem,
            vocabSize: Int, path: String = "tokenizer.bin"
        ): Tokenizer {
            val vocab = arrayOfNulls<String>(vocabSize)
            val vocabScores = FloatArray(vocabSize)
            fileSystem.read("../$path".toPath()) {
                val maxTokenLength = this.readIntLe()
                for (i in 0 until vocabSize) {
                    vocabScores[i] = this.readFloatLe()
                    val len = this.readIntLe()
                    val bytes = ByteArray(len)
                    this.readFully(bytes)
                    vocab[i] = bytes.decodeToString()
                }
            }
            return TokenizerImpl(vocab, vocabScores)
        }
    }
}
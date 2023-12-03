interface Model {
    fun generate(tokens: IntArray, steps: Int, temperature: Float, onTokens: (Int) -> Unit)
}
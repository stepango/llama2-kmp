import okio.FileSystem
import kotlin.system.exitProcess
import kotlin.time.measureTime

fun main(args: Array<String>) {

    // poor man's C argparse
    var checkPoint: String? = null // e.g. out/model.bin
    var temperature = 0.0f // 0.9f; // e.g. 1.0, or 0.0
    var steps = 256 // max number of steps to run for, 0: use seq_len
    var prompt: String? = null // prompt string

    // 'checkpoint' is necessary arg
    if (args.isEmpty()) {
        println("Usage: java -jar Llama2.jar <checkpoint_file> [temperature] [steps] [prompt]\n")
        exitProcess(1)
    }

    checkPoint = args[0]

    if (args.size >= 2) {
        // optional temperature. 0.0 = (deterministic) argmax sampling. 1.0 = baseline
        temperature = args[1].toFloat()
    }
    if (args.size >= 3) {
        steps = args[2].toInt()
    }
    if (args.size >= 4) {
        prompt = args[3]
    }

    val model = Llama2Utils.buildLlama2(checkPoint)

    val tokenize = TokenizerUtils.buildTokenizer(
        FileSystem.SYSTEM,
        model.config.vocabSize
    )

    // process the prompt, if any
    val promptTokens: IntArray = if (prompt != null) {
        tokenize.encode(prompt)
    } else {
        IntArray(0)
    }

    val time = measureTime{
        model.generate(promptTokens, steps, temperature) { next ->
            // following BOS token (1), sentencepiece decoder strips any leading whitespace (see PR#89)
            val tokenStr = tokenize.decode(next)
            print(tokenStr)
        }
    }.inWholeMilliseconds
    // report achieved tok/s
    println("\n\nachieved tok/s: ${(steps) / time.toDouble() * 1000}")
}
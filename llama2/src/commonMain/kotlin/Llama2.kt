import kotlin.math.exp
import kotlin.math.sqrt
import kotlin.random.Random

class Llama2(
    val config: Config,
    private val weights: Weights,
    private val state: RunState,
) : Model {

    // ----------------------------------------------------------------------------
    // initialization: read from checkpoint
    // ----------------------------------------------------------------------------
    // neural net blocks
    private fun accum(a: FloatArray, b: FloatArray, size: Int) {
        for (i in 0 until size) {
            a[i] += b[i]
        }
    }

    private fun rmsNorm(o: FloatArray, x: FloatArray, weight: FloatArray, weightOffset: Int, size: Int) {
        // calculate sum of squares
        var ss = 0.0f
        for (j in 0 until size) {
            ss += x[j] * x[j]
        }
        ss /= size.toFloat()
        ss += 1e-5f
        ss = 1.0f / sqrt(ss.toDouble()).toFloat()
        // normalize and scale
        for (j in 0 until size) {
            o[j] = weight[weightOffset + j] * (ss * x[j])
        }
    }

    private fun softmax(x: FloatArray, xOffset: Int, size: Int) {
        // find max value (for numerical stability)
        var maxVal = x[0 + xOffset]
        for (i in 1 until size) {
            if (x[i + xOffset] > maxVal) {
                maxVal = x[i + xOffset]
            }
        }
        // exp and sum
        var sum = 0.0f
        for (i in 0 until size) {
            x[i + xOffset] = exp((x[i + xOffset] - maxVal).toDouble()).toFloat()
            sum += x[i + xOffset]
        }
        // normalize
        for (i in 0 until size) {
            x[i + xOffset] /= sum
        }
    }

    private fun matmul(xout: FloatArray, x: FloatArray, w: FloatArray?, wOffset: Int, n: Int, d: Int) {
        // W (d,n) @ x (n,) -> xout (d,)
        // by far the most amount of time is spent inside this little function
        for (i in 0 until d) {
            var value = 0.0f
            for (j in 0 until n) {
                value += w!![wOffset + i * n + j] * x[j]
            }
            xout[i] = value
        }
    }

    private fun transformer(token: Int, pos: Int, p: Config, s: RunState, w: Weights) {
        // a few convenience variables
        val dim = p.dim
        val hiddenDim = p.hiddenDim
        val headSize = p.headSize

        // copy the token embedding into x
//        System.arraycopy(w.tokenEmbeddingTable, token * dim, s.x, 0, dim)
        w.tokenEmbeddingTable.copyInto(s.x, 0, token * dim, (token + 1) * dim)

        // forward all the layers
        for (l in 0 until p.nLayers) {

            // attention rmsnorm
            rmsNorm(s.xb, s.x, w.rmsAttWeight, dim * l, dim)

            // qkv matmuls for this position
            matmul(s.q, s.xb, w.wq, dim * dim * l, dim, dim)
            matmul(s.k, s.xb, w.wk, dim * dim * l, dim, dim)
            matmul(s.v, s.xb, w.wv, dim * dim * l, dim, dim)

            // apply RoPE rotation to the q and k vectors for each head
            for (h in 0 until p.nHeads) {
                // get the q and k vectors for this head
                val qOffset = h * headSize
                val kOffset = h * headSize
                // float* q = s->q + h * head_size;
                // float* k = s->k + h * head_size;
                // rotate q and k by the freq_cis_real and freq_cis_imag
                var i = 0
                while (i < headSize) {
                    val q0 = s.q[qOffset + i]
                    val q1 = s.q[qOffset + i + 1]
                    val k0 = s.k[kOffset + i]
                    val k1 = s.k[kOffset + i + 1]
                    val fcr = w.freqCisReal[pos * headSize / 2 + i / 2]
                    val fci = w.freqCisImag[pos * headSize / 2 + i / 2]
                    s.q[qOffset + i] = q0 * fcr - q1 * fci
                    s.q[qOffset + i + 1] = q0 * fci + q1 * fcr
                    s.k[kOffset + i] = k0 * fcr - k1 * fci
                    s.k[kOffset + i + 1] = k0 * fci + k1 * fcr
                    i += 2
                }
            }

            // save key,value at this time step (pos) to our kv cache
            val loff = l * p.seqLen * dim // kv cache layer offset for convenience
//            System.arraycopy(src = s.k, srcPos = 0, dest = s.keyCache, destPos = loff + pos * dim, length = dim)
            s.k.copyInto(s.keyCache, loff + pos * dim, 0, dim)

//            System.arraycopy(s.v, 0, s.valueCache, loff + pos * dim, dim)
            s.v.copyInto(s.valueCache, loff + pos * dim, 0, dim)

            // multihead attention. iterate over all heads
            for (h in 0 until p.nHeads) {
                // get the query vector for this head
                // float* q = s.q + h * head_size;
                val qOffset = h * headSize

                // attention scores for this head
                // float* att = s.att + h * p.seq_len;
                val attOffset = h * p.seqLen

                // iterate over all timesteps, including the current one
                for (t in 0..pos) {
                    // get the key vector for this head and at this timestep
                    // float* k = s.key_cache + loff + t * dim + h * head_size;
                    val keyCacheOffset = loff + t * dim + h * headSize
                    // calculate the attention score as the dot product of q and k
                    var score = 0.0f
                    for (i in 0 until headSize) {
                        score += s.q[qOffset + i] * s.keyCache[keyCacheOffset + i]
                    }
                    score /= sqrt(headSize.toDouble()).toFloat()
                    // save the score to the attention buffer
                    s.att[attOffset + t] = score
                }

                // softmax the scores to get attention weights, from 0..pos inclusively
                softmax(s.att, attOffset, pos + 1)

                // weighted sum of the values, store back into xb
                // float* xb = s.xb + h * head_size;
                val xbOffset = h * headSize
                // memset(xb, 0, head_size * sizeof(float));
                for (i in 0 until headSize) {
                    s.xb[xbOffset + i] = 0f
                }
                for (t in 0..pos) {
                    // get the value vector for this head and at this timestep
                    // float* v = s.value_cache + loff + t * dim + h * head_size;
                    val vOffset = loff + t * dim + h * headSize
                    // get the attention weight for this timestep
                    val a = s.att[attOffset + t]
                    // accumulate the weighted value inconfigto xb
                    for (i in 0 until headSize) {
                        s.xb[xbOffset + i] += a * s.valueCache[vOffset + i]
                    }
                }
            }

            // final matmul to get the output of the attention
            matmul(s.xb2, s.xb, w.wo, dim * dim * l, dim, dim)

            // residual connection back into x
            accum(s.x, s.xb2, dim)

            // ffn rmsnorm
            rmsNorm(s.xb, s.x, w.rmsFfnWeight, dim * l, dim)

            // Now for FFN in PyTorch we have: self.w2(F.silu(self.w1(x)) * self.w3(x))
            // first calculate self.w1(x) and self.w3(x)
            matmul(s.hb, s.xb, w.w1, dim * p.hiddenDim * l, dim, p.hiddenDim)
            matmul(s.hb2, s.xb, w.w3, p.hiddenDim * dim * l, dim, p.hiddenDim)

            // F.silu; silu(x)=x*σ(x),where σ(x) is the logistic sigmoid
            for (i in 0 until hiddenDim) {
                s.hb[i] = s.hb[i] / (1.0f + exp(-s.hb[i].toDouble()).toFloat())
            }

            // elementwise multiply with w3(x)
            for (i in 0 until hiddenDim) {
                s.hb[i] = s.hb[i] * s.hb2[i]
            }

            // final matmul to get the output of the ffn
            // matmul(s.xb, s.hb, w.w2 + l*dim*hidden_dim, hidden_dim, dim);
            matmul(s.xb, s.hb, w.w2, dim * p.hiddenDim * l, p.hiddenDim, dim)

            // residual connection
            accum(s.x, s.xb, dim)
        }

        // final rmsnorm
        rmsNorm(s.x, s.x, w.rmsFinalWeight, 0, dim)

        // classifier into logits
        matmul(s.logits, s.x, w.wcls, 0, dim, p.vocabSize)
    }

    private var rngSeed: Long = 0
    private fun randomU32(): Int {
        // xorshift rng: https://en.wikipedia.org/wiki/Xorshift#xorshift.2A
        rngSeed = rngSeed xor (rngSeed shr 12)
        rngSeed = rngSeed xor (rngSeed shl 25)
        rngSeed = rngSeed xor (rngSeed shr 27)
        return (rngSeed * 0x2545F4914F6CDD1DL shr 32).toInt()
    }

    private fun randomF32(): Float { // random float32 in [0,1)
        return (randomU32() ushr 8) / 16777216.0f
    }

    private fun sample(probabilities: FloatArray, n: Int): Int {
        // sample index from probabilities, they must sum to 1
        val r = randomF32()
        var cdf = 0.0f
        for (i in 0 until n) {
            cdf += probabilities[i]
            if (r < cdf) {
                return i
            }
        }
        return n - 1 // in case of rounding errors
    }

    private fun argmax(v: FloatArray, n: Int): Int {
        // return argmax of v in elements 0..n
        var maxI = 0
        var maxP = v[0]
        for (i in 1 until n) {
            if (v[i] > maxP) {
                maxI = i
                maxP = v[i]
            }
        }
        return maxI
    }

    override fun generate(tokens: IntArray, steps: Int, temperature: Float, onTokens: (Int) -> Unit) {
        // seed rng with time. if you want deterministic behavior use temperature 0.0
        rngSeed = Random.nextLong()
        val maxTokens = if (steps <= 0 || steps > config.seqLen) {
            config.seqLen
        } else {
            steps
        }
        internalGenerate(tokens, maxTokens, temperature, onTokens)
    }

    private fun internalGenerate(tokens: IntArray, steps: Int, temperature: Float, onTokens: (Int) -> Unit) {
        // start the main loop
        var next: Int // will store the next token in the sequence
        var token = 1 // init with token 1 (=BOS), as done in Llama-2 sentence piece tokenizer
        var pos = 0 // position in the sequence

        val numPromptTokens = tokens.size
        println("<s>") // explicit print the initial BOS token for stylistic symmetry
        // reasons
        while (pos < steps) {

            // forward the transformer to get logits for the next token
            transformer(token, pos, config, state, weights)
            if (pos < numPromptTokens) {
                // if we are still processing the input prompt, force the next prompt token
                next = tokens[pos]
            } else {
                // sample the next token
                if (temperature == 0.0f) {
                    // greedy argmax sampling: take the token with the highest probability
                    next = argmax(state.logits, config.vocabSize)
                } else {
                    // apply the temperature to the logits
                    for (q in 0 until config.vocabSize) {
                        state.logits[q] /= temperature
                    }
                    // apply softmax to the logits to get the probabilities for next token
                    softmax(state.logits, 0, config.vocabSize)
                    // we sample from this distribution to get the next token
                    next = sample(state.logits, config.vocabSize)
                }
            }

            onTokens.invoke(next)

            // advance forward
            token = next
            pos++
        }
    }
}

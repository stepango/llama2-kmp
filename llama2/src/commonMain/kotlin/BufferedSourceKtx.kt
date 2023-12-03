import okio.BufferedSource

fun BufferedSource.readFloatLe(): Float {
    return bytesToFloat(readByteArray(4))
}

fun BufferedSource.readFloatLeArray(size: Int): FloatArray {
    val floats = FloatArray(size)
    for (i in 0 until size) {
        floats[i] = readFloatLe()
    }
    return floats
}
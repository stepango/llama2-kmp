fun bytesToFloat(bytes: ByteArray, littleEndian: Boolean = true): Float {
    val bits = if (littleEndian) {
        bytes[0].toInt() and 0xFF or
                (bytes[1].toInt() and 0xFF shl 8) or
                (bytes[2].toInt() and 0xFF shl 16) or
                (bytes[3].toInt() shl 24)
    } else {
        bytes[3].toInt() and 0xFF or
                (bytes[2].toInt() and 0xFF shl 8) or
                (bytes[1].toInt() and 0xFF shl 16) or
                (bytes[0].toInt() shl 24)
    }
    return Float.fromBits(bits)
}
package com.ntnkeshri.cricketscorekeeper.utils

actual fun compressString(input: String): ByteArray {
    // Basic pass-through for Wasm since java.util.zip is JVM-only.
    // In a production app, we could implement a JS interop with CompressionStream here.
    return input.encodeToByteArray()
}

actual fun decompressString(input: ByteArray): String {
    return input.decodeToString()
}

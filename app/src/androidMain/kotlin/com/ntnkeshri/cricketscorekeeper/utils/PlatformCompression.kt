package com.ntnkeshri.cricketscorekeeper.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

actual fun compressString(input: String): ByteArray {
    val bytes = input.toByteArray(Charsets.UTF_8)
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).use { it.write(bytes) }
    return bos.toByteArray()
}

actual fun decompressString(input: ByteArray): String {
    val bis = ByteArrayInputStream(input)
    val uncompressedBytes = GZIPInputStream(bis).use { it.readBytes() }
    return String(uncompressedBytes, Charsets.UTF_8)
}

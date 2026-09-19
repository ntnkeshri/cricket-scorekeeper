package com.ntnkeshri.cricketscorekeeper.utils

import com.ntnkeshri.cricketscorekeeper.models.MatchState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object Base64Url {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

    fun encode(data: ByteArray): String {
        val sb = StringBuilder()
        var i = 0
        while (i < data.size) {
            val b0 = data[i++].toInt() and 0xFF
            val b1 = if (i < data.size) data[i++].toInt() and 0xFF else -1
            val b2 = if (i < data.size) data[i++].toInt() and 0xFF else -1

            val c0 = b0 shr 2
            val c1 = ((b0 and 0x03) shl 4) or (if (b1 != -1) (b1 shr 4) else 0)
            val c2 = if (b1 != -1) ((b1 and 0x0F) shl 2) or (if (b2 != -1) (b2 shr 6) else 0) else -1
            val c3 = if (b2 != -1) b2 and 0x3F else -1

            sb.append(ALPHABET[c0])
            sb.append(ALPHABET[c1])
            if (c2 != -1) sb.append(ALPHABET[c2]) else sb.append('=')
            if (c3 != -1) sb.append(ALPHABET[c3]) else sb.append('=')
        }
        return sb.toString()
    }

    fun decode(data: String): ByteArray {
        val cleanData = data.trimEnd('=')
        val list = mutableListOf<Byte>()
        var i = 0
        while (i < cleanData.length) {
            val c0 = ALPHABET.indexOf(cleanData[i++])
            val c1 = if (i < cleanData.length) ALPHABET.indexOf(cleanData[i++]) else 0
            val c2 = if (i < cleanData.length) ALPHABET.indexOf(cleanData[i++]) else -1
            val c3 = if (i < cleanData.length) ALPHABET.indexOf(cleanData[i++]) else -1

            val b0 = (c0 shl 2) or (c1 shr 4)
            list.add(b0.toByte())
            if (c2 != -1) {
                val b1 = ((c1 and 0x0F) shl 4) or (c2 shr 2)
                list.add(b1.toByte())
            }
            if (c3 != -1) {
                val b2 = ((c2 and 0x03) shl 6) or c3
                list.add(b2.toByte())
            }
        }
        return list.toByteArray()
    }
}

expect fun compressString(input: String): ByteArray
expect fun decompressString(input: ByteArray): String

object QRStateSerializer {
    val json = Json { ignoreUnknownKeys = true }

    fun compress(state: MatchState): String {
        val jsonString = json.encodeToString(state)
        val compressedBytes = compressString(jsonString)
        return Base64Url.encode(compressedBytes)
    }

    fun decompress(data: String): MatchState {
        val compressedBytes = Base64Url.decode(data)
        val jsonString = decompressString(compressedBytes)
        return json.decodeFromString(MatchState.serializer(), jsonString)
    }
}

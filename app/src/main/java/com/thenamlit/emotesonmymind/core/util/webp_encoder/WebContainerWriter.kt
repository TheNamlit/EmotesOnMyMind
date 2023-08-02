package com.thenamlit.emotesonmymind.core.util.webp_encoder

import android.util.Log
import com.thenamlit.emotesonmymind.core.util.Logging
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.BitSet


class WebContainerWriter(private val seekableOutputStream: SeekableOutputStream) {
    private val tag = Logging.loggingPrefix + WebContainerWriter::class.java.simpleName

    private var offset = 0

    fun writeBytes(byteArray: ByteArray) {
        Log.d(tag, "writeBytes | byteArray: $byteArray")

        writeBytesWithLength(byteArray = byteArray, length = byteArray.size)
    }

    fun writeBytesWithLength(byteArray: ByteArray, length: Int) {
        Log.d(tag, "writeBytesWithLength | byteArray: $byteArray, length: $length")

        seekableOutputStream.write(byteArray = byteArray, length = length)
        offset += length
    }

    fun writeHeader() {
        Log.d(tag, "writeHeader")

        writeBytes(
            byteArrayOf(
                'R'.code.toByte(),
                'I'.code.toByte(),
                'F'.code.toByte(),
                'F'.code.toByte()
            )
        )

        writeUInt32(value = 0)

        writeBytes(
            byteArrayOf(
                'W'.code.toByte(),
                'E'.code.toByte(),
                'B'.code.toByte(),
                'P'.code.toByte()
            )
        )
    }

    fun writeWebpChunk(webpChunk: WebpChunk) {
        Log.d(tag, "writeWebpChunk | webpChunk: $webpChunk")

        when (webpChunk.webpChunkType) {
            is WebpChunkType.VP8 -> {
                writePayloadChunk(
                    webpChunk = webpChunk,
                    fourCc = byteArrayOf(
                        'V'.code.toByte(),
                        'P'.code.toByte(),
                        '8'.code.toByte(),
                        ' '.code.toByte()
                    )
                )
            }

            is WebpChunkType.VP8L -> {
                writePayloadChunk(
                    webpChunk = webpChunk,
                    fourCc = byteArrayOf(
                        'V'.code.toByte(),
                        'P'.code.toByte(),
                        '8'.code.toByte(),
                        'L'.code.toByte()
                    )
                )
            }

            is WebpChunkType.VP8X -> {
                writeVp8x(webpChunk = webpChunk)
            }

            is WebpChunkType.ANIM -> {
                writeAnim(webpChunk = webpChunk)
            }

            is WebpChunkType.ANMF -> {
                writeAnmf(webpChunk = webpChunk)
            }
        }
    }

    fun writePayloadChunk(webpChunk: WebpChunk, fourCc: ByteArray) {
        Log.d(tag, "writePayloadChunk | webpChunk: $webpChunk, fourCc: $fourCc")

        writeBytesWithLength(byteArray = fourCc, 4)
        writeUInt32(value = webpChunk.payload.size)
        writeBytes(byteArray = webpChunk.payload.toByteArray())
    }

    fun writeVp8x(webpChunk: WebpChunk) {
        Log.d(tag, "writeVp8x | webpChunk: $webpChunk")

        writeBytes(
            byteArray = byteArrayOf(
                'V'.code.toByte(),
                'P'.code.toByte(),
                '8'.code.toByte(),
                'X'.code.toByte()
            )
        )

        writeUInt32(value = 10)

        val bitSet = BitSet(32)
        bitSet.set(0, webpChunk.hasIccp)
        bitSet.set(4, webpChunk.hasAlpha)
        bitSet.set(2, webpChunk.hasExif)
        bitSet.set(3, webpChunk.hasXmp)
        bitSet.set(1, webpChunk.hasAnim)

        writeBytes(byteArray = bitSetToBytes(bitSet = bitSet, bytes = 4))
        writeUInt24(value = webpChunk.width)
        writeUInt24(value = webpChunk.height)
    }

    fun writeAnim(webpChunk: WebpChunk) {
        Log.d(tag, "writeAnim | webpChunk: $webpChunk")

        writeBytes(
            byteArray = byteArrayOf(
                'A'.code.toByte(),
                'N'.code.toByte(),
                'I'.code.toByte(),
                'M'.code.toByte()
            )
        )
        writeUInt32(value = 6)

        writeUInt32(value = webpChunk.background)
        writeUInt16(value = webpChunk.loops)
    }

    fun writeAnmf(webpChunk: WebpChunk) {
        Log.d(tag, "writeAnmf | webpChunk: $webpChunk")

        writeBytes(
            byteArray = byteArrayOf(
                'A'.code.toByte(),
                'N'.code.toByte(),
                'M'.code.toByte(),
                'F'.code.toByte()
            )
        )
        writeUInt32(value = webpChunk.payload.size + 24)

        writeUInt24(value = webpChunk.x)        // 3 bytes (3)
        writeUInt24(value = webpChunk.y)        // 3 bytes (6)
        writeUInt24(value = webpChunk.width)    // 3 bytes (9)
        writeUInt24(value = webpChunk.height)   // 3 bytes (12)
        writeUInt24(value = webpChunk.duration) // 3 bytes (15)

        val bitSet = BitSet(8)
        bitSet.set(1, webpChunk.useAlphaBlending)
        bitSet.set(0, webpChunk.disposeToBackgroundColor)
        writeBytes(byteArray = bitSetToBytes(bitSet = bitSet, bytes = 1))   // 1 byte (16)

        if (webpChunk.isLossless) {
            writeBytes(
                // 4 bytes (20)
                byteArray = byteArrayOf(
                    'V'.code.toByte(),
                    'P'.code.toByte(),
                    '8'.code.toByte(),
                    'L'.code.toByte()
                )
            )
        } else {
            writeBytes(
                byteArray = byteArrayOf(
                    'V'.code.toByte(),
                    'P'.code.toByte(),
                    '8'.code.toByte(),
                    ' '.code.toByte()
                )
            )
        }

        writeUInt32(value = webpChunk.payload.size)
        writeBytes(byteArray = webpChunk.payload.toByteArray())
    }

    fun writeUInt(value: Int, bytes: Int) {
        Log.d(tag, "writeUInt | value: $value, bytes: $bytes")

        val byteArray: ByteArray =
            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
        writeBytesWithLength(byteArray = byteArray, length = bytes)
    }

    fun writeUInt16(value: Int) {
        Log.d(tag, "writeUInt16 | value: $value")

        writeUInt(value = value, bytes = 2)
    }

    fun writeUInt24(value: Int) {
        Log.d(tag, "writeUInt24 | value: $value")

        writeUInt(value = value, bytes = 3)
    }

    fun writeUInt32(value: Int) {
        Log.d(tag, "writeUInt32 | value: $value")

        writeUInt(value = value, bytes = 4)
    }

    fun bitSetToBytes(bitSet: BitSet, bytes: Int): ByteArray {
        Log.d(tag, "bitSetToBytes | bitSet: $bitSet, bytes: $bytes")

        val byteArray = ByteArray(bytes)
        val bitSetByteArray = bitSet.toByteArray()

        bitSetByteArray.forEachIndexed { index: Int, byte: Byte ->
            byteArray[index] = byte
        }

        return byteArray
    }

    fun close() {
        Log.d(tag, "close")

        val fileSize = offset - 8
        seekableOutputStream.setPosition(4)
        writeUInt32(value = fileSize)
    }
}

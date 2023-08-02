package com.thenamlit.emotesonmymind.core.util.webp_encoder

import android.util.Log
import com.thenamlit.emotesonmymind.core.util.Logging
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.BitSet


class WebContainerReader(private val inputStream: InputStream, private val debug: Boolean) {
    private val tag = Logging.loggingPrefix + WebContainerReader::class.java.simpleName

    private var fileSize: Int = 0
    private var offset: Int = 0


    fun readBytesWithLength(buffer: ByteArray, bytes: Int): Int {
        Log.d(tag, "readBytesWithLength | buffer: $buffer, bytes: $bytes")

        val count = inputStream.read(buffer, 0, bytes)
        offset += count
        return count
    }

    fun readUInt(bytes: Int): Int {
        Log.d(tag, "readUInt | bytes: $bytes")

        val byteArray = byteArrayOf(
            0.toByte(),
            0.toByte(),
            0.toByte(),
            0.toByte()
        )

        readBytesWithLength(buffer = byteArray, bytes = bytes)

        return ByteBuffer.wrap(byteArray, 0, 4).order(ByteOrder.LITTLE_ENDIAN).int
    }

    fun readUInt16(): Int {
        Log.d(tag, "readUInt16")

        return readUInt(bytes = 2)
    }

    fun readUInt24(): Int {
        Log.d(tag, "readUInt24")

        return readUInt(bytes = 3)
    }

    fun readUInt32(): Int {
        Log.d(tag, "readUInt32")

        return readUInt(bytes = 4)
    }

    fun readAndGetWebpChunk(): WebpChunk? {
        Log.d(tag, "readAndGetWebpChunk")

        val fcc = ByteArray(4)

        if (readBytesWithLength(buffer = fcc, bytes = 4) > 0) {
            if (isFourCc(
                    byteArray = fcc,
                    a = 'V',
                    b = 'P',
                    c = '8',
                    d = ' ',
                )
            ) {
                return readVp8()
            }

            if (isFourCc(
                    byteArray = fcc,
                    a = 'V',
                    b = 'P',
                    c = '8',
                    d = 'L',
                )
            ) {
                return readVp8l()
            }

            if (isFourCc(
                    byteArray = fcc,
                    a = 'V',
                    b = 'P',
                    c = '8',
                    d = 'X',
                )
            ) {
                return readVp8x()
            }

            if (isFourCc(
                    byteArray = fcc,
                    a = 'A',
                    b = 'N',
                    c = 'I',
                    d = 'M',
                )
            ) {
                return readAnim()
            }

            if (isFourCc(
                    byteArray = fcc,
                    a = 'A',
                    b = 'N',
                    c = 'M',
                    d = 'F',
                )
            ) {
                return readAnmf()
            }

            Log.e(tag, "readAndGetWebpChunk | ERROR #1")
        }

        if (fileSize != offset) {
            Log.e(tag, "readAndGetWebpChunk | ERROR #2")
        }

        return null
    }

    fun readPayload(bytes: Int): ByteArray {
        Log.d(tag, "readPayload | bytes: $bytes")

        val payload = ByteArray(bytes)
        if (readBytesWithLength(buffer = payload, bytes = bytes) != bytes) {
            Log.e(tag, "readPayload | Exception") // TODO
        }
        return payload
    }

    fun readVp8(): WebpChunk {
        Log.d(tag, "readVp8")

        val chunkSize = readUInt32()

        return WebpChunk(
            webpChunkType = WebpChunkType.VP8,
            isLossless = true,
            payload = readPayload(bytes = chunkSize).toList(),
        )
    }

    fun readVp8l(): WebpChunk {
        Log.d(tag, "readVp8l")

        val chunkSize = readUInt32()

        return WebpChunk(
            webpChunkType = WebpChunkType.VP8L,
            isLossless = true,
            payload = readPayload(bytes = chunkSize).toList(),
        )
    }

    fun readVp8x(): WebpChunk {
        Log.d(tag, "readVp8x")

        val chunkSize = readUInt32()

        if (chunkSize != 10) {
            Log.e(tag, "readVp8x | Exception") // TODO
        }

        val flags = ByteArray(4)
        readBytesWithLength(buffer = flags, 4)
        val bitSet = BitSet.valueOf(flags)

        return WebpChunk(
            webpChunkType = WebpChunkType.VP8X,
            isLossless = true,
            payload = readPayload(bytes = chunkSize).toList(),
            hasIccp = bitSet.get(0),
            hasAnim = bitSet.get(1),
            hasExif = bitSet.get(2),
            hasXmp = bitSet.get(3),
            hasAlpha = bitSet.get(4),
            width = readUInt24(),
            height = readUInt24(),
        )
    }

    fun readAnim(): WebpChunk {
        Log.d(tag, "readAnim")

        val chunkSize = readUInt32()
        if (chunkSize != 6) {
            Log.e(tag, "readVp8x | Exception") // TODO
        }

        return WebpChunk(
            webpChunkType = WebpChunkType.ANIM,
            background = readUInt32(),
            loops = readUInt16()
        )
    }

    fun readAnmf(): WebpChunk {
        Log.d(tag, "readAnmf")

        val chunkSize = readUInt32()

        val flags = ByteArray(1)
        readBytesWithLength(buffer = flags, bytes = 1)

        val bitSet = BitSet.valueOf(flags)

        val cch = ByteArray(4)
        readBytesWithLength(buffer = cch, bytes = 4)
        var webpChunkIsLossless: Boolean = false

        if (isFourCc(
                byteArray = cch,
                a = 'V',
                b = 'P',
                c = '8',
                d = 'L',
            )
        ) {
            webpChunkIsLossless = true
        } else if (
            isFourCc(
                byteArray = cch,
                a = 'V',
                b = 'P',
                c = '8',
                d = ' ',
            )
        ) {
            webpChunkIsLossless = false
        } else {
            Log.e(tag, "readAnmf | ERROR")  // TODO
        }

        readUInt32()    // Payload size
        val payloadSize: Int = chunkSize - 24

        return WebpChunk(
            webpChunkType = WebpChunkType.ANMF,
            x = readUInt24(),
            y = readUInt24(),
            width = readUInt24(),
            height = readUInt24(),
            duration = readUInt24(),
            useAlphaBlending = bitSet.get(1),
            disposeToBackgroundColor = bitSet.get(0),
            isLossless = webpChunkIsLossless,
            payload = readPayload(bytes = payloadSize).toList()
        )
    }

    fun readHeader() {
        Log.d(tag, "readHeader")

        val fcc = ByteArray(4)

        readBytesWithLength(buffer = fcc, bytes = 4)
        if (!isFourCc(byteArray = fcc, a = 'R', b = 'I', c = 'F', d = 'F')) {
            Log.e(tag, "readHeader #1")
            // TODO: Error
        }

        fileSize = readUInt32() + 8 - 1

        readBytesWithLength(buffer = fcc, bytes = 4)
        if (!isFourCc(byteArray = fcc, a = 'W', b = 'E', c = 'B', d = 'P')) {
            Log.e(tag, "readHeader #2")
            // TODO: Error
        }
    }

    fun isFourCc(byteArray: ByteArray, a: Char, b: Char, c: Char, d: Char): Boolean {
        Log.d(tag, "isFourCc | byteArray: $byteArray, a: $a, b: $b, c: $c, d: $d")

        return byteArray[0] == a.code.toByte() &&
                byteArray[1] == b.code.toByte() &&
                byteArray[2] == c.code.toByte() &&
                byteArray[3] == d.code.toByte()
    }

    fun close() {
        Log.d(tag, "close - CURRENTLY EMPTY!!!")
    }
}

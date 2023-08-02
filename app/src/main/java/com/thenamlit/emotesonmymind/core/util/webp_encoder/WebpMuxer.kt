package com.thenamlit.emotesonmymind.core.util.webp_encoder

import android.util.Log
import com.thenamlit.emotesonmymind.core.util.Logging
import java.io.InputStream


class WebpMuxer(private val writer: WebContainerWriter) {
    private val tag = Logging.loggingPrefix + WebpMuxer::class.java.simpleName

    private var isFirstFrame: Boolean = true
    private var loops: Int = -1
    private var duration: Int = -1
    private var width: Int = 0
    private var height: Int = 0

    fun setLoops(loops: Int) {
        Log.d(tag, "setLoops | loops: $loops")

        this.loops = loops
    }

    fun setDuration(duration: Int) {
        Log.d(tag, "setDuration | duration: $duration")

        this.duration = duration
    }

    fun setWidth(width: Int) {
        Log.d(tag, "setWidth | width: $width")

        this.width = width
    }

    fun setHeight(height: Int) {
        Log.d(tag, "setHeight | height: $height")

        this.height = height
    }

    fun close() {
        Log.d(tag, "close")

        writer.close()
    }

    fun writeFirstFrameFromWebm(inputStream: InputStream) {
        Log.d(tag, "writeFirstFrameFromWebm | inputStream: $inputStream")

        val reader = WebContainerReader(inputStream = inputStream, debug = false)
        reader.readHeader()

        val webpChunk: WebpChunk? = readFirstChunkWithPayload(reader = reader)
        reader.close()

        webpChunk?.let { chunk: WebpChunk ->
            writeFrame(chunk.payload.toByteArray(), chunk.isLossless)
        } ?: kotlin.run {
            Log.e(tag, "writeFirstFrameFromWebm | WebpChunk undefined")
        }
    }

    fun readFirstChunkWithPayload(reader: WebContainerReader): WebpChunk? {
        Log.d(tag, "readFirstChunkWithPayload | reader: $reader")

        return reader.readAndGetWebpChunk()
    }

    fun writeFrame(payload: ByteArray, isLossless: Boolean) {
        Log.d(tag, "writeFrame | payload: $payload, isLossless: $isLossless")

        if (isFirstFrame) {
            isFirstFrame = false
            writeHeader()
        }

        if (hasAnim()) {
            writeAnmf(payload, isLossless)
        } else {
            writeVp8(payload, isLossless)
        }
    }

    fun writeHeader() {
        Log.d(tag, "writeHeader")

        writer.writeHeader()

        val vp8x = WebpChunk(
            webpChunkType = WebpChunkType.VP8X,
            hasAnim = hasAnim(),
            hasAlpha = false,
            hasXmp = false,
            hasExif = false,
            hasIccp = false,
            width = width - 1,
            height = height - 1,
        )

        writer.writeWebpChunk(webpChunk = vp8x)

        if (vp8x.hasAnim) {
            val anim = WebpChunk(
                webpChunkType = WebpChunkType.ANIM,
                background = -1,
                loops = loops,
            )
            writer.writeWebpChunk(webpChunk = anim)
        }
    }

    fun hasAnim(): Boolean {
        Log.d(tag, "hasAnim")

        return loops >= 0 && duration >= 0
    }

    fun writeAnmf(payload: ByteArray, isLossless: Boolean) {
        Log.d(tag, "writeAnmf | payload: $payload, isLossless: $isLossless")

        val anmf = WebpChunk(
            webpChunkType = WebpChunkType.ANMF,
            x = 0,
            y = 0,
            width = width - 1,
            height = height - 1,
            duration = duration,
            isLossless = isLossless,
            payload = payload.toList(),
            useAlphaBlending = false,
            disposeToBackgroundColor = false
        )

        writer.writeWebpChunk(webpChunk = anmf)
    }

    fun writeVp8(payload: ByteArray, isLossless: Boolean) {
        Log.d(tag, "writeVp8 | payload: $payload, isLossless: $isLossless")

        val vp8 = WebpChunk(
            webpChunkType = if (isLossless) WebpChunkType.VP8L else WebpChunkType.VP8,
            isLossless = isLossless,
            payload = payload.toList()
        )

        writer.writeWebpChunk(webpChunk = vp8)
    }
}

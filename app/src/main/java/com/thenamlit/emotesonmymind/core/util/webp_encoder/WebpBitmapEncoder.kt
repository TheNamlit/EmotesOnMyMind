package com.thenamlit.emotesonmymind.core.util.webp_encoder

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import com.thenamlit.emotesonmymind.core.util.Logging
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File


// https://github.com/b4rtaz/android-webp-encoder/tree/master
//class WebpBitmapEncoder() {
class WebpBitmapEncoder(private val file: File) {
    private val tag = Logging.loggingPrefix + WebpBitmapEncoder::class.java.simpleName

    private var outputStream: FileSeekableOutputStream

    private var writer: WebContainerWriter
    private var webpMuxer: WebpMuxer
    private var isFirstFrame = true


    init {
        Log.d(tag, "init")

        outputStream = FileSeekableOutputStream(file = file)
        writer = WebContainerWriter(seekableOutputStream = outputStream)
        webpMuxer = WebpMuxer(writer = writer)
    }

    fun setLoops(loops: Int) {
        Log.d(tag, "setLoops | loops: $loops")

        webpMuxer.setLoops(loops = loops)
    }

    fun setDuration(duration: Int) {
        Log.d(tag, "setDuration | duration: $duration")

        webpMuxer.setDuration(duration = duration)
    }

    fun writeFrameWithByteArray(byteArray: ByteArray) {
        Log.d(tag, "writeFrameWithByteArray | byteArray: $byteArray")

        val inputBuffer = ByteArrayInputStream(byteArray)
        webpMuxer.writeFirstFrameFromWebm(inputStream = inputBuffer)
        inputBuffer.close()
    }

    fun writeFrame(frame: Bitmap, compress: Int) {
        Log.d(tag, "writeFrame | frame: $frame, compress: $compress")

        if (isFirstFrame) {
            isFirstFrame = false
            webpMuxer.setWidth(width = frame.width)
            webpMuxer.setHeight(height = frame.height)
        }

        val outputBuffer = ByteArrayOutputStream()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            frame.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, compress, outputBuffer)
        } else {
            frame.compress(Bitmap.CompressFormat.WEBP, compress, outputBuffer)
        }

        val inputBuffer = ByteArrayInputStream(outputBuffer.toByteArray())
        webpMuxer.writeFirstFrameFromWebm(inputStream = inputBuffer)
        outputBuffer.close()
        inputBuffer.close()
    }

    fun close() {
        Log.d(tag, "close")

        webpMuxer.close()
        outputStream.close()
    }
}

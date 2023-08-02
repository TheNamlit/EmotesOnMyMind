package com.thenamlit.emotesonmymind.core.util.webp_encoder

import android.util.Log
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import com.thenamlit.emotesonmymind.core.util.UiText
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.ClosedChannelException


class FileSeekableOutputStream(private val file: File) : SeekableOutputStream {
    private val tag = Logging.loggingPrefix + FileSeekableOutputStream::class.java.simpleName

    private var outputStream: FileOutputStream

    init {
        outputStream = FileOutputStream(file)
    }

    override fun setPosition(position: Int): SimpleResource {
        Log.d(tag, "setPosition | position: $position")

        return try {
            outputStream.channel.position(position.toLong())

            Resource.Success(data = null)
        } catch (e: ClosedChannelException) {
            Resource.Error(
                uiText = UiText.DynamicString(
                    value = "setPosition | ClosedChannelException: ${e.message}\n${e.stackTrace}"
                )
            )
        } catch (e: IllegalArgumentException) {
            Resource.Error(
                uiText = UiText.DynamicString(
                    value = "setPosition | IllegalArgumentException: ${e.message}\n${e.stackTrace}"
                )
            )
        } catch (e: IOException) {
            Resource.Error(
                uiText = UiText.DynamicString(
                    value = "setPosition | IOException: ${e.message}\n${e.stackTrace}"
                )
            )
        }
    }

    override fun write(byteArray: ByteArray, length: Int): SimpleResource {
        Log.d(tag, "write | byteArray: $byteArray, length: $length")

        return try {
            outputStream.write(byteArray, 0, length)

            Resource.Success(data = null)
        } catch (e: IOException) {
            Resource.Error(
                uiText = UiText.DynamicString(
                    value = "setPosition | IOException: ${e.message}\n${e.stackTrace}"
                )
            )
        }
    }

    override fun close(): SimpleResource {
        Log.d(tag, "close")

        return try {
            outputStream.close()

            Resource.Success(data = null)
        } catch (e: IOException) {
            Resource.Error(
                uiText = UiText.DynamicString(
                    value = "setPosition | ClosedChannelException: ${e.message}\n${e.stackTrace}"
                )
            )
        }
    }
}

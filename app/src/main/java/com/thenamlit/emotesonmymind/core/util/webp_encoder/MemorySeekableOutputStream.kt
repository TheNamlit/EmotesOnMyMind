package com.thenamlit.emotesonmymind.core.util.webp_encoder

import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource

class MemorySeekableOutputStream : SeekableOutputStream {

    private var buffer: ByteArray? = null
    private var position: Int = 0

    override fun setPosition(position: Int): SimpleResource {
        this.position = position
        return Resource.Success(data = null)    // TODO: Not used in Java-Example, there it's a void
    }

    override fun write(byteArray: ByteArray, length: Int): SimpleResource {
        val min = position + length

        if (buffer == null || buffer?.size!! < min) {
            val b = ByteArray(min)
            buffer?.let {
                System.arraycopy(it, 0, b, 0, it.size)
            }
            buffer = b
        }

        for (i in 0..length) {
            buffer!![position++] = byteArray[i]
        }

        return Resource.Success(data = null)    // TODO: Not used in Java-Example, there it's a void
    }

    override fun close(): SimpleResource {
        return Resource.Success(data = null)    // TODO: Not used in Java-Example, there it's a void
    }

    fun toArray(): ByteArray? {
        return buffer
    }
}
package com.thenamlit.emotesonmymind.core.util.webp_encoder

import com.thenamlit.emotesonmymind.core.util.SimpleResource


interface SeekableOutputStream {
    fun setPosition(position: Int): SimpleResource
    fun write(byteArray: ByteArray, length: Int): SimpleResource
    fun close(): SimpleResource
}

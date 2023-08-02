package com.thenamlit.emotesonmymind.core.util.webp_encoder

data class WebpChunk(
    val webpChunkType: WebpChunkType,

    val x: Int = 0,
    val y: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
    val loops: Int = 0,
    val duration: Int = 0,
    val background: Int = 0,

    val payload: List<Byte> = emptyList(),
    val isLossless: Boolean = true,

    val hasAnim: Boolean = false,
    val hasXmp: Boolean = false,
    val hasExif: Boolean = false,
    val hasAlpha: Boolean = false,
    val hasIccp: Boolean = false,

    val useAlphaBlending: Boolean = false,
    val disposeToBackgroundColor: Boolean = false,
)

sealed class WebpChunkType {
    object VP8X : WebpChunkType()
    object VP8 : WebpChunkType()
    object VP8L : WebpChunkType()
    object ANIM : WebpChunkType()
    object ANMF : WebpChunkType()
}

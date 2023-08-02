package com.thenamlit.emotesonmymind.core.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.OffsetDateTime


@Parcelize
@Serializable
data class Sticker(
    val id: String = "",
    val name: String,
    val createdAt: Long = LocalDateTime.now().toEpochSecond(OffsetDateTime.now().offset),
    val lastModified: Long = LocalDateTime.now().toEpochSecond(OffsetDateTime.now().offset),
    val stickerImageData: StickerImageData,
    val remoteEmoteData: StickerRemoteEmoteData,
) : Parcelable

@Parcelize
@Serializable
data class StickerImageData(
    val width: Int,
    val height: Int,
    val size: Int,
    val frameCount: Int,
    val format: String,
    val animated: Boolean,
) : Parcelable

@Parcelize
@Serializable
data class StickerRemoteEmoteData(
    val id: String,
    val url: String,
    val createdAt: Long,
    val owner: StickerRemoteEmoteDataOwner,
    val hostFile: StickerRemoteEmoteDataHostFile,
) : Parcelable

@Parcelize
@Serializable
data class StickerRemoteEmoteDataHostFile(
    val width: Int,
    val height: Int,
    val size: Int,
    val frameCount: Int,
    val format: String,
    val animated: Boolean,
) : Parcelable

@Parcelize
@Serializable
data class StickerRemoteEmoteDataOwner(
    val id: String,
    val username: String,
    val avatarUrl: String,
) : Parcelable

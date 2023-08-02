package com.thenamlit.emotesonmymind.core.domain.repository

import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import kotlinx.coroutines.flow.Flow


interface StickerRepository {
    fun getAllSticker(): Flow<List<Sticker>>

    fun getAllAnimated(): Flow<List<Sticker>>
    fun getAllNotAnimated(): Flow<List<Sticker>>

    fun getAllNotInIdList(idList: List<String>, animated: Boolean): Flow<List<Sticker>>

    suspend fun getStickerById(stickerId: String): Resource<Sticker>

    suspend fun getStickerByRemoteEmoteId(emoteId: String): Resource<Sticker>

    suspend fun insertSticker(sticker: Sticker): SimpleResource

    suspend fun deleteStickerById(id: String): SimpleResource
}

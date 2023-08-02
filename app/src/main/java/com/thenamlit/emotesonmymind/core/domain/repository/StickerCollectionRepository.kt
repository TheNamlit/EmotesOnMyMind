package com.thenamlit.emotesonmymind.core.domain.repository

import com.thenamlit.emotesonmymind.core.domain.models.StickerCollection
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import kotlinx.coroutines.flow.Flow


interface StickerCollectionRepository {
    fun getAllStickerCollections(): Flow<List<StickerCollection>>
    fun getAllAnimatedStickerCollections(): Flow<List<StickerCollection>>
    fun getAllNotAnimatedStickerCollections(): Flow<List<StickerCollection>>
    fun getByIdSynchronized(id: String): Resource<StickerCollection>
    fun getAllSynchronized(): Resource<List<StickerCollection>>

    fun getAllCollectionsOfSticker(
        stickerId: String,
        animated: Boolean,
    ): Flow<List<StickerCollection>>

    fun getAllSelectedCollectionsOfSticker(
        stickerId: String,
        animated: Boolean,
    ): Flow<List<StickerCollection>>

    fun getAllNotSelectedCollectionsOfSticker(
        stickerId: String,
        animated: Boolean,
    ): Flow<List<StickerCollection>>

    fun getCollectionById(collectionId: String): Resource<StickerCollection>

    suspend fun createStickerCollection(name: String, animated: Boolean): SimpleResource

    suspend fun addStickerToCollection(
        stickerCollectionId: String,
        stickerId: String,
    ): SimpleResource

    suspend fun addStickerListToCollection(
        stickerCollectionId: String,
        stickerIds: List<String>,
    ): SimpleResource

    suspend fun removeStickerFromCollection(
        stickerCollectionId: String,
        stickerId: String,
    ): SimpleResource

    suspend fun removeStickerFromEveryCollection(
        stickerId: String,
        animated: Boolean,
    ): Resource<List<String>>

    suspend fun deleteById(id: String): SimpleResource

    suspend fun updateName(id: String, name: String): SimpleResource
}

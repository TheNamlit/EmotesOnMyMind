package com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection

import android.util.Log
import com.thenamlit.emotesonmymind.core.domain.models.StickerCollection
import com.thenamlit.emotesonmymind.core.domain.repository.StickerCollectionRepository
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetCollectionsUseCase @Inject constructor(
    private val stickerCollectionRepository: StickerCollectionRepository,
) {
    private val tag = Logging.loggingPrefix + GetCollectionsUseCase::class.java.simpleName

    fun getAll(): Flow<List<StickerCollection>> {
        Log.d(tag, "getAll")

        return stickerCollectionRepository.getAllStickerCollections()
    }

    fun getByIdSynchronized(id: String): Resource<StickerCollection> {
        return stickerCollectionRepository.getByIdSynchronized(id = id)
    }

    fun getAllSynchronized(): Resource<List<StickerCollection>> {
        return stickerCollectionRepository.getAllSynchronized()
    }

    fun getAllAnimated(): Flow<List<StickerCollection>> {
        Log.d(tag, "getAllAnimated")

        return stickerCollectionRepository.getAllAnimatedStickerCollections()
    }

    fun getAllNotAnimated(): Flow<List<StickerCollection>> {
        Log.d(tag, "getAllNotAnimated")

        return stickerCollectionRepository.getAllNotAnimatedStickerCollections()
    }

    fun getAllCollectionsOfSticker(
        stickerId: String,
        animated: Boolean,
    ): Flow<List<StickerCollection>> {
        Log.d(tag, "getAllCollectionsOfSticker | stickerId: $stickerId, animated: $animated")

        return stickerCollectionRepository.getAllCollectionsOfSticker(
            stickerId = stickerId,
            animated = animated
        )
    }

    fun getAllSelectedCollectionsOfSticker(
        stickerId: String,
        animated: Boolean,
    ): Flow<List<StickerCollection>> {
        Log.d(
            tag,
            "getAllSelectedCollectionsOfSticker | stickerId: $stickerId, animated: $animated"
        )

        return stickerCollectionRepository.getAllSelectedCollectionsOfSticker(
            stickerId = stickerId,
            animated = animated
        )
    }

    fun getAllNotSelectedCollectionsOfSticker(
        stickerId: String,
        animated: Boolean,
    ): Flow<List<StickerCollection>> {
        Log.d(
            tag,
            "getAllNotSelectedCollectionsOfSticker | stickerId: $stickerId, animated: $animated"
        )

        return stickerCollectionRepository.getAllNotSelectedCollectionsOfSticker(
            stickerId = stickerId,
            animated = animated
        )
    }
}

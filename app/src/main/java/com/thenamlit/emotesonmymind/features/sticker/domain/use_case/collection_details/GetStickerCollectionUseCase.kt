package com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection_details

import android.util.Log
import com.thenamlit.emotesonmymind.core.domain.models.StickerCollection
import com.thenamlit.emotesonmymind.core.domain.repository.StickerCollectionRepository
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import javax.inject.Inject


class GetStickerCollectionUseCase @Inject constructor(
    private val stickerCollectionRepository: StickerCollectionRepository,
) {
    private val tag = Logging.loggingPrefix + GetStickerCollectionUseCase::class.java.simpleName

    fun byId(stickerCollectionId: String): Resource<StickerCollection> {
        Log.d(tag, "byId | stickerCollectionId: $stickerCollectionId")

        return stickerCollectionRepository.getCollectionById(collectionId = stickerCollectionId)
    }
}

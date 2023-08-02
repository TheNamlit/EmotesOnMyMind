package com.thenamlit.emotesonmymind.features.sticker.domain.use_case.sticker_details

import android.util.Log
import com.thenamlit.emotesonmymind.core.domain.repository.StickerCollectionRepository
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import javax.inject.Inject


class AddStickerToCollectionUseCase @Inject constructor(
    private val stickerCollectionRepository: StickerCollectionRepository,
) {
    private val tag = Logging.loggingPrefix + AddStickerToCollectionUseCase::class.java.simpleName

    suspend operator fun invoke(stickerCollectionId: String, stickerId: String): SimpleResource {
        Log.d(tag, "invoke | stickerCollectionId: $stickerCollectionId, stickerId: $stickerId")

        return stickerCollectionRepository.addStickerToCollection(
            stickerCollectionId = stickerCollectionId,
            stickerId = stickerId
        )
    }
}

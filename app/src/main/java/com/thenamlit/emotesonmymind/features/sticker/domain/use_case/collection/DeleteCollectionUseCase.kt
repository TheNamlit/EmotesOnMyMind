package com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection

import android.util.Log
import com.thenamlit.emotesonmymind.core.domain.repository.StickerCollectionRepository
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import javax.inject.Inject


class DeleteCollectionUseCase @Inject constructor(
    private val stickerCollectionRepository: StickerCollectionRepository,
) {
    private val tag = Logging.loggingPrefix + DeleteCollectionUseCase::class.java.simpleName

    suspend operator fun invoke(id: String): SimpleResource {
        Log.d(tag, "invoke | id: $id")

        return stickerCollectionRepository.deleteById(id = id)
    }
}

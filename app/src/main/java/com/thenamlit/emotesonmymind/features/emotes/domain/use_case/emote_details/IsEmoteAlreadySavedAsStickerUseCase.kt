package com.thenamlit.emotesonmymind.features.emotes.domain.use_case.emote_details

import android.util.Log
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.repository.StickerRepository
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import javax.inject.Inject


class IsEmoteAlreadySavedAsStickerUseCase @Inject constructor(
    private val stickerRepository: StickerRepository,
) {
    private val tag =
        Logging.loggingPrefix + IsEmoteAlreadySavedAsStickerUseCase::class.java.simpleName

    suspend operator fun invoke(emoteId: String): Resource<Sticker> {
        Log.d(tag, "invoke | emoteId: $emoteId")

        return stickerRepository.getStickerByRemoteEmoteId(emoteId = emoteId)
    }
}

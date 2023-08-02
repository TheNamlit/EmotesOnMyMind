package com.thenamlit.emotesonmymind.features.emotes.domain.use_case.main_feed

import android.util.Log
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.repository.StickerRepository
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import javax.inject.Inject


class CheckIfEmoteIsAlreadyDownloadedAsStickerUseCase @Inject constructor(
    private val stickerRepository: StickerRepository,
) {
    private val tag =
        Logging.loggingPrefix + CheckIfEmoteIsAlreadyDownloadedAsStickerUseCase::class.java.simpleName

    suspend operator fun invoke(remoteEmoteId: String): Resource<Sticker> {
        Log.d(tag, "invoke | remoteEmoteId: $remoteEmoteId")

        return stickerRepository.getStickerByRemoteEmoteId(emoteId = remoteEmoteId)
    }
}

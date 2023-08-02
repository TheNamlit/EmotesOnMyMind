package com.thenamlit.emotesonmymind.features.emotes.domain.use_case.emote_details

import android.util.Log
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetails
import com.thenamlit.emotesonmymind.features.emotes.domain.repository.EmoteRepository
import com.thenamlit.emotesonmymind.type.ImageFormat
import javax.inject.Inject


class GetEmoteDetailsUseCase @Inject constructor(
    private val emoteRepository: EmoteRepository,
) {
    private val tag = Logging.loggingPrefix + GetEmoteDetailsUseCase::class.java.simpleName

    suspend operator fun invoke(
        emoteId: String,
        formats: List<ImageFormat>,
    ): Resource<EmoteDetails> {
        Log.d(tag, "invoke | emoteId: $emoteId, formats: $formats")

        return emoteRepository.getEmoteDetails(
            emoteId = emoteId,
            formats = formats
        )
    }
}

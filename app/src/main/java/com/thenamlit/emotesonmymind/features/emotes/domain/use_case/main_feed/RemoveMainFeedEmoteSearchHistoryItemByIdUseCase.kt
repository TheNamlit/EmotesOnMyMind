package com.thenamlit.emotesonmymind.features.emotes.domain.use_case.main_feed

import android.util.Log
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import com.thenamlit.emotesonmymind.features.emotes.domain.repository.EmoteRepository
import javax.inject.Inject


class RemoveMainFeedEmoteSearchHistoryItemByIdUseCase @Inject constructor(
    private val emoteRepository: EmoteRepository,
) {
    private val tag =
        Logging.loggingPrefix + RemoveMainFeedEmoteSearchHistoryItemByIdUseCase::class.java.simpleName

    suspend operator fun invoke(id: String): SimpleResource {
        Log.d(tag, "invoke | id: $id")

        return emoteRepository.deleteMainFeedEmoteSearchHistoryItemById(id = id)
    }
}

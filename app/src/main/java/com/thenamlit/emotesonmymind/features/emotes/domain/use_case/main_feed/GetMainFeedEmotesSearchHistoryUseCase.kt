package com.thenamlit.emotesonmymind.features.emotes.domain.use_case.main_feed

import android.util.Log
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmoteSearchHistoryItem
import com.thenamlit.emotesonmymind.features.emotes.domain.repository.EmoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetMainFeedEmotesSearchHistoryUseCase @Inject constructor(
    private val emoteRepository: EmoteRepository,
) {
    private val tag =
        Logging.loggingPrefix + GetMainFeedEmotesSearchHistoryUseCase::class.java.simpleName

    operator fun invoke(): Flow<List<MainFeedEmoteSearchHistoryItem>> {
        Log.d(tag, "invoke")

        return emoteRepository.getMainFeedEmoteSearchHistory()
    }
}

package com.thenamlit.emotesonmymind.features.emotes.domain.use_case.main_feed

import android.util.Log
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmoteSearchHistoryItem
import com.thenamlit.emotesonmymind.features.emotes.domain.repository.EmoteRepository
import javax.inject.Inject


class AddMainFeedEmoteSearchHistoryItemUseCase @Inject constructor(
    private val emoteRepository: EmoteRepository,
) {
    private val tag =
        Logging.loggingPrefix + AddMainFeedEmoteSearchHistoryItemUseCase::class.java.simpleName

    suspend operator fun invoke(searchQuery: String): SimpleResource {
        Log.d(tag, "invoke | searchQuery: $searchQuery")

        if (searchQuery.isNotEmpty()) {
            val foundEmoteResult: Resource<MainFeedEmoteSearchHistoryItem> =
                emoteRepository.getMainFeedEmoteSearchHistoryItemByValue(
                    value = searchQuery
                )

            return when (foundEmoteResult) {
                is Resource.Success -> {
                    // Update lastRequested-Value to now
                    foundEmoteResult.data
                        ?.let { mainFeedEmoteSearchHistoryItem: MainFeedEmoteSearchHistoryItem ->
                            return emoteRepository.updateLastRequestedToNow(
                                id = mainFeedEmoteSearchHistoryItem.id
                            )
                        } ?: kotlin.run {
                        return Resource.Error(
                            logging = "invoke | MainFeedEmoteSearchHistoryItem was undefined"
                        )
                    }
                }

                is Resource.Error -> {
                    // Adding because it wasn't found
                    emoteRepository.addMainFeedEmoteSearchHistoryItem(
                        mainFeedEmoteSearchHistoryItem = MainFeedEmoteSearchHistoryItem(
                            value = searchQuery
                        )
                    )
                }
            }
        } else {
            return Resource.Success(data = null)
        }
    }
}

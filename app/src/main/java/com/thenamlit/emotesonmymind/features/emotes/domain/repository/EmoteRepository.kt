package com.thenamlit.emotesonmymind.features.emotes.domain.repository

import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetails
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmote
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmoteQueryResult
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmoteSearchHistoryItem
import com.thenamlit.emotesonmymind.type.EmoteSearchFilter
import com.thenamlit.emotesonmymind.type.ImageFormat
import com.thenamlit.emotesonmymind.type.Sort
import kotlinx.coroutines.flow.Flow


interface EmoteRepository {
    suspend fun getMainFeedEmotes(
        query: String,
        page: Int,
        limit: Int,
        sort: Sort,
        formats: List<ImageFormat>,
        filter: EmoteSearchFilter,
    ): Resource<List<MainFeedEmote>>

    fun getMainFeedEmoteSearchHistory(): Flow<List<MainFeedEmoteSearchHistoryItem>>

    suspend fun getMainFeedEmoteSearchHistoryItemByValue(
        value: String,
    ): Resource<MainFeedEmoteSearchHistoryItem>

    suspend fun addMainFeedEmoteSearchHistoryItem(
        mainFeedEmoteSearchHistoryItem: MainFeedEmoteSearchHistoryItem,
    ): SimpleResource

    suspend fun deleteMainFeedEmoteSearchHistoryItemById(id: String): SimpleResource

    suspend fun getEmoteDetails(
        emoteId: String,
        formats: List<ImageFormat>,
    ): Resource<EmoteDetails>

    suspend fun updateLastRequestedToNow(id: String): SimpleResource
}

package com.thenamlit.emotesonmymind.features.emotes.domain.use_case.main_feed

import android.util.Log
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmote
import com.thenamlit.emotesonmymind.features.emotes.domain.repository.EmoteRepository
import com.thenamlit.emotesonmymind.type.EmoteSearchFilter
import com.thenamlit.emotesonmymind.type.ImageFormat
import com.thenamlit.emotesonmymind.type.Sort
import javax.inject.Inject


class GetMainFeedEmotesUseCase @Inject constructor(
    private val emoteRepository: EmoteRepository,
) {
    private val tag = Logging.loggingPrefix + GetMainFeedEmotesUseCase::class.java.simpleName

    suspend operator fun invoke(
        query: String,
        page: Int,
        limit: Int,
        sort: Sort,
        formats: List<ImageFormat>,
        filter: EmoteSearchFilter,
    ): Resource<List<MainFeedEmote>> {
        Log.d(
            tag,
            "invoke | query: $query, " +
                    "page: $page, " +
                    "limit: $limit, " +
                    "sort: $sort, " +
                    "formats: $formats, " +
                    "filter: $filter"
        )

        return emoteRepository.getMainFeedEmotes(
            query = query,
            page = page,
            limit = limit,
            sort = sort,
            formats = formats,
            filter = filter,
        )
    }
}

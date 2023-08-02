package com.thenamlit.emotesonmymind.features.emotes.data.repository

import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.thenamlit.emotesonmymind.EmoteDetailsQuery
import com.thenamlit.emotesonmymind.MainFeedEmoteQuery
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import com.thenamlit.emotesonmymind.core.util.UiText
import com.thenamlit.emotesonmymind.features.emotes.data.local.dao.MainFeedEmoteSearchHistoryDao
import com.thenamlit.emotesonmymind.features.emotes.data.local.schema.MainFeedEmoteSearchHistoryItemSchema
import com.thenamlit.emotesonmymind.features.emotes.domain.mapper.MainFeedEmoteSearchHistoryItemMapper
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetails
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetailsHost
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetailsHostFile
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetailsOwner
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmote
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmoteItemHost
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmoteItemHostFile
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmoteQueryResult
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmoteSearchHistoryItem
import com.thenamlit.emotesonmymind.features.emotes.domain.repository.EmoteRepository
import com.thenamlit.emotesonmymind.type.EmoteSearchFilter
import com.thenamlit.emotesonmymind.type.ImageFormat
import com.thenamlit.emotesonmymind.type.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import javax.inject.Inject


class EmoteRepositoryImpl @Inject constructor(
    private val apolloClient: ApolloClient,
    private val mainFeedEmoteSearchHistoryDao: MainFeedEmoteSearchHistoryDao,
) : EmoteRepository {
    private val tag = Logging.loggingPrefix + EmoteRepositoryImpl::class.java.simpleName

    override suspend fun getMainFeedEmotes(
        query: String,
        page: Int,
        limit: Int,
        sort: Sort,
        formats: List<ImageFormat>,
        filter: EmoteSearchFilter,
    ): Resource<List<MainFeedEmote>> {
        Log.d(
            tag,
            "getMainFeedEmotes | query: $query, " +
                    "page: $page, " +
                    "limit: $limit, " +
                    "sort: $sort, " +
                    "formats: $formats, " +
                    "filter: $filter"
        )

        try {
            val mainFeedEmoteSearchQueryResponse = apolloClient.query(
                MainFeedEmoteQuery(
                    query = query,
                    page = Optional.present(value = page),
                    limit = Optional.present(value = limit),
                    sort = Optional.present(value = sort),
                    formats = Optional.present(value = formats),
                    filter = Optional.present(value = filter)
                )
            ).execute()

            mainFeedEmoteSearchQueryResponse.data?.let { data: MainFeedEmoteQuery.Data ->
                Log.d(tag, "getMainFeedEmotes | Success $data")

                val emotes: MutableList<MainFeedEmote> = mutableListOf()

                data.emotes.items.forEach { itemResult: MainFeedEmoteQuery.Item? ->
                    itemResult?.let { item: MainFeedEmoteQuery.Item ->
                        val mainFeedEmote = MainFeedEmote(
                            id = item.id.toString(),
                            name = item.name,
                            createdAt = convertEmoteDateStringToLong(date = item.created_at.toString()),
                            listed = item.listed,
                            personalUse = item.personal_use,
                            lifecycle = item.lifecycle,
                            animated = item.animated,
                            host = MainFeedEmoteItemHost(
                                url = "https:${item.host.url}",
                                defaultFileName = item.host.files.last().name,
                                files = item.host.files.map { file: MainFeedEmoteQuery.File ->
                                    MainFeedEmoteItemHostFile(
                                        name = file.name,
                                        width = file.width,
                                        height = file.height,
                                        size = file.size,
                                        frameCount = file.frame_count,
                                        format = file.format.name
                                    )
                                },
                            ),
                        )

                        emotes.add(element = mainFeedEmote)
                    } ?: kotlin.run {
                        Log.e(tag, "getMainFeedEmotes | MainFeedEmoteSearchQuery.Item is undefined")
                        // TODO
                    }
                }

                val mainFeedEmoteSearchResult = MainFeedEmoteQueryResult(
                    count = data.emotes.count,
                    emotes = emotes
                )

                return Resource.Success(data = mainFeedEmoteSearchResult.emotes)
            } ?: kotlin.run {
                return Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "Failed to fetch 7TV Emotes"
                    ),
                    logging = "getMainFeedEmotes | MainFeedEmoteSearchQuery.Data is undefined"
                )
            }
        } catch (exception: ApolloException) {
            return Resource.Error(
                uiText = UiText.DynamicString(
                    value = "Failed to fetch 7TV Emotes"
                ),
                logging = "getMainFeedEmotes | ${exception.message}"
            )
        }
    }

    override fun getMainFeedEmoteSearchHistory(): Flow<List<MainFeedEmoteSearchHistoryItem>> {
        Log.d(tag, "getMainFeedEmoteSearchHistory")

        return mainFeedEmoteSearchHistoryDao.getAll()
            .map { mainFeedEmoteSearchHistoryItemSchemaList: List<MainFeedEmoteSearchHistoryItemSchema> ->
                mainFeedEmoteSearchHistoryItemSchemaList
                    .map { mainFeedEmoteSearchHistoryItemSchema: MainFeedEmoteSearchHistoryItemSchema ->
                        MainFeedEmoteSearchHistoryItemMapper.schemaToModel(
                            mainFeedEmoteSearchHistoryItemSchema = mainFeedEmoteSearchHistoryItemSchema
                        )
                    }
            }
    }

    override suspend fun getMainFeedEmoteSearchHistoryItemByValue(
        value: String,
    ): Resource<MainFeedEmoteSearchHistoryItem> {
        Log.d(tag, "getMainFeedEmoteSearchHistoryItemByValue | value: $value")

        mainFeedEmoteSearchHistoryDao.getByValue(value = value)
            ?.let { mainFeedEmoteSearchHistoryItemSchema: MainFeedEmoteSearchHistoryItemSchema ->
                return Resource.Success(
                    data = MainFeedEmoteSearchHistoryItemMapper.schemaToModel(
                        mainFeedEmoteSearchHistoryItemSchema = mainFeedEmoteSearchHistoryItemSchema
                    )
                )
            } ?: kotlin.run {
            return Resource.Error(
                uiText = UiText.StringResource(
                    id = R.string.emote_repository_failed_to_get_main_feed_emote_search_history_item
                ),
                logging = "getMainFeedEmoteSearchHistoryItemByValue | Failed to fetch MainFeedEmoteSearchHistoryItem"
            )
        }
    }

    override suspend fun addMainFeedEmoteSearchHistoryItem(
        mainFeedEmoteSearchHistoryItem: MainFeedEmoteSearchHistoryItem,
    ): SimpleResource {
        Log.d(
            tag,
            "addMainFeedEmoteSearchHistoryItem | mainFeedEmoteSearchHistoryItem: $mainFeedEmoteSearchHistoryItem"
        )

        return mainFeedEmoteSearchHistoryDao.insert(
            mainFeedEmoteSearchHistoryItemSchema =
            MainFeedEmoteSearchHistoryItemMapper.modelToSchema(
                mainFeedEmoteSearchHistoryItem = mainFeedEmoteSearchHistoryItem
            )
        )
    }

    override suspend fun deleteMainFeedEmoteSearchHistoryItemById(id: String): SimpleResource {
        Log.d(tag, "deleteMainFeedEmoteSearchHistoryItemById | id: $id")

        return mainFeedEmoteSearchHistoryDao.deleteById(id = id)
    }

    override suspend fun getEmoteDetails(
        emoteId: String,
        formats: List<ImageFormat>,
    ): Resource<EmoteDetails> {
        Log.d(tag, "getEmoteDetails | emoteId: $emoteId, formats: $formats")

        try {
            val emoteDetailsQueryResult = apolloClient.query(
                EmoteDetailsQuery(
                    emoteId = emoteId,
                    formats = Optional.present(formats)
                )
            ).execute()

            emoteDetailsQueryResult.data?.let { data: EmoteDetailsQuery.Data ->
                Log.d(tag, "getEmoteDetails | Success $data")

                data.emote?.let { emote: EmoteDetailsQuery.Emote ->
                    emote.owner?.let { emoteOwner: EmoteDetailsQuery.Owner ->
                        val emoteDetails = EmoteDetails(
                            id = emote.id as String,
                            name = emote.name,
                            createdAt = convertEmoteDateStringToLong(date = emote.created_at as String),
                            listed = emote.listed,
                            personalUse = emote.personal_use,
                            animated = emote.animated,
                            trending = emote.trending,
                            lifecycle = emote.lifecycle,
                            host = EmoteDetailsHost(
                                url = "https:${emote.host.url}",
                                defaultFileName = emote.host.files.last().name,
                                files = emote.host.files.map { emoteDetailsQueryFile ->
                                    EmoteDetailsHostFile(
                                        name = emoteDetailsQueryFile.name,
                                        width = emoteDetailsQueryFile.width,
                                        height = emoteDetailsQueryFile.height,
                                        size = emoteDetailsQueryFile.size,
                                        frameCount = emoteDetailsQueryFile.frame_count,
                                        format = emoteDetailsQueryFile.format
                                    )
                                }
                            ),
                            owner = EmoteDetailsOwner(
                                id = emoteOwner.id as String,
                                username = emoteOwner.username,
                                displayName = emoteOwner.display_name,
                                avatarUrl = "https:${emoteOwner.avatar_url}",
                            ),
                            tags = emote.tags
                        )

                        return Resource.Success(data = emoteDetails)
                    } ?: kotlin.run {
                        return Resource.Error(
                            uiText = UiText.StringResource(
                                id = R.string.emote_repository_failed_to_fetch_7tv_emote_details
                            ),
                            logging = "getEmoteDetails | EmoteDetailsQuery.Owner is undefined"
                        )
                    }
                } ?: kotlin.run {
                    return Resource.Error(
                        uiText = UiText.StringResource(
                            id = R.string.emote_repository_failed_to_fetch_7tv_emote_details
                        ),
                        logging = "getEmoteDetails | EmoteDetailsQuery.Emote is undefined"
                    )
                }
            } ?: kotlin.run {
                return Resource.Error(
                    uiText = UiText.StringResource(
                        id = R.string.emote_repository_failed_to_fetch_7tv_emote_details
                    ),
                    logging = "getEmoteDetails | MainFeedEmoteSearchQuery.Data is undefined"
                )
            }
        } catch (exception: ApolloException) {
            return Resource.Error(
                uiText = UiText.StringResource(
                    id = R.string.emote_repository_failed_to_fetch_7tv_emote_details
                ),
                logging = "getEmoteDetails | ${exception.message}\n${exception.stackTrace}"
            )
        }
    }

    override suspend fun updateLastRequestedToNow(id: String): SimpleResource {
        Log.d(tag, "updateLastRequestedToNow | id: $id")

        return mainFeedEmoteSearchHistoryDao.updateLastRequestedToNow(id = id)
    }

    private fun convertEmoteDateStringToLong(date: String): Long {
        Log.d(tag, "convertEmoteDateStringToLong | date: $date")

        return try {
            ZonedDateTime.parse(date).toEpochSecond()
        } catch (e: DateTimeParseException) {
            Log.e(tag, "convertEmoteDateStringToLong | ${e.message}\n${e.stackTrace}")
            0L
        }
    }
}

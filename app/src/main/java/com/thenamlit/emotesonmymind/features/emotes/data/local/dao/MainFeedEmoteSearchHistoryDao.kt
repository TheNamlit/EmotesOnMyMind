package com.thenamlit.emotesonmymind.features.emotes.data.local.dao

import android.util.Log
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import com.thenamlit.emotesonmymind.core.util.UiText
import com.thenamlit.emotesonmymind.features.emotes.data.local.schema.MainFeedEmoteSearchHistoryItemSchema
import com.thenamlit.emotesonmymind.features.emotes.util.MainFeedSettings
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import javax.inject.Inject


interface MainFeedEmoteSearchHistoryDao {
    fun getAll(): Flow<List<MainFeedEmoteSearchHistoryItemSchema>>
    suspend fun getByValue(value: String): MainFeedEmoteSearchHistoryItemSchema?

    suspend fun updateLastRequestedToNow(id: String): SimpleResource

    suspend fun insert(mainFeedEmoteSearchHistoryItemSchema: MainFeedEmoteSearchHistoryItemSchema): SimpleResource

    suspend fun deleteById(id: String): SimpleResource
}

class MainFeedEmoteSearchHistoryDaoImpl @Inject constructor(
    private val realm: Realm,
) : MainFeedEmoteSearchHistoryDao {
    private val tag =
        Logging.loggingPrefix + MainFeedEmoteSearchHistoryDaoImpl::class.java.simpleName

    override fun getAll(): Flow<List<MainFeedEmoteSearchHistoryItemSchema>> {
        Log.d(tag, "getAll")

        return realm
            .query<MainFeedEmoteSearchHistoryItemSchema>()
            .sort(property = "lastRequested", sortOrder = Sort.DESCENDING)
            .limit(limit = MainFeedSettings.limit)
            .asFlow()
            .map { value: ResultsChange<MainFeedEmoteSearchHistoryItemSchema> -> value.list }
    }

    override suspend fun getByValue(value: String): MainFeedEmoteSearchHistoryItemSchema? {
        Log.d(tag, "getByValue | value: $value")

        return realm
            .query<MainFeedEmoteSearchHistoryItemSchema>("value == $0", value)
            .first()
            .find()
    }

    override suspend fun updateLastRequestedToNow(id: String): SimpleResource {
        Log.d(tag, "updateLastRequestedToNow | id: $id")

        return realm.write {
            return@write try {
                val mainFeedEmoteSearchHistoryItem: MainFeedEmoteSearchHistoryItemSchema? = this
                    .query<MainFeedEmoteSearchHistoryItemSchema>("id == $0", ObjectId(id))
                    .first()
                    .find()
                mainFeedEmoteSearchHistoryItem?.lastRequested = RealmInstant.now()
                Resource.Success(data = null)
            } catch (exception: IllegalArgumentException) {
                Resource.Error(
//                    uiText = UiText.StringResource(
//                        id = R.string.main_feed_emote_search_history_dao_update_last_requested_error
//                    ),
                    logging = "Failed to update MainFeedEmotesSearchHistorySchema.lastRequested: " +
                            "${exception.message}\n${exception.stackTrace}"
                )
            }
        }
    }

    override suspend fun insert(
        mainFeedEmoteSearchHistoryItemSchema: MainFeedEmoteSearchHistoryItemSchema,
    ): SimpleResource {
        Log.d(
            tag,
            "insert | mainFeedEmotesSearchHistoryItemSchema: $mainFeedEmoteSearchHistoryItemSchema"
        )

        return realm.write {
            return@write try {
                copyToRealm(instance = mainFeedEmoteSearchHistoryItemSchema)
                Resource.Success(data = null)
            } catch (exception: IllegalArgumentException) {
                Resource.Error(
                    uiText = UiText.StringResource(
                        id = R.string.main_feed_emote_search_history_dao_insert_error
                    ),
                    logging = "Failed to insert MainFeedEmotesSearchHistorySchema: " +
                            "${exception.message}\n${exception.stackTrace}"
                )
            }
        }
    }

    override suspend fun deleteById(id: String): SimpleResource {
        Log.d(tag, "deleteById | id: $id")

        return realm.write {
            val emote =
                query<MainFeedEmoteSearchHistoryItemSchema>(
                    query = "id == $0",
                    ObjectId(id)
                ).first().find()

            return@write try {
                emote?.let {
                    delete(it)
                    Resource.Success(data = null)
                } ?: kotlin.run {
                    Resource.Error(
                        uiText = UiText.StringResource(
                            id = R.string.main_feed_emote_search_history_dao_delete_by_id_error
                        ),
                        logging = "Couldn't find mainFeedEmotesSearchHistoryItemSchema to delete"
                    )
                }
            } catch (exception: IllegalArgumentException) {
                Resource.Error(
                    uiText = UiText.StringResource(
                        id = R.string.main_feed_emote_search_history_dao_delete_by_id_error
                    ),
                    logging = "Failed to delete mainFeedEmotesSearchHistoryItemSchema:" +
                            "${exception.message}\n${exception.stackTrace}"
                )
            }
        }
    }
}

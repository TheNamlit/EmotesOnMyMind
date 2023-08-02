package com.thenamlit.emotesonmymind.features.sticker.data.local.dao

import android.util.Log
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import com.thenamlit.emotesonmymind.core.util.UiText
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerSchema
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import javax.inject.Inject


// Tutorial: https://www.youtube.com/watch?v=7ukzzjtUCJA

interface StickerDao {
    fun getAll(): Flow<List<StickerSchema>>
    fun getAllAnimated(): Flow<List<StickerSchema>>
    fun getAllNotAnimated(): Flow<List<StickerSchema>>
    fun getAllNotInIdList(idList: List<String>, animated: Boolean): Flow<List<StickerSchema>>
    suspend fun getById(stickerId: String): Resource<StickerSchema>
    suspend fun getStickerByRemoteEmoteId(emoteId: String): Resource<StickerSchema>
    suspend fun insert(stickerSchema: StickerSchema): SimpleResource
    suspend fun deleteById(id: String): SimpleResource
}

class StickerDaoImpl @Inject constructor(
    private val realm: Realm,
) : StickerDao {
    private val tag = Logging.loggingPrefix + StickerDaoImpl::class.java.simpleName

    // https://www.mongodb.com/docs/realm/sdk/kotlin/realm-database/crud/read/
    override fun getAll(): Flow<List<StickerSchema>> {
        Log.d(tag, "getAll")

        return realm
            .query<StickerSchema>()
            .asFlow()
            .map { value: ResultsChange<StickerSchema> -> value.list }
    }

    override fun getAllAnimated(): Flow<List<StickerSchema>> {
        Log.d(tag, "getAllAnimated")

        return realm
            .query<StickerSchema>("stickerImageData.animated == $0", true)
            .asFlow()
            .map { value: ResultsChange<StickerSchema> -> value.list }
    }

    override fun getAllNotAnimated(): Flow<List<StickerSchema>> {
        Log.d(tag, "getAllNotAnimated")

        return realm
            .query<StickerSchema>("stickerImageData.animated == $0", false)
            .asFlow()
            .map { value: ResultsChange<StickerSchema> -> value.list }
    }

    override fun getAllNotInIdList(
        idList: List<String>,
        animated: Boolean,
    ): Flow<List<StickerSchema>> {
        Log.d(tag, "getAllNotInIdList | idList: $idList, animated: $animated")

        return realm
            .query<StickerSchema>(
                "NOT id IN $0 && stickerImageData.animated == $1",
                idList.map { id: String -> ObjectId(id) },
                animated
            )
            .asFlow()
            .map { value: ResultsChange<StickerSchema> -> value.list }
    }

    override suspend fun getById(stickerId: String): Resource<StickerSchema> {
        Log.d(tag, "getById | stickerId: $stickerId")

        val stickerSchema: StickerSchema? = realm
            .query<StickerSchema>("id == $0", ObjectId(hexString = stickerId))
            .first()
            .find()

        return stickerSchema?.let {
            Resource.Success(data = it)
        } ?: kotlin.run {
            Resource.Error(
                uiText = UiText.DynamicString(
                    value = "getById | Couldn't find Sticker by Id"
                )
            )
        }
    }

    // https://www.mongodb.com/docs/realm/sdk/kotlin/realm-database/crud/read/#find-object-by-primary-key
    override suspend fun getStickerByRemoteEmoteId(emoteId: String): Resource<StickerSchema> {
        Log.d(tag, "getStickerByRemoteEmoteId | emoteId: $emoteId")

        val stickerSchema: StickerSchema? = realm
            .query<StickerSchema>("remoteEmoteData.id == $0", emoteId)
            .first()
            .find()

        return stickerSchema?.let {
            Resource.Success(data = it)
        } ?: kotlin.run {
            Resource.Error(
                uiText = UiText.DynamicString(
                    value = "getStickerByRemoteEmoteId | Couldn't find Sticker by RemoteEmoteId"
                )
            )
        }
    }

    // https://www.mongodb.com/docs/realm/sdk/kotlin/realm-database/crud/create/
    override suspend fun insert(stickerSchema: StickerSchema): SimpleResource {
        Log.d(tag, "insert | stickerSchema: $stickerSchema")

        return realm.write {
            return@write try {
                copyToRealm(instance = stickerSchema)
                Resource.Success(data = null)
            } catch (exception: IllegalArgumentException) {
                Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "Failed to insert Sticker: ${exception.message}\n${exception.stackTrace}"
                    )
                )
            }
        }
    }

    // https://www.mongodb.com/docs/realm/sdk/kotlin/realm-database/crud/delete/
    override suspend fun deleteById(id: String): SimpleResource {
        Log.d(tag, "deleteById | id: $id")

        return realm.write {
            val emote = query<StickerSchema>(query = "id == $0", ObjectId(id)).first().find()

            return@write try {
                emote?.let {
                    delete(it)
                    Resource.Success(data = null)
                } ?: kotlin.run {
                    Resource.Error(uiText = UiText.DynamicString(value = "Couldn't find Sticker to delete"))
                }
            } catch (exception: IllegalArgumentException) {
                Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "Failed to delete Sticker: ${exception.message}\n${exception.stackTrace}"
                    )
                )
            }
        }
    }
}

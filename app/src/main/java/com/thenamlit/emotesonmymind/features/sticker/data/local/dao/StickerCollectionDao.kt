package com.thenamlit.emotesonmymind.features.sticker.data.local.dao

import android.util.Log
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import com.thenamlit.emotesonmymind.core.util.UiText
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerCollectionSchema
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerSchema
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import javax.inject.Inject


interface StickerCollectionDao {
    fun getAll(): Flow<List<StickerCollectionSchema>>
    fun getAllAnimated(): Flow<List<StickerCollectionSchema>>
    fun getAllNotAnimated(): Flow<List<StickerCollectionSchema>>

    fun getByIdSynchronized(id: String): StickerCollectionSchema?
    fun getAllSynchronized(): List<StickerCollectionSchema>

    fun getAllCollectionsOfSticker(
        stickerId: String,
        animated: Boolean,
    ): Flow<List<StickerCollectionSchema>>

    fun getAllCollectionsOfStickerSynchronized(
        stickerId: String,
        animated: Boolean,
    ): List<StickerCollectionSchema>

    fun getAllSelectedCollectionsOfSticker(
        stickerId: String,
        animated: Boolean,
    ): Flow<List<StickerCollectionSchema>>

    fun getAllNotSelectedCollectionsOfSticker(
        stickerId: String,
        animated: Boolean,
    ): Flow<List<StickerCollectionSchema>>

    fun getCollectionById(collectionId: String): StickerCollectionSchema?

    suspend fun insert(stickerCollectionSchema: StickerCollectionSchema): SimpleResource
    suspend fun addStickerToCollection(
        stickerCollectionId: String,
        stickerId: String,
    ): SimpleResource

    suspend fun addStickerListToCollection(
        stickerCollectionId: String,
        stickerIds: List<String>,
    ): SimpleResource

    suspend fun removeStickerFromCollection(
        stickerCollectionId: String,
        stickerId: String,
    ): SimpleResource

    suspend fun removeStickerFromEveryCollection(
        stickerId: String,
        animated: Boolean,
    ): Resource<List<String>>

    suspend fun deleteById(id: String): SimpleResource

    suspend fun updateName(id: String, name: String): SimpleResource
}

class StickerCollectionDaoImpl @Inject constructor(
    private val realm: Realm,
) : StickerCollectionDao {
    private val tag = Logging.loggingPrefix + StickerCollectionDaoImpl::class.java.simpleName

    override fun getAll(): Flow<List<StickerCollectionSchema>> {
        Log.d(tag, "getAll")

        return realm
            .query<StickerCollectionSchema>()
            .asFlow()
            .map { value: ResultsChange<StickerCollectionSchema> -> value.list }
    }

    override fun getAllAnimated(): Flow<List<StickerCollectionSchema>> {
        Log.d(tag, "getAllAnimated")

        return realm
            .query<StickerCollectionSchema>("animated == $0", true)
            .asFlow()
            .map { value: ResultsChange<StickerCollectionSchema> -> value.list }
    }

    override fun getAllNotAnimated(): Flow<List<StickerCollectionSchema>> {
        Log.d(tag, "getAllNotAnimated")

        return realm
            .query<StickerCollectionSchema>("animated == $0", false)
            .asFlow()
            .map { value: ResultsChange<StickerCollectionSchema> -> value.list }
    }

    override fun getByIdSynchronized(id: String): StickerCollectionSchema? {
        Log.d(tag, "getByIdSynchronized | id: $id")

        return realm.query<StickerCollectionSchema>("id == $0", id).first().find()
    }

    override fun getAllSynchronized(): List<StickerCollectionSchema> {
        Log.d(tag, "getAllSynchronized")

        return realm.query<StickerCollectionSchema>().find().toList()
    }

    override fun getAllCollectionsOfSticker(
        stickerId: String,
        animated: Boolean,
    ): Flow<List<StickerCollectionSchema>> {
        Log.d(tag, "getAllCollectionsOfSticker | stickerId: $stickerId, animated: $animated")

        return realm
            .query<StickerCollectionSchema>(
                "$0 in stickerIds && animated == $1",
                stickerId,
                animated
            )
            .asFlow()
            .map { value: ResultsChange<StickerCollectionSchema> -> value.list }
    }

    override fun getAllCollectionsOfStickerSynchronized(
        stickerId: String,
        animated: Boolean,
    ): List<StickerCollectionSchema> {
        Log.d(
            tag, "getAllCollectionsOfStickerSynchronized | stickerId: $stickerId, " +
                    "animated: $animated"
        )

        val stickerSchemaRealm =
            realm.query<StickerSchema>("id == $0", ObjectId(hexString = stickerId)).first().find()

        return stickerSchemaRealm?.let {
            realm
                .query<StickerCollectionSchema>("$0 in stickers && animated == $1", it, animated)
                .find()
                .toList()
        } ?: kotlin.run {
            emptyList()
        }
    }

    override fun getAllSelectedCollectionsOfSticker(
        stickerId: String,
        animated: Boolean,
    ): Flow<List<StickerCollectionSchema>> {
        Log.d(
            tag,
            "getAllSelectedCollectionsOfSticker | stickerId: $stickerId, animated: $animated"
        )

        val stickerSchemaRealm =
            realm.query<StickerSchema>("id == $0", ObjectId(hexString = stickerId)).first().find()

        return stickerSchemaRealm?.let {
            realm
                .query<StickerCollectionSchema>("$0 in stickers && animated == $1", it, animated)
                .asFlow()
                .map { value: ResultsChange<StickerCollectionSchema> -> value.list }
        } ?: kotlin.run {
            emptyFlow()
        }
    }

    override fun getAllNotSelectedCollectionsOfSticker(
        stickerId: String,
        animated: Boolean,
    ): Flow<List<StickerCollectionSchema>> {
        Log.d(
            tag,
            "getAllNotSelectedCollectionsOfSticker | stickerId: $stickerId, animated: $animated"
        )


        val stickerSchemaRealm =
            realm.query<StickerSchema>("id == $0", ObjectId(hexString = stickerId)).first().find()

        return stickerSchemaRealm?.let {
            realm
                .query<StickerCollectionSchema>(
                    "not $0 in stickers && animated == $1",
                    it,
                    animated
                )
                .asFlow()
                .map { value: ResultsChange<StickerCollectionSchema> -> value.list }
        } ?: kotlin.run {
            emptyFlow()
        }
    }

    override fun getCollectionById(collectionId: String): StickerCollectionSchema? {
        Log.d(tag, "getAllStickersInCollection | collectionId: $collectionId")

        return realm
            .query<StickerCollectionSchema>("id == $0", ObjectId(hexString = collectionId))
            .first()
            .find()
    }

    override suspend fun insert(stickerCollectionSchema: StickerCollectionSchema): SimpleResource {
        Log.d(tag, "insert | stickerCollectionSchema: $stickerCollectionSchema")

        return realm.write {
            return@write try {
                copyToRealm(instance = stickerCollectionSchema)
                Resource.Success(data = null)
            } catch (exception: IllegalArgumentException) {
                Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "Failed to insert StickerCollection: ${exception.message}\n${exception.stackTrace}"
                    )
                )
            }
        }
    }

    // https://www.mongodb.com/docs/realm/sdk/kotlin/realm-database/crud/update/#update-a-collection
    override suspend fun addStickerToCollection(
        stickerCollectionId: String,
        stickerId: String,
    ): SimpleResource {
        Log.d(
            tag,
            "addStickerToCollection | stickerCollectionId: $stickerCollectionId, " +
                    "stickerId: $stickerId"
        )

        val stickerCollectionSchemaResult: StickerCollectionSchema? = realm
            .query<StickerCollectionSchema>("id == $0", ObjectId(stickerCollectionId))
            .first()
            .find()

        stickerCollectionSchemaResult?.let { stickerCollectionSchema: StickerCollectionSchema ->
            val stickerSchemaRealm: StickerSchema? =
                realm.query<StickerSchema>("id == $0", ObjectId(hexString = stickerId)).first()
                    .find()

            return stickerSchemaRealm?.let { stickerSchema: StickerSchema ->
                return realm.write {
                    return@write try {
                        findLatest(stickerSchema)?.let { latestStickerSchema: StickerSchema ->
                            findLatest(stickerCollectionSchema)?.let { latestStickerCollectionSchema: StickerCollectionSchema ->
                                val addedSticker = latestStickerCollectionSchema.stickers.add(
                                    element = latestStickerSchema
                                )

                                if (addedSticker) {
                                    Resource.Success(data = null)
                                } else {
                                    Resource.Error(
                                        uiText = UiText.DynamicString(
                                            value = "Could not add Sticker " +
                                                    "${latestStickerSchema.id} to StickerCollection " +
                                                    "${latestStickerCollectionSchema.stickers.map { it.id }}"
                                        )
                                    )
                                }
                            } ?: kotlin.run {
                                Resource.Error(
                                    uiText = UiText.DynamicString(
                                        value = "Could not find latest StickerCollectionSchema"
                                    )
                                )
                            }
                        } ?: kotlin.run {
                            Resource.Error(
                                uiText = UiText.DynamicString(
                                    value = "Could not find latest StickerSchema"
                                )
                            )
                        }
                    } catch (exception: IllegalArgumentException) {
                        Resource.Error(
                            uiText = UiText.DynamicString(
                                value = "addStickerToCollection | Exception: " +
                                        "${exception.message}\n${exception.stackTrace}"
                            )
                        )
                    }
                }
            } ?: kotlin.run { Resource.Error(uiText = UiText.unknownError()) }

        } ?: kotlin.run {
            return Resource.Error(
                uiText = UiText.DynamicString(
                    value = "addStickerToCollection | StickerCollectionSchema is undefined"
                )
            )
        }
    }

    override suspend fun addStickerListToCollection(
        stickerCollectionId: String,
        stickerIds: List<String>,
    ): SimpleResource {
        Log.d(
            tag,
            "addStickerListToCollection | stickerCollectionId: $stickerCollectionId, " +
                    "stickerIds: $stickerIds"
        )

        val stickerCollectionSchemaResult: StickerCollectionSchema? = realm
            .query<StickerCollectionSchema>("id == $0", ObjectId(stickerCollectionId))
            .first()
            .find()

        stickerCollectionSchemaResult?.let { stickerCollectionSchema: StickerCollectionSchema ->
            return realm.write {
                val stickerSchemaList: List<StickerSchema> =
                    this.query<StickerSchema>("id IN $0", stickerIds.map { ObjectId(it) }).find()

                return@write try {
                    findLatest(stickerCollectionSchema)?.let { latestStickerCollectionSchema: StickerCollectionSchema ->
                        val addedStickerList =
                            latestStickerCollectionSchema.stickers.addAll(stickerSchemaList)

                        if (addedStickerList) {
                            Resource.Success(data = null)
                        } else {
                            Resource.Error(
                                uiText = UiText.DynamicString(
                                    value = "addStickerListToCollection | Could not add StickerList " +
                                            "to StickerCollection " +
                                            "${latestStickerCollectionSchema.stickers.map { it.id }}"
                                )
                            )
                        }
                    } ?: kotlin.run {
                        Resource.Error(
                            uiText = UiText.DynamicString(
                                value = "addStickerListToCollection | Could not find latest " +
                                        "StickerCollectionSchema"
                            )
                        )
                    }
                } catch (exception: IllegalArgumentException) {
                    Resource.Error(
                        uiText = UiText.DynamicString(
                            value = "addStickerListToCollection | Exception: " +
                                    "${exception.message}\n${exception.stackTrace}"
                        ),
                        logging = "addStickerListToCollection"
                    )
                }
            }
        } ?: kotlin.run {
            return Resource.Error(
                uiText = UiText.DynamicString(
                    value = "addStickerListToCollection | StickerCollectionSchema is undefined"
                )
            )
        }
    }

    override suspend fun removeStickerFromCollection(
        stickerCollectionId: String,
        stickerId: String,
    ): SimpleResource {
        Log.d(
            tag,
            "removeStickerFromCollection | stickerCollectionId: $stickerCollectionId, " +
                    "stickerId: $stickerId"
        )

        val stickerCollectionSchemaResult: StickerCollectionSchema? = realm
            .query<StickerCollectionSchema>("id == $0", ObjectId(stickerCollectionId))
            .first()
            .find()

        stickerCollectionSchemaResult?.let { stickerCollectionSchema: StickerCollectionSchema ->
            val stickerSchemaRealm: StickerSchema? =
                realm.query<StickerSchema>("id == $0", ObjectId(hexString = stickerId)).first()
                    .find()
            stickerSchemaRealm?.let { stickerSchema: StickerSchema ->
                return realm.write {
                    return@write try {
                        findLatest(stickerSchema)?.let { latestStickerSchema: StickerSchema ->
                            findLatest(stickerCollectionSchema)?.let { latestStickerCollectionSchema: StickerCollectionSchema ->
                                // Can't just use the regular remove()-Function here...
                                // Probably because of it being an Object
                                val removedSticker = latestStickerCollectionSchema.stickers
                                    .removeIf { removableStickerSchema: StickerSchema ->
                                        removableStickerSchema.id == latestStickerSchema.id
                                    }

                                if (removedSticker) {
                                    Resource.Success(data = null)
                                } else {
                                    Resource.Error(
                                        uiText = UiText.DynamicString(
                                            value = "Could not remove Sticker " +
                                                    "${latestStickerSchema.id} from StickerCollection " +
                                                    "${latestStickerCollectionSchema.stickers.map { it.id }}"
                                        )
                                    )
                                }
                            } ?: kotlin.run {
                                Resource.Error(
                                    uiText = UiText.DynamicString(
                                        value = "Could not find latest StickerCollectionSchema"
                                    )
                                )
                            }
                        } ?: kotlin.run {
                            Resource.Error(
                                uiText = UiText.DynamicString(
                                    value = "Could not find latest StickerSchema"
                                )
                            )
                        }
                    } catch (exception: IllegalArgumentException) {
                        Resource.Error(
                            uiText = UiText.DynamicString(
                                value = "removeStickerFromCollection | Exception: " +
                                        "${exception.message}\n${exception.stackTrace}"
                            )
                        )
                    }
                }
            }
        } ?: kotlin.run {
            return Resource.Error(
                uiText = UiText.DynamicString(
                    value = "removeStickerFromCollection | StickerCollectionSchema is undefined"
                )
            )
        }
    }

    override suspend fun removeStickerFromEveryCollection(
        stickerId: String,
        animated: Boolean,
    ): Resource<List<String>> = withContext(Dispatchers.IO) {
        Log.d(
            tag, "removeStickerFromEveryCollection | stickerId: $stickerId, " +
                    "animated: $animated"
        )

        val removedFromCollections: MutableList<String> = mutableListOf()

        getAllCollectionsOfStickerSynchronized(
            stickerId = stickerId,
            animated = animated
        ).forEach { stickerCollectionSchema: StickerCollectionSchema ->
            val removedStickerFromCollectionResult = removeStickerFromCollection(
                stickerCollectionId = stickerCollectionSchema.id.toHexString(),
                stickerId = stickerId
            )

            when (removedStickerFromCollectionResult) {
                is Resource.Success -> {
                    removedFromCollections.add(stickerCollectionSchema.id.toHexString())
                }

                is Resource.Error -> {
                    return@withContext Resource.Error(
                        uiText = removedStickerFromCollectionResult.uiText,
                        logging = "removeStickerFromEveryCollection | " +
                                "${removedStickerFromCollectionResult.logging}"
                    )
                }
            }
        }

        return@withContext Resource.Success(data = removedFromCollections)
    }

    override suspend fun deleteById(id: String): SimpleResource {
        Log.d(tag, "deleteById | id: $id")

        return realm.write {
            val stickerCollectionSchemaResult =
                query<StickerCollectionSchema>(query = "id == $0", ObjectId(id)).first().find()

            return@write try {
                stickerCollectionSchemaResult?.let { stickerCollectionSchema: StickerCollectionSchema ->
                    delete(stickerCollectionSchema)
                    Resource.Success(data = null)
                } ?: kotlin.run {
                    Resource.Error(
                        uiText = UiText.DynamicString(
                            value = "Couldn't find StickerCollection to delete"
                        )
                    )
                }
            } catch (exception: IllegalArgumentException) {
                Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "Failed to delete StickerCollection: " +
                                "${exception.message}\n${exception.stackTrace}"
                    )
                )
            }
        }
    }

    // https://www.mongodb.com/docs/realm/sdk/kotlin/realm-database/crud/update/
    override suspend fun updateName(id: String, name: String): SimpleResource {
        Log.d(tag, "updateName: id: $id, name: $name")

        return realm.write {
            val stickerCollectionSchemaResult =
                this.query<StickerCollectionSchema>("id == $0", ObjectId(id)).first().find()

            stickerCollectionSchemaResult?.let { stickerCollectionSchema: StickerCollectionSchema ->
                stickerCollectionSchema.name = name

                return@write Resource.Success(data = null)
            } ?: kotlin.run {
                return@write Resource.Error(
                    uiText = UiText.StringResource(
                        id = R.string.sticker_collection_dao_update_name_error
                    ),
                    logging = "updateName | StickerCollectionSchema is undefined"
                )
            }
        }
    }
}

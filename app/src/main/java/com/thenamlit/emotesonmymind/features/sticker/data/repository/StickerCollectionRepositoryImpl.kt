package com.thenamlit.emotesonmymind.features.sticker.data.repository

import android.util.Log
import com.thenamlit.emotesonmymind.core.domain.models.StickerCollection
import com.thenamlit.emotesonmymind.core.domain.repository.StickerCollectionRepository
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import com.thenamlit.emotesonmymind.core.util.UiText
import com.thenamlit.emotesonmymind.features.sticker.data.local.dao.StickerCollectionDao
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerCollectionSchema
import com.thenamlit.emotesonmymind.features.sticker.domain.mapper.StickerCollectionMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class StickerCollectionRepositoryImpl @Inject constructor(
    private val stickerCollectionDao: StickerCollectionDao,
) : StickerCollectionRepository {
    private val tag = Logging.loggingPrefix + StickerCollectionRepositoryImpl::class.java.simpleName

    override fun getAllStickerCollections(): Flow<List<StickerCollection>> {
        Log.d(tag, "getAllStickerCollections")

        return stickerCollectionDao.getAll()
            .map { stickerCollectionSchemaList: List<StickerCollectionSchema> ->
                stickerCollectionSchemaList.map { stickerCollectionSchema: StickerCollectionSchema ->
                    StickerCollectionMapper.schemaToModel(
                        stickerCollectionSchema = stickerCollectionSchema
                    )
                }
            }
    }

    override fun getAllAnimatedStickerCollections(): Flow<List<StickerCollection>> {
        Log.d(tag, "getAllAnimatedStickerCollections")

        return stickerCollectionDao.getAllAnimated()
            .map { stickerCollectionSchemaList: List<StickerCollectionSchema> ->
                stickerCollectionSchemaList.map { stickerCollectionSchema: StickerCollectionSchema ->
                    StickerCollectionMapper.schemaToModel(
                        stickerCollectionSchema = stickerCollectionSchema
                    )
                }
            }
    }

    override fun getAllNotAnimatedStickerCollections(): Flow<List<StickerCollection>> {
        Log.d(tag, "getAllNotAnimatedStickerCollections")

        return stickerCollectionDao.getAllNotAnimated()
            .map { stickerCollectionSchemaList: List<StickerCollectionSchema> ->
                stickerCollectionSchemaList.map { stickerCollectionSchema: StickerCollectionSchema ->
                    StickerCollectionMapper.schemaToModel(
                        stickerCollectionSchema = stickerCollectionSchema
                    )
                }
            }
    }

    override fun getByIdSynchronized(id: String): Resource<StickerCollection> {
        Log.d(tag, "getByIdSynchronized | id: $id")

        stickerCollectionDao.getByIdSynchronized(id = id)
            ?.let { stickerCollectionSchema: StickerCollectionSchema ->
                return Resource.Success(
                    data = StickerCollectionMapper.schemaToModel(
                        stickerCollectionSchema = stickerCollectionSchema
                    )
                )
            } ?: kotlin.run {
            return Resource.Error(
                uiText = UiText.DynamicString(
                    value = "getByIdSynchronized | StickerCollectionSchema undefined"
                )
            )
        }
    }

    override fun getAllSynchronized(): Resource<List<StickerCollection>> {
        Log.d(tag, "getAllSynchronized")

        return Resource.Success(
            data = stickerCollectionDao.getAllSynchronized()
                .map { stickerCollectionSchema: StickerCollectionSchema ->
                    StickerCollectionMapper.schemaToModel(
                        stickerCollectionSchema = stickerCollectionSchema
                    )
                }
        )
    }

    override fun getAllCollectionsOfSticker(
        stickerId: String,
        animated: Boolean,
    ): Flow<List<StickerCollection>> {
        Log.d(tag, "getAllCollectionsOfSticker | stickerId: $stickerId, animated: $animated")

        return stickerCollectionDao.getAllCollectionsOfSticker(
            stickerId = stickerId,
            animated = animated
        )
            .map { stickerCollectionSchemaList: List<StickerCollectionSchema> ->
                stickerCollectionSchemaList.map { stickerCollectionSchema: StickerCollectionSchema ->
                    StickerCollectionMapper.schemaToModel(
                        stickerCollectionSchema = stickerCollectionSchema
                    )
                }
            }
    }

    override fun getAllSelectedCollectionsOfSticker(
        stickerId: String,
        animated: Boolean,
    ): Flow<List<StickerCollection>> {
        Log.d(
            tag,
            "getAllSelectedCollectionsOfSticker | stickerId: $stickerId, animated: $animated"
        )

        return stickerCollectionDao.getAllSelectedCollectionsOfSticker(
            stickerId = stickerId,
            animated = animated
        )
            .map { stickerCollectionSchemaList: List<StickerCollectionSchema> ->
                stickerCollectionSchemaList.map { stickerCollectionSchema: StickerCollectionSchema ->
                    StickerCollectionMapper.schemaToModel(
                        stickerCollectionSchema = stickerCollectionSchema
                    )
                }
            }
    }

    override fun getAllNotSelectedCollectionsOfSticker(
        stickerId: String,
        animated: Boolean,
    ): Flow<List<StickerCollection>> {
        Log.d(
            tag,
            "getAllNotSelectedCollectionsOfSticker | stickerId: $stickerId, animated: $animated"
        )

        return stickerCollectionDao.getAllNotSelectedCollectionsOfSticker(
            stickerId = stickerId,
            animated = animated
        )
            .map { stickerCollectionSchemaList: List<StickerCollectionSchema> ->
                stickerCollectionSchemaList.map { stickerCollectionSchema: StickerCollectionSchema ->
                    StickerCollectionMapper.schemaToModel(
                        stickerCollectionSchema = stickerCollectionSchema
                    )
                }
            }
    }

    override fun getCollectionById(collectionId: String): Resource<StickerCollection> {
        Log.d(tag, "getCollectionById | collectionId: $collectionId")

        stickerCollectionDao.getCollectionById(collectionId = collectionId)
            ?.let { stickerCollectionSchema: StickerCollectionSchema ->
                return Resource.Success(
                    data = StickerCollectionMapper.schemaToModel(
                        stickerCollectionSchema = stickerCollectionSchema
                    )
                )
            } ?: return Resource.Error(
            uiText = UiText.DynamicString(
                value = "getCollectionById | StickerCollectionSchema is undefined"
            )
        )
    }

    override suspend fun addStickerToCollection(
        stickerCollectionId: String,
        stickerId: String,
    ): SimpleResource {
        Log.d(
            tag,
            "addStickerToCollection | stickerCollectionId: $stickerCollectionId, " +
                    "stickerId: $stickerId"
        )

        return stickerCollectionDao.addStickerToCollection(
            stickerCollectionId = stickerCollectionId,
            stickerId = stickerId
        )
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

        return stickerCollectionDao.addStickerListToCollection(
            stickerCollectionId = stickerCollectionId,
            stickerIds = stickerIds
        )
    }

    override suspend fun removeStickerFromCollection(
        stickerCollectionId: String,
        stickerId: String,
    ): SimpleResource {
        Log.d(
            tag,
            "removeStickerFromCollection | stickerCollectionId: $stickerCollectionId, stickerId: $stickerId"
        )

        return stickerCollectionDao.removeStickerFromCollection(
            stickerCollectionId = stickerCollectionId,
            stickerId = stickerId
        )
    }

    override suspend fun removeStickerFromEveryCollection(
        stickerId: String,
        animated: Boolean,
    ): Resource<List<String>> {
        Log.d(
            tag,
            "removeStickerFromEveryCollection | stickerId: $stickerId, animated: $animated"
        )

        val removedStickerFromEveryCollectionResult =
            stickerCollectionDao.removeStickerFromEveryCollection(
                stickerId = stickerId,
                animated = animated
            )

        return when (removedStickerFromEveryCollectionResult) {
            is Resource.Success -> {
                removedStickerFromEveryCollectionResult
            }

            is Resource.Error -> {
                Resource.Error(
                    uiText = removedStickerFromEveryCollectionResult.uiText,
                    logging = "removeStickerFromEveryCollection  | " +
                            "${removedStickerFromEveryCollectionResult.logging}"
                )
            }
        }
    }

    override suspend fun createStickerCollection(name: String, animated: Boolean): SimpleResource {
        Log.d(tag, "createStickerCollection | name: $name, animated: $animated")

        return stickerCollectionDao.insert(
            stickerCollectionSchema = StickerCollectionMapper.modelToSchema(
                stickerCollection = StickerCollection(name = name, animated = animated)
            )
        )
    }

    override suspend fun deleteById(id: String): SimpleResource {
        Log.d(tag, "deleteById | id: $id")

        return stickerCollectionDao.deleteById(id = id)
    }

    override suspend fun updateName(id: String, name: String): SimpleResource {
        Log.d(tag, "updateName | id: $id, name: $name")

        return stickerCollectionDao.updateName(id = id, name = name)
    }
}

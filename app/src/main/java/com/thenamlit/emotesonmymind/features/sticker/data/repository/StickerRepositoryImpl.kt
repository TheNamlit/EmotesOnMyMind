package com.thenamlit.emotesonmymind.features.sticker.data.repository

import android.util.Log
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.repository.StickerRepository
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import com.thenamlit.emotesonmymind.core.util.UiText
import com.thenamlit.emotesonmymind.features.sticker.data.local.dao.StickerDao
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerSchema
import com.thenamlit.emotesonmymind.features.sticker.domain.mapper.StickerMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class StickerRepositoryImpl @Inject constructor(
    private val stickerDao: StickerDao,
) : StickerRepository {
    private val tag = Logging.loggingPrefix + StickerRepositoryImpl::class.java.simpleName

    override fun getAllSticker(): Flow<List<Sticker>> {
        Log.d(tag, "getAllSticker")

        return stickerDao.getAll().map { stickerSchemaList: List<StickerSchema> ->
            stickerSchemaList.map { stickerSchema: StickerSchema ->
                StickerMapper.schemaToModel(stickerSchema = stickerSchema)
            }
        }
    }

    override fun getAllAnimated(): Flow<List<Sticker>> {
        Log.d(tag, "getAllAnimated")

        return stickerDao.getAllAnimated().map { stickerSchemaList: List<StickerSchema> ->
            stickerSchemaList.map { stickerSchema: StickerSchema ->
                StickerMapper.schemaToModel(stickerSchema = stickerSchema)
            }
        }
    }

    override fun getAllNotAnimated(): Flow<List<Sticker>> {
        Log.d(tag, "getAllNotAnimated")

        return stickerDao.getAllNotAnimated().map { stickerSchemaList: List<StickerSchema> ->
            stickerSchemaList.map { stickerSchema: StickerSchema ->
                StickerMapper.schemaToModel(stickerSchema = stickerSchema)
            }
        }
    }

    override fun getAllNotInIdList(idList: List<String>, animated: Boolean): Flow<List<Sticker>> {
        Log.d(tag, "getAllNotInIdList | idList: $idList, animated: $animated")

        return stickerDao.getAllNotInIdList(idList = idList, animated = animated)
            .map { stickerSchemaList: List<StickerSchema> ->
                stickerSchemaList.map { stickerSchema: StickerSchema ->
                    StickerMapper.schemaToModel(stickerSchema = stickerSchema)
                }
            }
    }

    override suspend fun getStickerById(stickerId: String): Resource<Sticker> {
        Log.d(tag, "getStickerById | stickerId: $stickerId")

        when (val stickerByIdResult = stickerDao.getById(stickerId = stickerId)) {
            is Resource.Success -> {
                stickerByIdResult.data?.let { stickerSchema: StickerSchema ->
                    return Resource.Success(
                        data = StickerMapper.schemaToModel(
                            stickerSchema = stickerSchema
                        )
                    )
                } ?: return Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "getStickerById | StickerSchema is undefined"
                    )
                )
            }

            is Resource.Error -> {
                return Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "getStickerById | ${stickerByIdResult.uiText}"
                    )
                )
            }
        }
    }

    override suspend fun getStickerByRemoteEmoteId(emoteId: String): Resource<Sticker> {
        Log.d(tag, "getStickerByRemoteEmoteId | emoteId: $emoteId")

        val stickerByRemoteEmoteIdResult =
            stickerDao.getStickerByRemoteEmoteId(emoteId = emoteId)

        when (stickerByRemoteEmoteIdResult) {
            is Resource.Success -> {
                stickerByRemoteEmoteIdResult.data?.let { stickerSchema: StickerSchema ->
                    return Resource.Success(
                        data = StickerMapper.schemaToModel
                            (
                            stickerSchema = stickerSchema
                        )
                    )
                } ?: kotlin.run {
                    return Resource.Error(
                        uiText = UiText.DynamicString(
                            value = "getStickerByRemoteEmoteId | StickerSchema is undefined"
                        )
                    )
                }
            }

            is Resource.Error -> {
                return Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "getStickerByRemoteEmoteId | ${stickerByRemoteEmoteIdResult.uiText}"
                    )
                )
            }
        }
    }

    override suspend fun insertSticker(sticker: Sticker): SimpleResource {
        Log.d(tag, "insertSticker | sticker: $sticker")

        return stickerDao.insert(
            stickerSchema = StickerMapper.modelToSchema(
                sticker = sticker
            )
        )
    }

    override suspend fun deleteStickerById(id: String): SimpleResource {
        Log.d(tag, "deleteStickerById | id: $id")

        return stickerDao.deleteById(id = id)
    }
}

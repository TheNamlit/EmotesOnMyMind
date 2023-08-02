package com.thenamlit.emotesonmymind.features.sticker.domain.mapper

import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.models.StickerImageData
import com.thenamlit.emotesonmymind.core.domain.models.StickerRemoteEmoteData
import com.thenamlit.emotesonmymind.core.domain.models.StickerRemoteEmoteDataHostFile
import com.thenamlit.emotesonmymind.core.domain.models.StickerRemoteEmoteDataOwner
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerImageDataSchema
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerRemoteEmoteDataHostFileSchema
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerRemoteEmoteDataOwnerSchema
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerRemoteEmoteDataSchema
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerSchema
import io.realm.kotlin.types.RealmInstant


object StickerMapper {
    private val tag = Logging.loggingPrefix + StickerMapper::class.java.simpleName

    /*
     * Schema to Model
     */

    fun schemaToModel(stickerSchema: StickerSchema): Sticker {
        return Sticker(
            id = stickerSchema.id.toHexString(),
            name = stickerSchema.name,
            createdAt = stickerSchema.createdAt.epochSeconds,
            lastModified = stickerSchema.lastModified.epochSeconds,
            stickerImageData = imageDataSchemaToModel(
                stickerImageDataSchema = stickerSchema.stickerImageData
            ),
            remoteEmoteData = remoteEmoteDataSchemaToModel(
                stickerRemoteEmoteDataSchema = stickerSchema.remoteEmoteData
            )
        )
    }

    private fun imageDataSchemaToModel(
        stickerImageDataSchema: StickerImageDataSchema?,
    ): StickerImageData {
        return StickerImageData(
            width = stickerImageDataSchema?.width ?: 0,
            height = stickerImageDataSchema?.height ?: 0,
            size = stickerImageDataSchema?.size ?: 0,
            frameCount = stickerImageDataSchema?.frameCount ?: 0,
            format = stickerImageDataSchema?.format ?: "WEBP",
            animated = stickerImageDataSchema?.animated ?: false,
        )
    }

    private fun remoteEmoteDataSchemaToModel(
        stickerRemoteEmoteDataSchema: StickerRemoteEmoteDataSchema?,
    ): StickerRemoteEmoteData {
        return StickerRemoteEmoteData(
            id = stickerRemoteEmoteDataSchema?.id ?: "",
            url = stickerRemoteEmoteDataSchema?.url ?: "",
            createdAt = stickerRemoteEmoteDataSchema?.createdAt?.epochSeconds ?: 0L,
            owner = remoteEmoteDataOwnerSchemaToModel(
                stickerRemoteEmoteDataOwnerSchema = stickerRemoteEmoteDataSchema?.owner
            ),
            hostFile = remoteEmoteDataHostFileSchemaToModel(
                stickerRemoteEmoteDataHostFileSchema = stickerRemoteEmoteDataSchema?.hostFile
            )
        )
    }

    private fun remoteEmoteDataHostFileSchemaToModel(
        stickerRemoteEmoteDataHostFileSchema: StickerRemoteEmoteDataHostFileSchema?,
    ): StickerRemoteEmoteDataHostFile {
        return StickerRemoteEmoteDataHostFile(
            width = stickerRemoteEmoteDataHostFileSchema?.width ?: 0,
            height = stickerRemoteEmoteDataHostFileSchema?.height ?: 0,
            size = stickerRemoteEmoteDataHostFileSchema?.size ?: 0,
            frameCount = stickerRemoteEmoteDataHostFileSchema?.frameCount ?: 0,
            format = stickerRemoteEmoteDataHostFileSchema?.format ?: "WEBP",
            animated = stickerRemoteEmoteDataHostFileSchema?.animated ?: false,
        )
    }

    private fun remoteEmoteDataOwnerSchemaToModel(
        stickerRemoteEmoteDataOwnerSchema: StickerRemoteEmoteDataOwnerSchema?,
    ): StickerRemoteEmoteDataOwner {
        return StickerRemoteEmoteDataOwner(
            id = stickerRemoteEmoteDataOwnerSchema?.id ?: "",
            username = stickerRemoteEmoteDataOwnerSchema?.username ?: "",
            avatarUrl = stickerRemoteEmoteDataOwnerSchema?.avatarUrl ?: ""
        )
    }


    /*
     * Model to Schema
     */


    fun modelToSchema(sticker: Sticker): StickerSchema {
        // Not mapping ID because when we initially convert an Emote to a Sticker, ID is empty
        // And we don't want an empty ID in our DB
        // Same with createdAt and lastModified
        return StickerSchema().apply {
            name = sticker.name
//            createdAt = RealmInstant.from(sticker.createdAt, 0)
//            lastModified = RealmInstant.from(sticker.lastModified, 0)
            stickerImageData = imageDataModelToSchema(
                stickerImageData = sticker.stickerImageData
            )
            remoteEmoteData = remoteEmoteDataModelToSchema(
                stickerRemoteEmoteData = sticker.remoteEmoteData
            )
        }
    }

    private fun imageDataModelToSchema(
        stickerImageData: StickerImageData,
    ): StickerImageDataSchema {
        return StickerImageDataSchema().apply {
            width = stickerImageData.width
            height = stickerImageData.height
            size = stickerImageData.size
            frameCount = stickerImageData.frameCount
            format = stickerImageData.format
            animated = stickerImageData.animated
        }
    }

    private fun remoteEmoteDataModelToSchema(
        stickerRemoteEmoteData: StickerRemoteEmoteData,
    ): StickerRemoteEmoteDataSchema {
        return StickerRemoteEmoteDataSchema().apply {
            id = stickerRemoteEmoteData.id
            url = stickerRemoteEmoteData.url
            createdAt = RealmInstant.from(stickerRemoteEmoteData.createdAt, 0)
            owner = remoteEmoteDataOwnerModelToSchema(
                stickerRemoteEmoteDataOwner = stickerRemoteEmoteData.owner
            )
        }
    }

    private fun remoteEmoteDataOwnerModelToSchema(
        stickerRemoteEmoteDataOwner: StickerRemoteEmoteDataOwner,
    ): StickerRemoteEmoteDataOwnerSchema {
        return StickerRemoteEmoteDataOwnerSchema().apply {
            id = stickerRemoteEmoteDataOwner.id
            username = stickerRemoteEmoteDataOwner.username
            avatarUrl = stickerRemoteEmoteDataOwner.avatarUrl
        }
    }
}

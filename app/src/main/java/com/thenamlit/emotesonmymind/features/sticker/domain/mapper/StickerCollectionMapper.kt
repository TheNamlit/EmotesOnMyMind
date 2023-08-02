package com.thenamlit.emotesonmymind.features.sticker.domain.mapper

import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.models.StickerCollection
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerCollectionSchema
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerSchema
import io.realm.kotlin.ext.toRealmList


object StickerCollectionMapper {
    private val tag = Logging.loggingPrefix + StickerCollectionMapper::class.java.simpleName

    /*
     * Schema to Model
     */

    fun schemaToModel(stickerCollectionSchema: StickerCollectionSchema): StickerCollection {
        return StickerCollection(
            id = stickerCollectionSchema.id.toHexString(),
            name = stickerCollectionSchema.name,
            animated = stickerCollectionSchema.animated,
            stickers = stickerCollectionSchema.stickers.map { stickerSchema: StickerSchema ->
                StickerMapper.schemaToModel(
                    stickerSchema = stickerSchema
                )
            }
        )
    }


    /*
     * Model to Schema
     */


    fun modelToSchema(stickerCollection: StickerCollection): StickerCollectionSchema {
        return StickerCollectionSchema().apply {
            // Not mapping ID because when we initially convert an Emote to a Sticker, ID is empty
            // And we don't want an empty ID in our DB
            name = stickerCollection.name
            animated = stickerCollection.animated
            stickers = stickerCollection.stickers.map { sticker: Sticker ->
                StickerMapper.modelToSchema(sticker = sticker)
            }.toRealmList()
        }
    }
}

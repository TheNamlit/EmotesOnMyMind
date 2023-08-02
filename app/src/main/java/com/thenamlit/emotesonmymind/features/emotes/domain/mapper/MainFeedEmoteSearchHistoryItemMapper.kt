package com.thenamlit.emotesonmymind.features.emotes.domain.mapper

import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.features.emotes.data.local.schema.MainFeedEmoteSearchHistoryItemSchema
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmoteSearchHistoryItem


object MainFeedEmoteSearchHistoryItemMapper {
    private val tag =
        Logging.loggingPrefix + MainFeedEmoteSearchHistoryItemMapper::class.java.simpleName

    /*
     * Schema to Model
     */

    fun schemaToModel(
        mainFeedEmoteSearchHistoryItemSchema: MainFeedEmoteSearchHistoryItemSchema,
    ): MainFeedEmoteSearchHistoryItem {
        return MainFeedEmoteSearchHistoryItem(
            id = mainFeedEmoteSearchHistoryItemSchema.id.toHexString(),
            value = mainFeedEmoteSearchHistoryItemSchema.value,
        )
    }


    /*
     * Model to Schema
     */


    fun modelToSchema(
        mainFeedEmoteSearchHistoryItem: MainFeedEmoteSearchHistoryItem,
    ): MainFeedEmoteSearchHistoryItemSchema {
        return MainFeedEmoteSearchHistoryItemSchema().apply {
            // Not mapping ID because when we initially convert an Emote to a Sticker, ID is empty
            // And we don't want an empty ID in our DB
            value = mainFeedEmoteSearchHistoryItem.value
        }
    }
}

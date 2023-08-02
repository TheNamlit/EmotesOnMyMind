package com.thenamlit.emotesonmymind.features.emotes.data.local.schema

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId


class MainFeedEmoteSearchHistoryItemSchema : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId.invoke()
    var value: String = ""
    var lastRequested: RealmInstant = RealmInstant.now()
}

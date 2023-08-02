package com.thenamlit.emotesonmymind.features.sticker.data.local.schema

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId


class StickerCollectionSchema : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId.invoke()
    var name: String = ""
    var animated: Boolean = false
    var stickers: RealmList<StickerSchema> = realmListOf()
}

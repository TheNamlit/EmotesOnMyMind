package com.thenamlit.emotesonmymind.features.sticker.data.local.schema

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId


class StickerSchema : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId.invoke()
    var name: String = ""
    var createdAt: RealmInstant = RealmInstant.now()
    var lastModified: RealmInstant = RealmInstant.now()
    var stickerImageData: StickerImageDataSchema? = StickerImageDataSchema()
    var remoteEmoteData: StickerRemoteEmoteDataSchema? = StickerRemoteEmoteDataSchema()
}

class StickerImageDataSchema : RealmObject {
    var width: Int = 0
    var height: Int = 0
    var size: Int = 0
    var frameCount: Int = 0
    var format: String = "WEBP"
    var animated: Boolean = false
}

class StickerRemoteEmoteDataSchema : RealmObject {
    @Index
    var id: String = ""
    var url: String = ""
    var createdAt: RealmInstant = RealmInstant.now()
    var owner: StickerRemoteEmoteDataOwnerSchema? = StickerRemoteEmoteDataOwnerSchema()
    var hostFile: StickerRemoteEmoteDataHostFileSchema? = StickerRemoteEmoteDataHostFileSchema()
}

class StickerRemoteEmoteDataHostFileSchema : RealmObject {
    var width: Int = 0
    var height: Int = 0
    var size: Int = 0
    var frameCount: Int = 0
    var format: String = "WEBP"
    var animated: Boolean = false
}

class StickerRemoteEmoteDataOwnerSchema : RealmObject {
    var id: String = ""
    var username: String = ""
    var avatarUrl: String = ""
}

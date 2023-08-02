package com.thenamlit.emotesonmymind.features.sticker.util

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.UriMatcher
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import android.util.Log
import com.thenamlit.emotesonmymind.BuildConfig
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.models.StickerCollection
import com.thenamlit.emotesonmymind.core.domain.repository.StickerCollectionRepository
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.UiText
import com.thenamlit.emotesonmymind.core.util.WhatsAppSettings
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.io.IOException


object StickerContentProviderSettings {
    const val STICKER_PACK_IDENTIFIER_IN_QUERY = "sticker_pack_identifier"
    const val STICKER_PACK_NAME_IN_QUERY = "sticker_pack_name"
    const val STICKER_PACK_PUBLISHER_IN_QUERY = "sticker_pack_publisher"
    const val STICKER_PACK_ICON_IN_QUERY = "sticker_pack_icon"
    const val ANDROID_APP_DOWNLOAD_LINK_IN_QUERY = "android_play_store_link"
    const val IOS_APP_DOWNLOAD_LINK_IN_QUERY = "ios_app_download_link"
    const val PUBLISHER_EMAIL = "sticker_pack_publisher_email"
    const val PUBLISHER_WEBSITE = "sticker_pack_publisher_website"
    const val PRIVACY_POLICY_WEBSITE = "sticker_pack_privacy_policy_website"
    const val LICENSE_AGREEMENT_WEBSITE = "sticker_pack_license_agreement_website"
    const val IMAGE_DATA_VERSION = "image_data_version"
    const val AVOID_CACHE = "whatsapp_will_not_cache_stickers"
    const val ANIMATED_STICKER_PACK = "animated_sticker_pack"

    const val STICKER_FILE_NAME_IN_QUERY = "sticker_file_name"
    const val STICKER_FILE_EMOJI_IN_QUERY = "sticker_emoji"

    val AUTHORITY_URI: Uri =
        Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY)
            .appendPath(StickerUriMatcher.METADATA).build()

    val AUTHORITY_STICKERS_URI: Uri =
        Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY)
            .appendPath(StickerUriMatcher.STICKERS).build()

    val AUTHORITY_STICKER_ASSETS_URI: Uri =
        Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY)
            .appendPath(StickerUriMatcher.STICKERS_ASSET).build()
}

private object StickerUriMatcher {
    const val METADATA = "metadata"
    const val METADATA_CODE = 1

    const val METADATA_CODE_FOR_SINGLE_PACK = 2

    const val STICKERS = "stickers"
    const val STICKERS_CODE = 3

    const val STICKERS_ASSET = "stickers_asset"
    const val STICKERS_ASSET_CODE = 4

    const val STICKER_PACK_TRAY_ICON_CODE = 5
}

// WhatsApp-StickerPack Github - StickerContentProvider:
// https://github.com/WhatsApp/stickers/blob/main/Android/app/src/main/java/com/example/samplestickerapp/StickerContentProvider.java

// ContentProvider is one of the first things created when launching the application:
// https://stackoverflow.com/a/9966668
class StickerContentProvider : ContentProvider() {
    private val tag = Logging.loggingPrefix + StickerContentProvider::class.java.simpleName

    // @Inject lateinit var ...
    // doesn't work to instantiate stuff, so we have to use Dagger-EntryPoints
    // https://dagger.dev/hilt/entry-points
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface StickerContentProviderEntryPoint {
        fun stickerCollectionRepository(): StickerCollectionRepository
    }

    override fun onCreate(): Boolean {
        Log.d(tag, "onCreate")

        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? {
        Log.d(
            tag,
            "query | uri: $uri, " +
                    "projection: $projection, " +
                    "selection: $selection, " +
                    "selectionArgs: $selectionArgs, " +
                    "sortOrder: $sortOrder"
        )

        val stickerCollections: List<StickerCollection> = getAllStickerCollections()
        val uriMatcher: UriMatcher = getUriMatcher(stickerCollections = stickerCollections)

        when (uriMatcher.match(uri)) {
            StickerUriMatcher.METADATA_CODE -> {
                val cursorResult = getCursorForAllStickerPacks(
                    uri = uri,
                    stickerCollections = stickerCollections
                )

                return when (cursorResult) {
                    is Resource.Success -> {
                        Log.d(tag, "query.METADATA_CODE | Success")

                        cursorResult.data
                    }

                    is Resource.Error -> {
                        Log.e(tag, "query.METADATA_CODE | ${cursorResult.uiText}")

                        null
                    }
                }
            }

            StickerUriMatcher.METADATA_CODE_FOR_SINGLE_PACK -> {
                val stickerCollectionResult =
                    getStickerCollectionFromUri(uri = uri, stickerCollections = stickerCollections)

                when (stickerCollectionResult) {
                    is Resource.Success -> {
                        stickerCollectionResult.data?.let { stickerCollection: StickerCollection ->
                            val cursorResult = getCursorForSingleStickerPack(
                                uri = uri,
                                stickerCollection = stickerCollection
                            )

                            return when (cursorResult) {
                                is Resource.Success -> {
                                    Log.d(tag, "query.METADATA_CODE_FOR_SINGLE_PACK | Success")

                                    cursorResult.data
                                }

                                is Resource.Error -> {
                                    Log.e(
                                        tag,
                                        "query.METADATA_CODE_FOR_SINGLE_PACK | ${cursorResult.uiText}"
                                    )

                                    null
                                }
                            }
                        } ?: kotlin.run {
                            Log.e(
                                tag,
                                "query.METADATA_CODE_FOR_SINGLE_PACK | StickerCollection undefined"
                            )

                            return null
                        }
                    }

                    is Resource.Error -> {
                        Log.e(
                            tag,
                            "query.METADATA_CODE_FOR_SINGLE_PACK | ${stickerCollectionResult.uiText}"
                        )

                        return null
                    }
                }
            }

            StickerUriMatcher.STICKERS_CODE -> {
                val stickerCollectionResult =
                    getStickerCollectionFromUri(uri = uri, stickerCollections = stickerCollections)

                when (stickerCollectionResult) {
                    is Resource.Success -> {
                        stickerCollectionResult.data?.let { stickerCollection: StickerCollection ->
                            val cursorResult = getStickersForAStickerPack(
                                uri = uri,
                                stickerCollection = stickerCollection
                            )

                            return when (cursorResult) {
                                is Resource.Success -> {
                                    Log.d(tag, "query.STICKERS_CODE | Success")

                                    cursorResult.data
                                }

                                is Resource.Error -> {
                                    Log.e(tag, "query.STICKERS_CODE | ${cursorResult.uiText}")

                                    null
                                }
                            }
                        } ?: kotlin.run {
                            Log.e(tag, "query.STICKERS_CODE | StickerCollection undefined")

                            return null
                        }
                    }

                    is Resource.Error -> {
                        Log.e(tag, "query.STICKERS_CODE | ${stickerCollectionResult.uiText}")

                        return null
                    }
                }
            }

            else -> {
                Log.e(tag, "query | Unknown Uri: $uri")

                return null
            }
        }
    }

    private fun getAllStickerCollections(): List<StickerCollection> {
        Log.d(tag, "getAllStickerCollections")

        // Get StickerCollectionRepository from Dagger-EntryPoints because
        // @Inject lateinit var stickerCollectionRepository: StickerCollectionRepository
        // doesn't work...won't be instantiated here
        // https://dagger.dev/hilt/entry-points
        val stickerContentProviderEntryPoint =
            EntryPoints.get(context!!, StickerContentProviderEntryPoint::class.java)
        val stickerCollectionRepository =
            stickerContentProviderEntryPoint.stickerCollectionRepository()

        // Use synchronized call because coroutines don't work in here!?
        // https://www.mongodb.com/docs/realm/sdk/kotlin/realm-database/crud/read/#read-realm-objects---kotlin-sdk
        when (val stickerCollectionsResult = stickerCollectionRepository.getAllSynchronized()) {
            is Resource.Success -> {
                stickerCollectionsResult.data?.let { stickerCollections: List<StickerCollection> ->
                    return stickerCollections
                } ?: kotlin.run {
                    Log.e(tag, "getAllStickerCollections | StickerCollections undefined")
                    return emptyList()
                }
            }

            is Resource.Error -> {
                Log.e(tag, "getAllStickerCollections | ${stickerCollectionsResult.uiText}")

                return emptyList()
            }
        }
    }

    private fun getCursorForAllStickerPacks(
        uri: Uri,
        stickerCollections: List<StickerCollection>,
    ): Resource<Cursor> {
        Log.d(
            tag,
            "getCursorForAllStickerPacks | uri: $uri, " +
                    "stickerCollections: $stickerCollections"
        )

        return getCursorWithStickerCollectionsData(
            uri = uri,
            stickerCollections = stickerCollections
        )
    }

    private fun getCursorForSingleStickerPack(
        uri: Uri,
        stickerCollection: StickerCollection,
    ): Resource<Cursor> {
        Log.d(
            tag,
            "getCursorForSingleStickerPack | uri: $uri, " +
                    "stickerCollection: $stickerCollection"
        )

        return getCursorWithStickerCollectionsData(
            uri = uri,
            stickerCollections = listOf(stickerCollection)
        )
    }

    private fun getCursorWithStickerCollectionsData(
        uri: Uri,
        stickerCollections: List<StickerCollection>,
    ): Resource<Cursor> {
        Log.d(
            tag,
            "getCursorWithStickerCollectionsData | uri: $uri, " +
                    "stickerCollections: $stickerCollections"
        )

        val cursorFieldNames: Array<out String> = arrayOf(
            StickerContentProviderSettings.STICKER_PACK_IDENTIFIER_IN_QUERY,
            StickerContentProviderSettings.STICKER_PACK_NAME_IN_QUERY,
            StickerContentProviderSettings.STICKER_PACK_PUBLISHER_IN_QUERY,
            StickerContentProviderSettings.STICKER_PACK_ICON_IN_QUERY,
            StickerContentProviderSettings.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY,
            StickerContentProviderSettings.IOS_APP_DOWNLOAD_LINK_IN_QUERY,
            StickerContentProviderSettings.PUBLISHER_EMAIL,
            StickerContentProviderSettings.PUBLISHER_WEBSITE,
            StickerContentProviderSettings.PRIVACY_POLICY_WEBSITE,
            StickerContentProviderSettings.LICENSE_AGREEMENT_WEBSITE,
            StickerContentProviderSettings.IMAGE_DATA_VERSION,
            StickerContentProviderSettings.AVOID_CACHE,
            StickerContentProviderSettings.ANIMATED_STICKER_PACK,
        )

        val cursor = MatrixCursor(cursorFieldNames)

        // https://github.com/WhatsApp/stickers/tree/main/Android#how-to-create-a-sticker-app
        stickerCollections.forEach { stickerCollection: StickerCollection ->
            val animated = stickerCollection.animated

            // TODO: Implement Field in Database for avoidCache
            val avoidCache = false

            val cursorMatrixRowBuilder = cursor.newRow()
            // Unique Identifier of Collection
            cursorMatrixRowBuilder.add(stickerCollection.id)
            // Name of the WhatsApp-StickerPack
            cursorMatrixRowBuilder.add(stickerCollection.name)
            // Publisher
            cursorMatrixRowBuilder.add("John Doe")
            // TODO: Implement Username, which is being saved in AppSettings-Table or so
            // Icon that is displayed in WhatsApp for the StickerPack
            cursorMatrixRowBuilder.add(WhatsAppSettings.STICKER_COLLECTION_TRAY_IMAGE_NAME)
            // TODO: Let User choose a Sticker that is used as StickerPack_Icon - Could also let him upload one?
            // Android PlayStore-Link [OPTIONAL]
            cursorMatrixRowBuilder.add("")
            // iOS AppStore-Link [OPTIONAL]
            cursorMatrixRowBuilder.add("")
            // Publisher E-Mail
            cursorMatrixRowBuilder.add("")
            // TODO: Implement E-Mail...Can it be empty too? ^^
            //  Don't wanna bother the user with inputting their E-Mail
            // Publisher Website [OPTIONAL]
            cursorMatrixRowBuilder.add("")
            // Privacy Policy Website [OPTIONAL]
            cursorMatrixRowBuilder.add("")
            // License Agreement Website [OPTIONAL]
            cursorMatrixRowBuilder.add("")
            // Image Data Version
            cursorMatrixRowBuilder.add("1")
            // TODO: Implement Field in Database for this to update
            //  AvoidCache - Default is false; Should be activated if Sticker is updated without User-Actions
            //  Example: Having a Clock-Sticker that auto updates every minute
            cursorMatrixRowBuilder.add(if (avoidCache) 1 else 0)
            // Animated - Has to be specified for every Pack
            cursorMatrixRowBuilder.add(if (animated) 1 else 0)
        }

        cursor.setNotificationUri(context?.contentResolver, uri)
        return Resource.Success(data = cursor)
    }

    private fun getStickersForAStickerPack(
        uri: Uri,
        stickerCollection: StickerCollection,
    ): Resource<Cursor> {
        Log.d(
            tag, "getStickersForAStickerPack | uri: $uri, " +
                    "stickerCollection: $stickerCollection"
        )

        val cursorFieldNames: Array<out String> = arrayOf(
            StickerContentProviderSettings.STICKER_FILE_NAME_IN_QUERY,
            StickerContentProviderSettings.STICKER_FILE_EMOJI_IN_QUERY
        )
        val cursor = MatrixCursor(cursorFieldNames)

        stickerCollection.stickers.forEach { sticker: Sticker ->
            val emojis = listOf("😀")    // TODO: Get Emojis here somehow (Implement in DB?)

            cursor.addRow(
                arrayOf<Any>(
                    "${sticker.remoteEmoteData.id}.webp",
                    TextUtils.join(",", emojis)
                )
            )
        }

        cursor.setNotificationUri(context?.contentResolver, uri)
        return Resource.Success(data = cursor)
    }

    private fun getStickerCollectionFromUri(
        uri: Uri,
        stickerCollections: List<StickerCollection>,
    ): Resource<StickerCollection> {
        Log.d(
            tag, "getStickerCollectionFromUri | uri: $uri, " +
                    "stickerCollections: $stickerCollections"
        )

        val pathSegments: List<String> = uri.pathSegments
        var identifierFromUri: String? = ""

        if (pathSegments.size == 2) {
            identifierFromUri = uri.lastPathSegment
        } else if (pathSegments.size == 3) {
            identifierFromUri = pathSegments[pathSegments.size - 2]
        }

        identifierFromUri?.let { identifier: String ->
            try {
                return Resource.Success(
                    data = stickerCollections.first { stickerCollection: StickerCollection ->
                        stickerCollection.id == identifier
                    }
                )
            } catch (e: NoSuchElementException) {
                return Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "getStickerCollectionFromUri | NoSuchElementException: " +
                                "${e.message}\n${e.stackTrace}"
                    )
                )
            }
        } ?: kotlin.run {
            return Resource.Error(
                uiText = UiText.DynamicString(
                    value = "getStickerCollectionFromUri | Identifier undefined"
                )
            )
        }
    }

    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        Log.d(tag, "openAssetFile | uri: $uri, mode: $mode")

        val pathSegments: List<String> = uri.pathSegments
        if (pathSegments.size != 3) {
            Log.e(tag, "openAssetFile | PathSegments should be 3")

            return null
        }

        val stickerCollections: List<StickerCollection> = getAllStickerCollections()
        val uriMatcher: UriMatcher = getUriMatcher(stickerCollections = stickerCollections)
        val fileName = pathSegments[pathSegments.size - 1]

        return when (uriMatcher.match(uri)) {
            StickerUriMatcher.STICKERS_ASSET_CODE -> {
                val stickerImageFileResult =
                    getStickerImageFile(fileName = fileName)

                when (stickerImageFileResult) {
                    is Resource.Success -> {
                        stickerImageFileResult.data
                    }

                    is Resource.Error -> {
                        Log.e(
                            tag,
                            "openAssetFile.STICKERS_ASSET_CODE | ${stickerImageFileResult.uiText}"
                        )

                        null
                    }
                }


//                val stickerCollectionResult =
//                    getStickerCollectionFromUri(uri = uri, stickerCollections = stickerCollections)
//
//                return when (stickerCollectionResult) {
//                    is Resource.Success -> {
//                        stickerCollectionResult.data?.let { stickerCollection: StickerCollection ->
//                            val stickerImageFileResult =
//                                getStickerImageFile(fileName=fileName)
//
//                            when (stickerImageFileResult) {
//                                is Resource.Success -> {
//                                    stickerImageFileResult.data
//                                }
//
//                                is Resource.Error -> {
//                                    Log.e(tag, "openAssetFile | ${stickerImageFileResult.uiText}")
//
//                                    null
//                                }
//                            }
//                        } ?: kotlin.run {
//                            Log.e(tag, "openAssetFile | StickerCollection is undefined")
//
//                            null
//                        }
//                    }
//
//                    is Resource.Error -> {
//                        Log.e(tag, "openAssetFile | ${stickerCollectionResult.uiText}")
//
//                        null
//                    }
//                }
            }

            StickerUriMatcher.STICKER_PACK_TRAY_ICON_CODE -> {
                val stickerCollectionCoverImageFileResult =
                    getStickerCollectionCoverImageFile(fileName = fileName)

                return when (stickerCollectionCoverImageFileResult) {
                    is Resource.Success -> {
                        stickerCollectionCoverImageFileResult.data
                    }

                    is Resource.Error -> {
                        Log.e(
                            tag,
                            "openAssetFile.STICKER_PACK_TRAY_ICON_CODE | ${stickerCollectionCoverImageFileResult.uiText}"
                        )

                        null
                    }
                }
            }

            else -> {
                Log.e(tag, "openAssetFile | Unknown Uri: $uri")

                super.openAssetFile(uri, mode)
            }
        }
    }

    private fun getStickerImageFile(fileName: String): Resource<AssetFileDescriptor> {
        Log.d(tag, "getStickerImageFile | fileName: $fileName")

        val filePath = "${context?.filesDir?.absolutePath}/stickers/$fileName"
        try {
            val stickerImageFile = File(filePath)

            return Resource.Success(
                data = AssetFileDescriptor(
                    ParcelFileDescriptor.open(
                        stickerImageFile,
                        ParcelFileDescriptor.MODE_READ_ONLY
                    ), 0, AssetFileDescriptor.UNKNOWN_LENGTH
                )
            )
        } catch (e: NullPointerException) {
            return Resource.Error(
                uiText = UiText.DynamicString(
                    value = "getStickerImageFile | Couldn't find File in path: $filePath"
                )
            )
        }
    }

    private fun getStickerCollectionCoverImageFile(fileName: String): Resource<AssetFileDescriptor> {
        Log.d(tag, "getStickerCollectionCoverImageFile | fileName: $fileName")

        val contextAssetManager: AssetManager? = context?.assets

        contextAssetManager?.let { assetManager: AssetManager ->
            try {
                return Resource.Success(
                    data = assetManager.openFd(
                        "StickerCollectionCoverImages/$fileName"
                    )
                )
            } catch (e: IOException) {
                return Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "getStickerCollectionCoverImageFile | ${e.message}\n${e.stackTrace}"
                    )
                )
            }
        } ?: kotlin.run {
            return Resource.Error(
                uiText = UiText.DynamicString(
                    value = "getStickerCollectionCoverImageFile | AssetManager is undefined"
                )
            )
        }

        // TODO: Implement StickerCollectionCovers
//        val filePath = "${context?.filesDir?.absolutePath}/sticker_collection_cover/$fileName"
//        try {
//            val stickerImageFile = File(filePath)
//
//            return Resource.Success(
//                data = AssetFileDescriptor(
//                    ParcelFileDescriptor.open(
//                        stickerImageFile,
//                        ParcelFileDescriptor.MODE_READ_ONLY
//                    ), 0, AssetFileDescriptor.UNKNOWN_LENGTH
//                )
//            )
//        } catch (e: NullPointerException) {
//            return Resource.Error(
//                uiText = UiText.DynamicString(
//                    value = "getStickerCollectionCoverImageFile | Couldn't find File in path: $filePath"
//                )
//            )
//        }
    }

    override fun getType(uri: Uri): String? {
        Log.d(tag, "getType | uri: $uri")

        val stickerCollections: List<StickerCollection> = getAllStickerCollections()
        val uriMatcher: UriMatcher = getUriMatcher(stickerCollections = stickerCollections)

        return when (uriMatcher.match(uri)) {
            StickerUriMatcher.METADATA_CODE -> {
                Log.d(tag, "getType.METADATA_CODE")

                return "vnd.android.cursor.dir/vnd.${BuildConfig.CONTENT_PROVIDER_AUTHORITY}.${StickerUriMatcher.METADATA}"
            }

            StickerUriMatcher.METADATA_CODE_FOR_SINGLE_PACK -> {
                Log.d(tag, "getType.METADATA_CODE_FOR_SINGLE_PACK")

                return "vnd.android.cursor.item/vnd.${BuildConfig.CONTENT_PROVIDER_AUTHORITY}.${StickerUriMatcher.METADATA}"
            }

            StickerUriMatcher.STICKERS_CODE -> {
                Log.d(tag, "getType.STICKERS_CODE")

                return "vnd.android.cursor.dir/vnd.${BuildConfig.CONTENT_PROVIDER_AUTHORITY}.${StickerUriMatcher.STICKERS}"
            }

            StickerUriMatcher.STICKERS_ASSET_CODE -> {
                Log.d(tag, "getType.STICKERS_ASSET_CODE")

                return "image/webp"
            }

            StickerUriMatcher.STICKER_PACK_TRAY_ICON_CODE -> {
                Log.d(tag, "getType.STICKER_PACK_TRAY_ICON_CODE")

                return "image/png"
            }

            else -> {
                Log.e(tag, "getType | Unknown Uri: $uri")

                null
            }
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.d(tag, "insert | uri: $uri, values: $values")

        throw UnsupportedOperationException("Not supported")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d(
            tag, "delete | uri: $uri, " +
                    "selection: $selection, " +
                    "selectionArgs: $selectionArgs"
        )

        throw UnsupportedOperationException("Not supported")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        slection: String?,
        selectionArgs: Array<out String>?,
    ): Int {
        Log.d(
            tag,
            "update | uri: $uri, " +
                    "values: $values, " +
                    "selection: $selectionArgs, " +
                    "selectionArgs: $selectionArgs"
        )
        throw UnsupportedOperationException("Not supported")
    }

    private fun getUriMatcher(stickerCollections: List<StickerCollection>): UriMatcher {
        Log.d(tag, "getUriMatcher | stickerCollections: $stickerCollections")

        val authority = BuildConfig.CONTENT_PROVIDER_AUTHORITY
        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        // https://developer.android.com/reference/android/content/UriMatcher#addURI(java.lang.String,%20java.lang.String,%20int)
        // The call to get the metadata for the sticker packs
        uriMatcher.addURI(
            authority,
            StickerUriMatcher.METADATA,
            StickerUriMatcher.METADATA_CODE
        )

        // The call to get the metadata for single sticker pack. * represent the identifier
        uriMatcher.addURI(
            authority,
            StickerUriMatcher.METADATA + "/*",
            StickerUriMatcher.METADATA_CODE_FOR_SINGLE_PACK
        )

        // Gets the list of stickers for a sticker pack, * represent the identifier
        uriMatcher.addURI(
            authority,
            StickerUriMatcher.STICKERS + "/*",
            StickerUriMatcher.STICKERS_CODE
        )

        stickerCollections.forEach { stickerCollection: StickerCollection ->
            // TODO: Save and get from Collection (DB)
            val stickerPackTrayImageFile = WhatsAppSettings.STICKER_COLLECTION_TRAY_IMAGE_NAME
            val stickerCollectionPath =
                "${StickerUriMatcher.STICKERS_ASSET}/${stickerCollection.id}/$stickerPackTrayImageFile"

            uriMatcher.addURI(
                authority,
                stickerCollectionPath,
                StickerUriMatcher.STICKER_PACK_TRAY_ICON_CODE
            )

            stickerCollection.stickers.forEach { sticker: Sticker ->
                val stickerPath =
                    "${StickerUriMatcher.STICKERS_ASSET}/${stickerCollection.id}/${sticker.remoteEmoteData.id}.webp"

                uriMatcher.addURI(
                    authority,
                    stickerPath,
                    StickerUriMatcher.STICKERS_ASSET_CODE
                )
            }
        }

        return uriMatcher
    }
}

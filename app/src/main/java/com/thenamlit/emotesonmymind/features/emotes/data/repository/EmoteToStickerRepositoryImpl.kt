package com.thenamlit.emotesonmymind.features.emotes.data.repository

import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.disk.DiskCache
import com.thenamlit.emotesonmymind.core.domain.repository.DeviceStorageRepository
import com.thenamlit.emotesonmymind.core.util.DownloadAndScaleAnimatedImageWorker
import com.thenamlit.emotesonmymind.core.util.DownloadAndScaleImageWorker
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.UiText
import com.thenamlit.emotesonmymind.core.util.WhatsAppSettings
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetails
import com.thenamlit.emotesonmymind.features.emotes.domain.repository.EmoteToStickerRepository
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.Path
import java.util.UUID
import javax.inject.Inject


class EmoteToStickerRepositoryImpl @Inject constructor(
    private val deviceStorageRepository: DeviceStorageRepository,
    private val imageLoader: ImageLoader,
    private val workManager: WorkManager,
    private val jsonBuilder: Json,
) : EmoteToStickerRepository {
    private val tag = Logging.loggingPrefix + EmoteToStickerRepositoryImpl::class.java.simpleName

    @OptIn(ExperimentalCoilApi::class)
    override suspend fun downloadNotAnimatedStickerFile(
        sourceUrl: String,
        sourceFileName: String,
        sourceImageToScaleSizeMultiplier: Double,
        destinationDirectory: String,
        destinationFileName: String,
        emoteDetails: EmoteDetails,
    ): Resource<UUID> {
        Log.d(
            tag,
            "downloadNotAnimatedStickerFile | sourceUrl: $sourceUrl, " +
                    "sourceFileName: $sourceFileName, " +
                    "sourceImageToScaleSizeMultiplier: $sourceImageToScaleSizeMultiplier, " +
                    "destinationDirectory: $destinationDirectory, " +
                    "destinationFileName: $destinationFileName, " +
                    "emoteDetails: $emoteDetails"
        )

        val fullSourceUrl = "$sourceUrl/$sourceFileName"
        var isCachedLocalFile = false
        var contentUri: String = fullSourceUrl

        // https://github.com/coil-kt/coil/issues/100#issuecomment-939691688
        imageLoader.diskCache?.get(fullSourceUrl)?.use { snapshot: DiskCache.Snapshot ->
            // Read or copy the file. You **must** close the snapshot (`use` closes the snapshot)
            // or it'll prevent writing to that entry until your app is killed.

            val cachedFilePath: Path = snapshot.data
            val cachedImageFile = snapshot.data.toFile()
            if (cachedImageFile.exists()) {
                Log.d(
                    tag,
                    "downloadNotAnimatedStickerFile | Download cachedImageFile: $cachedImageFile"
                )

                isCachedLocalFile = true
                contentUri = cachedFilePath.toString()
            }
        } ?: kotlin.run {
            Log.d(
                tag,
                "downloadNotAnimatedStickerFile | Couldn't find File in Coil-DiskCache - Using remote version"
            )
        }

        when (val createDirectoryResult =
            deviceStorageRepository.createDirectoryIfNotExists(directory = destinationDirectory)) {
            is Resource.Success -> {
                val emoteDetailsJsonStringResult: Resource<String> =
                    encodeEmoteDetailsToJsonString(emoteDetails = emoteDetails)

                when (emoteDetailsJsonStringResult) {
                    is Resource.Success -> {
                        emoteDetailsJsonStringResult.data?.let { emoteDetailsJsonString: String ->
                            val request = OneTimeWorkRequestBuilder<DownloadAndScaleImageWorker>()
                                .setInputData(
                                    workDataOf(
                                        DownloadAndScaleImageWorker.KEY_CONTENT_URI to contentUri,
                                        DownloadAndScaleImageWorker.KEY_DESTINATION_FILES_PATH to destinationDirectory,
                                        DownloadAndScaleImageWorker.KEY_DESTINATION_FILE_NAME to destinationFileName,
                                        DownloadAndScaleImageWorker.KEY_COMPRESSION_THRESHOLD to WhatsAppSettings.NOT_ANIMATED_STICKER_SIZE,
                                        DownloadAndScaleImageWorker.KEY_DESTINATION_IMAGE_WIDTH to WhatsAppSettings.STICKER_WIDTH,
                                        DownloadAndScaleImageWorker.KEY_DESTINATION_IMAGE_HEIGHT to WhatsAppSettings.STICKER_HEIGHT,
                                        DownloadAndScaleImageWorker.KEY_IS_CACHED_LOCAL_FILE to isCachedLocalFile,
                                        DownloadAndScaleImageWorker.KEY_BITMAP_TO_SCALE_SIZE_MULTIPLIER to sourceImageToScaleSizeMultiplier,
                                        DownloadAndScaleImageWorker.KEY_EMOTE_DETAILS_JSON_STRING to emoteDetailsJsonString
                                    )
                                ).build()

                            // TODO: Check positioning of original Image in later Image again...
                            //  still on the left and/or top sometimes...

                            workManager.enqueue(request)

                            return Resource.Success(data = request.id)
                        } ?: kotlin.run {
                            return Resource.Error(
                                uiText = UiText.DynamicString(
                                    value = "downloadNotAnimatedStickerFile | EmoteDetailsJsonString is undefined"
                                )
                            )
                        }
                    }

                    is Resource.Error -> {
                        return Resource.Error(
                            uiText = UiText.DynamicString(
                                value = "downloadNotAnimatedStickerFile | ${emoteDetailsJsonStringResult.uiText}"
                            )
                        )
                    }
                }
            }

            is Resource.Error -> {
                return Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "downloadNotAnimatedStickerFile | ${createDirectoryResult.uiText}"
                    )
                )
            }
        }
    }

    @OptIn(ExperimentalCoilApi::class)
    override suspend fun downloadAnimatedStickerFile(
        sourceUrl: String,
        sourceFileName: String,
        sourceImageToScaleSizeMultiplier: Double,
        destinationDirectory: String,
        destinationFileName: String,
        emoteDetails: EmoteDetails,
    ): Resource<UUID> {
        Log.d(
            tag,
            "downloadAnimatedStickerFile | sourceUrl: $sourceUrl, " +
                    "sourceFileName: $sourceFileName, " +
                    "sourceImageToScaleSizeMultiplier: $sourceImageToScaleSizeMultiplier, " +
                    "destinationDirectory: $destinationDirectory, " +
                    "destinationFileName: $destinationFileName, " +
                    "emoteDetails: $emoteDetails"
        )

        val fullSourceUrl = "$sourceUrl/$sourceFileName"
        var isCachedLocalFile = false
        var contentUri: String = fullSourceUrl

        // https://github.com/coil-kt/coil/issues/100#issuecomment-939691688
        imageLoader.diskCache?.get(fullSourceUrl)?.use { snapshot: DiskCache.Snapshot ->
            // Read or copy the file. You **must** close the snapshot (`use` closes the snapshot)
            // or it'll prevent writing to that entry until your app is killed.

            val cachedFilePath: Path = snapshot.data
            val cachedImageFile = snapshot.data.toFile()
            if (cachedImageFile.exists()) {
                Log.d(
                    tag,
                    "downloadAnimatedStickerFile | Download cachedImageFile: $cachedImageFile"
                )

                isCachedLocalFile = true
                contentUri = cachedFilePath.toString()
            }
        } ?: kotlin.run {
            Log.d(
                tag,
                "downloadAnimatedStickerFile | Couldn't find File in Coil-DiskCache - Using remote version"
            )
        }

        when (val createDirectoryResult =
            deviceStorageRepository.createDirectoryIfNotExists(directory = destinationDirectory)) {
            is Resource.Success -> {
                val emoteDetailsJsonStringResult: Resource<String> =
                    encodeEmoteDetailsToJsonString(emoteDetails = emoteDetails)

                when (emoteDetailsJsonStringResult) {
                    is Resource.Success -> {
                        emoteDetailsJsonStringResult.data?.let { emoteDetailsJsonString: String ->
                            val request =
                                OneTimeWorkRequestBuilder<DownloadAndScaleAnimatedImageWorker>()
                                    .setInputData(
                                        workDataOf(
                                            DownloadAndScaleImageWorker.KEY_CONTENT_URI to contentUri,
                                            DownloadAndScaleImageWorker.KEY_DESTINATION_FILES_PATH to destinationDirectory,
                                            DownloadAndScaleImageWorker.KEY_DESTINATION_FILE_NAME to destinationFileName,
                                            DownloadAndScaleImageWorker.KEY_COMPRESSION_THRESHOLD to WhatsAppSettings.ANIMATED_STICKER_SIZE,
                                            DownloadAndScaleImageWorker.KEY_DESTINATION_IMAGE_WIDTH to WhatsAppSettings.STICKER_WIDTH,
                                            DownloadAndScaleImageWorker.KEY_DESTINATION_IMAGE_HEIGHT to WhatsAppSettings.STICKER_HEIGHT,
                                            DownloadAndScaleImageWorker.KEY_IS_CACHED_LOCAL_FILE to isCachedLocalFile,
                                            DownloadAndScaleImageWorker.KEY_BITMAP_TO_SCALE_SIZE_MULTIPLIER to sourceImageToScaleSizeMultiplier,
                                            DownloadAndScaleImageWorker.KEY_EMOTE_DETAILS_JSON_STRING to emoteDetailsJsonString,
                                        )
                                    ).build()

                            // TODO: Check positioning of original Image in later Image again...
                            //  still on the left and/or top sometimes...

                            workManager.enqueue(request)

                            return Resource.Success(data = request.id)
                        } ?: kotlin.run {
                            return Resource.Error(
                                uiText = UiText.DynamicString(
                                    value = "downloadAnimatedStickerFile | EmoteDetailsJsonString is undefined"
                                )
                            )
                        }
                    }

                    is Resource.Error -> {
                        return Resource.Error(
                            uiText = UiText.DynamicString(
                                value = "downloadAnimatedStickerFile | ${emoteDetailsJsonStringResult.uiText}"
                            )
                        )
                    }
                }
            }

            is Resource.Error -> {
                return Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "downloadAnimatedStickerFile | ${createDirectoryResult.uiText}"
                    )
                )
            }
        }
    }

    private fun encodeEmoteDetailsToJsonString(emoteDetails: EmoteDetails): Resource<String> {
        Log.d(tag, "encodeEmoteDetailsToJsonString | emoteDetails: $emoteDetails")

        return try {
            Resource.Success(data = jsonBuilder.encodeToString(emoteDetails))
        } catch (e: SerializationException) {
            Resource.Error(
                uiText = UiText.DynamicString(
                    value = "encodeEmoteDetailsToJsonString | SerializationException: ${e.message}\n${e.stackTrace}"
                )
            )
        } catch (e: IllegalArgumentException) {
            Resource.Error(
                uiText = UiText.DynamicString(
                    value = "encodeEmoteDetailsToJsonString | IllegalArgumentException: ${e.message}\n${e.stackTrace}"
                )
            )
        }
    }
}

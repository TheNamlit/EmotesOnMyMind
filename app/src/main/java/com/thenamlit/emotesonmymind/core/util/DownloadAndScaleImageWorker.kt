package com.thenamlit.emotesonmymind.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.thenamlit.emotesonmymind.core.domain.repository.DeviceStorageRepository
import com.thenamlit.emotesonmymind.core.domain.repository.StickerRepository
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetails
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URL


@HiltWorker
class DownloadAndScaleImageWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val stickerRepository: StickerRepository,
    private val deviceStorageRepository: DeviceStorageRepository,
    private val jsonBuilder: Json,
) : CoroutineWorker(appContext = appContext, params = params) {
    private val tag = Logging.loggingPrefix + DownloadAndScaleImageWorker::class.java.simpleName

    companion object {
        const val KEY_CONTENT_URI = "KEY_CONTENT_URI"
        const val KEY_DESTINATION_FILES_PATH = "KEY_DESTINATION_FILES_PATH"
        const val KEY_DESTINATION_FILE_NAME = "KEY_DESTINATION_FILE_NAME"
        const val KEY_COMPRESSION_THRESHOLD = "KEY_COMPRESSION_THRESHOLD"
        const val KEY_DESTINATION_IMAGE_WIDTH = "KEY_DESTINATION_IMAGE_WIDTH"
        const val KEY_DESTINATION_IMAGE_HEIGHT = "KEY_DESTINATION_IMAGE_HEIGHT"
        const val KEY_IS_CACHED_LOCAL_FILE = "KEY_IS_CACHED_LOCAL_FILE"
        const val KEY_BITMAP_TO_SCALE_SIZE_MULTIPLIER = "KEY_BITMAP_TO_SCALE_SIZE_MULTIPLIER"
        const val KEY_EMOTE_DETAILS_JSON_STRING = "KEY_EMOTE_DETAILS_JSON_STRING"
        const val KEY_RESULT_TEXT = "KEY_RESULT_TEXT"
        const val KEY_RESULT_SUCCESS = "KEY_RESULT_SUCCESS"
        const val KEY_RESULT_PROGRESS = "KEY_RESULT_PROGRESS"
    }

    private var scaledImageWidth: Int? = null
    private var scaledImageHeight: Int? = null
    private var scaledImageSize: Int? = null

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(tag, "doWork")

        setProgress(workDataOf(KEY_RESULT_PROGRESS to 0))

        val stringUriParam: String? = params.inputData.getString(KEY_CONTENT_URI)
        val stringDestinationFilesPathParam: String? =
            params.inputData.getString(KEY_DESTINATION_FILES_PATH)
        val stringDestinationFileNameParam: String? =
            params.inputData.getString(KEY_DESTINATION_FILE_NAME)
        val isCachedLocalFile: Boolean =
            params.inputData.getBoolean(KEY_IS_CACHED_LOCAL_FILE, false)
        val compressionThresholdInBytes =
            params.inputData.getLong(KEY_COMPRESSION_THRESHOLD, 1024 * 95L)
        val destinationImageWidth = params.inputData.getInt(KEY_DESTINATION_IMAGE_WIDTH, 512)
        val destinationImageHeight = params.inputData.getInt(KEY_DESTINATION_IMAGE_HEIGHT, 512)
        val bitmapToScaleSizeMultiplier =
            params.inputData.getDouble(KEY_BITMAP_TO_SCALE_SIZE_MULTIPLIER, 1.0)
        val emoteDetailsStringParam: String? =
            params.inputData.getString(KEY_EMOTE_DETAILS_JSON_STRING)

        stringUriParam?.let { stringUri: String ->
            stringDestinationFilesPathParam?.let { stringDestinationFilesPath: String ->
                stringDestinationFileNameParam?.let { stringDestinationFileName: String ->
                    emoteDetailsStringParam?.let { emoteDetailsString: String ->
                        val emoteDetailsFromJsonStringResult =
                            getEmoteDetailsFromJsonString(jsonString = emoteDetailsString)

                        val absoluteDestinationFilePath =
                            "${appContext.filesDir}/$stringDestinationFilesPath/$stringDestinationFileName"

                        when (emoteDetailsFromJsonStringResult) {
                            is Resource.Success -> {
                                emoteDetailsFromJsonStringResult.data?.let { emoteDetails: EmoteDetails ->
                                    val downloadAndScaleBitmapResult = downloadAndScaleBitmap(
                                        stringUri = stringUri,
                                        absoluteDestinationFilePath = absoluteDestinationFilePath,
                                        isCachedLocalFile = isCachedLocalFile,
                                        compressionThresholdInBytes = compressionThresholdInBytes,
                                        destinationImageWidth = destinationImageWidth,
                                        destinationImageHeight = destinationImageHeight,
                                        bitmapToScaleSizeMultiplier = bitmapToScaleSizeMultiplier
                                    )

                                    when (downloadAndScaleBitmapResult) {
                                        is Resource.Success -> {
                                            Log.d(
                                                tag,
                                                "Successfully downloaded Sticker-Bitmap to Internal Storage"
                                            )

                                            val saveEmoteAsStickerInLocalDatabaseResult =
                                                saveEmoteAsStickerInLocalDatabase(
                                                    emoteDetails = emoteDetails
                                                )

                                            when (saveEmoteAsStickerInLocalDatabaseResult) {
                                                is Resource.Success -> {
                                                    Log.d(
                                                        tag,
                                                        "Successfully inserted Sticker into local Database"
                                                    )

                                                    return@withContext returnSuccess(
                                                        message = "Successfully downloaded Sticker"
                                                    )
                                                }

                                                is Resource.Error -> {
                                                    Log.e(
                                                        tag,
                                                        "doWork | ${saveEmoteAsStickerInLocalDatabaseResult.uiText}"
                                                    )

                                                    val deletedFileFromDeviceResult =
                                                        deviceStorageRepository.deleteFile(
                                                            absoluteDestinationFilePath
                                                        )

                                                    return@withContext when (deletedFileFromDeviceResult) {
                                                        is Resource.Success -> {
                                                            returnFailure(
                                                                message = "doWork | Failed to save EmoteDetails as Sticker in Database: ${saveEmoteAsStickerInLocalDatabaseResult.uiText} - Deleted File from Device"
                                                            )
                                                        }

                                                        is Resource.Error -> {
                                                            returnFailure(
                                                                message = "doWork | Failed to save EmoteDetails as Sticker in Database: ${saveEmoteAsStickerInLocalDatabaseResult.uiText} - Could not delete File from Device"
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        is Resource.Error -> {
                                            Log.e(
                                                tag,
                                                "doWork | ${downloadAndScaleBitmapResult.uiText}"
                                            )

                                            return@withContext returnFailure(
                                                message = "doWork | Failed to download and scale Bitmap: ${downloadAndScaleBitmapResult.uiText}"
                                            )
                                        }
                                    }
                                } ?: kotlin.run {
                                    Log.e(tag, "doWork | EmoteDetails undefined")

                                    return@withContext returnFailure(
                                        message = "doWork | EmoteDetails undefined"
                                    )
                                }
                            }

                            is Resource.Error -> {
                                Log.e(tag, "doWork | ${emoteDetailsFromJsonStringResult.uiText}")

                                return@withContext returnFailure(
                                    message = "doWork | Failed to get EmoteDetails from EmoteDetailsJsonString: ${emoteDetailsFromJsonStringResult.uiText}"
                                )
                            }
                        }
                    } ?: kotlin.run {
                        Log.e(tag, "doWork | No EmoteDetailsString provided")

                        return@withContext returnFailure(
                            message = "doWork | No EmoteDetailsString provided"
                        )
                    }
                } ?: kotlin.run {
                    Log.e(tag, "doWork | No DestinationFileName provided")

                    return@withContext returnFailure(
                        message = "doWork | No DestinationFileName provided"
                    )
                }
            } ?: kotlin.run {
                Log.e(tag, "doWork | No DestinationFilesPath provided")

                return@withContext returnFailure(
                    message = "doWork | No DestinationFilesPath provided"
                )
            }
        } ?: kotlin.run {
            Log.e(tag, "doWork | No Uri provided")

            return@withContext returnFailure(message = "doWork | No Uri provided")
        }
    }

    private suspend fun returnFailure(message: String): Result {
        Log.d(tag, "returnFailure | message: $message")

        setProgress(workDataOf(KEY_RESULT_PROGRESS to 100))

        return Result.failure(
            workDataOf(
                KEY_RESULT_TEXT to message,
                KEY_RESULT_SUCCESS to false
            )
        )
    }

    private suspend fun returnSuccess(message: String): Result {
        Log.d(tag, "returnSuccess | message: $message")

        setProgress(workDataOf(KEY_RESULT_PROGRESS to 100))

        return Result.success(
            workDataOf(
                KEY_RESULT_TEXT to message,
                KEY_RESULT_SUCCESS to true
            )
        )
    }

    private fun getEmoteDetailsFromJsonString(jsonString: String): Resource<EmoteDetails> {
        Log.d(tag, "getEmoteDetailsFromJsonString | jsonString: $jsonString")

        return try {
            Resource.Success(data = jsonBuilder.decodeFromString<EmoteDetails>(jsonString))
        } catch (e: SerializationException) {
            Resource.Error(
                uiText = UiText.DynamicString(
                    value = "getEmoteDetailsFromJsonString | SerializationException: ${e.message}\n${e.stackTrace}"
                )
            )
        } catch (e: IllegalArgumentException) {
            Resource.Error(
                uiText = UiText.DynamicString(
                    value = "getEmoteDetailsFromJsonString | IllegalArgumentException: ${e.message}\n${e.stackTrace}"
                )
            )
        }
    }

    private suspend fun saveEmoteAsStickerInLocalDatabase(emoteDetails: EmoteDetails): SimpleResource {
        Log.d(tag, "saveEmoteAsStickerInLocalDatabase | emoteDetails: $emoteDetails")

        return stickerRepository.insertSticker(
            sticker = emoteDetails.toSticker(
                scaledWidth = scaledImageWidth ?: 0,
                scaledHeight = scaledImageHeight ?: 0,
                scaledSize = scaledImageSize ?: 0,
            )
        )
    }

    private fun downloadAndScaleBitmap(
        stringUri: String,
        absoluteDestinationFilePath: String,
        isCachedLocalFile: Boolean,
        compressionThresholdInBytes: Long,
        destinationImageWidth: Int,
        destinationImageHeight: Int,
        bitmapToScaleSizeMultiplier: Double,
    ): SimpleResource {
        Log.d(
            tag,
            "downloadAndScaleBitmap | stringUri: $stringUri, " +
                    "absoluteDestinationFilePath: $absoluteDestinationFilePath, " +
                    "isCachedLocalFile: $isCachedLocalFile, " +
                    "compressionThresholdInBytes: $compressionThresholdInBytes, " +
                    "destinationImageWidth: $destinationImageWidth, " +
                    "destinationImageHeight: $destinationImageHeight, " +
                    "bitmapToScaleSizeMultiplier: $bitmapToScaleSizeMultiplier"
        )

        val inputStream: InputStream? =
            if (isCachedLocalFile) {
                val fileFromStringUri: File = stringUri.toPath().toFile()
                if (fileFromStringUri.exists()) {
                    FileInputStream(fileFromStringUri)
                } else {
                    return Resource.Error(
                        uiText = UiText.DynamicString(
                            value = "downloadAndScaleBitmap | Path doesn't match local file"
                        ),
                        logging = "downloadAndScaleBitmap | Path doesn't match local file"
                    )
                }
            } else {
                try {
                    URL(stringUri).openStream()
                } catch (e: IOException) {
                    return Resource.Error(
                        uiText = UiText.DynamicString(
                            value = "downloadAndScaleBitmap | IOException: ${e.message}\n${e.stackTrace}"
                        ),
                        logging = "downloadAndScaleBitmap | IOException: ${e.message}\n${e.stackTrace}"
                    )
                }
            }

        val imageByteArray = inputStream?.use { stream: InputStream ->
            stream.readBytes()
        } ?: kotlin.run {
            return Resource.Error(
                uiText = UiText.DynamicString(
                    value = "downloadAndScaleBitmap | InputStream is undefined"
                ),
                logging = "downloadAndScaleBitmap | InputStream is undefined"
            )
        }

        val imageBitmap: Bitmap =
            BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)

        val scaledImageByteArrayResult: Resource<ByteArray> = scaleBitmap(
            imageBitmap = imageBitmap,
            destinationWidth = destinationImageWidth,
            destinationHeight = destinationImageHeight,
            bitmapToScaleSizeMultiplier = bitmapToScaleSizeMultiplier,
            maxImageByteSize = compressionThresholdInBytes
        )

        when (scaledImageByteArrayResult) {
            is Resource.Success -> {
                scaledImageByteArrayResult.data?.let { scaledImageByteArray: ByteArray ->
                    // Save ByteArray to file
                    val file = File(absoluteDestinationFilePath)
                    file.writeBytes(scaledImageByteArray)

                    return Resource.Success(data = null)
                } ?: kotlin.run {
                    return Resource.Error(
                        uiText = UiText.DynamicString(
                            value = "downloadAndScaleBitmap | ScaledImageBitmap is undefined"
                        )
                    )
                }
            }

            is Resource.Error -> {
                return Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "downloadAndScaleBitmap | ${scaledImageByteArrayResult.uiText}"
                    )
                )
            }
        }
    }

    private fun scaleBitmap(
        imageBitmap: Bitmap,
        destinationWidth: Int,
        destinationHeight: Int,
        bitmapToScaleSizeMultiplier: Double,
        maxImageByteSize: Long,
    ): Resource<ByteArray> {
        Log.d(
            tag,
            "scaleBitmap | imageBitmap: $imageBitmap, " +
                    "destinationWidth: $destinationWidth, " +
                    "destinationHeight: $destinationHeight, " +
                    "bitmapToScaleSizeMultiplier: $bitmapToScaleSizeMultiplier, " +
                    "maxImageByteSize: $maxImageByteSize"
        )

        val scaledImageBitmap: Bitmap = BitmapHelper.scalePreserveRatio(
            bitmapToScale = imageBitmap,
            destinationWidth = destinationWidth,
            destinationHeight = destinationHeight,
            bitmapToScaleSizeMultiplier = bitmapToScaleSizeMultiplier
        )

        val compressedImageByteArrayResult: Resource<ByteArray> =
            BitmapHelper.webpFullEffortCompression(
                bitmapToCompress = scaledImageBitmap,
                maxImageByteSize = maxImageByteSize
            )

        when (compressedImageByteArrayResult) {
            is Resource.Success -> {
                compressedImageByteArrayResult.data?.let { compressedImageByteArray: ByteArray ->

                    if (compressedImageByteArray.size > maxImageByteSize) {
                        Log.d(
                            tag,
                            "scaleBitmap | compressedImageByteArray.size (${compressedImageByteArray.size}) > maxImageByteSize ($maxImageByteSize)"
                        )

                        val newBitmapToScaleSizeMultiplier =
                            (bitmapToScaleSizeMultiplier - (bitmapToScaleSizeMultiplier * 0.05))

                        if (newBitmapToScaleSizeMultiplier > 0.25) {
                            return scaleBitmap(
                                imageBitmap = imageBitmap,
                                destinationWidth = destinationWidth,
                                destinationHeight = destinationHeight,
                                bitmapToScaleSizeMultiplier = newBitmapToScaleSizeMultiplier,
                                maxImageByteSize = maxImageByteSize
                            )
                        } else {
                            return Resource.Error(
                                uiText = UiText.DynamicString(
                                    value = "scaleBitmap | Image is getting too small"
                                )
                            )
                        }
                    } else {
                        scaledImageWidth = destinationWidth
                        scaledImageHeight = destinationHeight
                        scaledImageSize = compressedImageByteArray.size

                        return Resource.Success(data = compressedImageByteArray)
                    }
                } ?: kotlin.run {
                    return Resource.Error(
                        uiText = UiText.DynamicString(
                            value = "scaleBitmap | CompressedImageByteArray is undefined"
                        )
                    )
                }
            }

            is Resource.Error -> {
                return Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "scaleBitmap | ${compressedImageByteArrayResult.uiText}"
                    )
                )
            }
        }
    }
}

package com.thenamlit.emotesonmymind.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.thenamlit.emotesonmymind.core.domain.repository.StickerRepository
import com.thenamlit.emotesonmymind.core.util.webp_encoder.WebpBitmapEncoder
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
class DownloadAndScaleAnimatedImageWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val stickerRepository: StickerRepository,
    private val jsonBuilder: Json,
) : CoroutineWorker(appContext = appContext, params = params) {
    private val tag =
        Logging.loggingPrefix + DownloadAndScaleAnimatedImageWorker::class.java.simpleName

    // TODO: For Scaling and converting animated WEBP
    //  https://stackoverflow.com/questions/71360520/how-can-i-resize-gif-animated-webp-image-files-in-android
    //
    // TODO: Scaling
    //  https://stackoverflow.com/questions/15440647/scaled-bitmap-maintaining-aspect-ratio/32810187#32810187
    //
    // TODO: Converting Animated WEBP using Glide - https://github.com/bumptech/glide
    //  https://stackoverflow.com/questions/52030982/using-glide-how-can-i-go-over-each-frame-of-gifdrawable-as-bitmap/64744296#64744296

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

                        when (emoteDetailsFromJsonStringResult) {
                            is Resource.Success -> {
                                emoteDetailsFromJsonStringResult.data?.let { emoteDetails: EmoteDetails ->
                                    val downloadAndScaleAnimatedBitmapResult =
                                        downloadAndScaleAnimatedBitmap(
                                            stringUri = stringUri,
                                            stringDestinationFilesPath = stringDestinationFilesPath,
                                            stringDestinationFileName = stringDestinationFileName,
                                            isCachedLocalFile = isCachedLocalFile,
                                            compressionThresholdInBytes = compressionThresholdInBytes,
                                            destinationImageWidth = destinationImageWidth,
                                            destinationImageHeight = destinationImageHeight,
                                            bitmapToScaleSizeMultiplier = bitmapToScaleSizeMultiplier
                                        )

                                    when (downloadAndScaleAnimatedBitmapResult) {
                                        is Resource.Success -> {
                                            Log.d(
                                                tag,
                                                "Successfully downloaded Sticker-Bitmap to Internal Storage"
                                            )

                                            val saveEmoteAsStickerInLocalDatabaseResult =
                                                saveEmoteAsStickerInLocalDatabase(emoteDetails = emoteDetails)

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
                                                    // TODO: Delete saved image from local directory
                                                    Log.e(
                                                        tag,
                                                        "doWork | ${saveEmoteAsStickerInLocalDatabaseResult.uiText}"
                                                    )

                                                    return@withContext returnFailure(
                                                        message = "doWork | Failed to save " +
                                                                "EmoteDetails as Sticker in Database: " +
                                                                "${saveEmoteAsStickerInLocalDatabaseResult.uiText}"
                                                    )
                                                }
                                            }
                                        }

                                        is Resource.Error -> {
                                            Log.e(
                                                tag,
                                                "doWork | ${downloadAndScaleAnimatedBitmapResult.uiText}"
                                            )

                                            return@withContext returnFailure(
                                                message = "doWork | Failed to download and scale " +
                                                        "AnimatedBitmap: " +
                                                        "${downloadAndScaleAnimatedBitmapResult.uiText}"
                                            )
                                        }
                                    }
                                }
                            }

                            is Resource.Error -> {
                                Log.e(tag, "doWork | ${emoteDetailsFromJsonStringResult.uiText}")

                                return@withContext returnFailure(
                                    message = "doWork | Failed to get EmoteDetails from " +
                                            "EmoteDetailsJsonString: ${emoteDetailsFromJsonStringResult.uiText}"
                                )
                            }
                        }
                    }


//                    val absoluteDestinationFilePath =
//                        "${appContext.filesDir}/$stringDestinationFilesPath/$stringDestinationFileName"
//
//                    val scaleResult = scale(
//                        isCachedLocalFile = isCachedLocalFile,
//                        stringUri = stringUri,
//                        absoluteDestinationFilePath = absoluteDestinationFilePath,
//                        destinationWidth = destinationImageWidth,
//                        destinationHeight = destinationImageHeight,
//                        maxImageByteSize = compressionThresholdInBytes,
//                        bitmapToScaleSizeMultiplier = bitmapToScaleSizeMultiplier
//                    )
//
//                    when (scaleResult) {
//                        is Resource.Success -> {
//                            // TODO
//                            Log.d(tag, "SUCCESS???: ${scaleResult.data}")
//                        }
//
//                        is Resource.Error -> {
//                            // TODO
//                            Log.e(tag, "ERROR: ${scaleResult.uiText}")
//                        }
//                    }
                } ?: kotlin.run {
                    Log.e(tag, "doWork | No StringDestinationFileName provided")

                    return@withContext returnFailure(
                        message = "doWork | No StringDestinationFileName provided"
                    )
                }
            } ?: kotlin.run {
                Log.e(tag, "doWork | No StringDestinationFilesPath provided")

                return@withContext returnFailure(
                    message = "doWork | No StringDestinationFilesPath provided"
                )
            }
        } ?: kotlin.run {
            Log.e(tag, "doWork | No StringUri provided")

            return@withContext returnFailure(
                message = "doWork | No StringUri provided"
            )
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

    private fun downloadAndScaleAnimatedBitmap(
        stringUri: String,
        stringDestinationFilesPath: String,
        stringDestinationFileName: String,
        isCachedLocalFile: Boolean,
        compressionThresholdInBytes: Long,
        destinationImageWidth: Int,
        destinationImageHeight: Int,
        bitmapToScaleSizeMultiplier: Double,
    ): SimpleResource {
        Log.d(
            tag,
            "downloadAndScaleAnimatedBitmap | stringUri: $stringUri, " +
                    "stringDestinationFilesPath: $stringDestinationFilesPath, " +
                    "stringDestinationFileName: $stringDestinationFileName, " +
                    "isCachedLocalFile: $isCachedLocalFile, " +
                    "compressionThresholdInBytes: $compressionThresholdInBytes, " +
                    "destinationImageWidth: $destinationImageWidth, " +
                    "destinationImageHeight: $destinationImageHeight, " +
                    "bitmapToScaleSizeMultiplier: $bitmapToScaleSizeMultiplier"
        )

        val absoluteDestinationFilePath =
            "${appContext.filesDir}/$stringDestinationFilesPath/$stringDestinationFileName"

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

        val imageByteArray: ByteArray =
            inputStream?.use { stream: InputStream ->
                stream.readBytes()
            } ?: kotlin.run {
                return Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "downloadAndScaleBitmap | InputStream is undefined"
                    )
                )
            }

        // TODO: Implement scaling...that actually works :D
//        val imageBitmap: Bitmap =
//            BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)

        // Save ByteArray to file
        val file = File(absoluteDestinationFilePath)
        file.writeBytes(imageByteArray)

        // TODO: Put into scale once it works
        scaledImageWidth = destinationImageWidth
        scaledImageHeight = destinationImageHeight
        scaledImageSize = imageByteArray.size

        return Resource.Success(data = null)
    }

    private suspend fun scale(
        isCachedLocalFile: Boolean,
        stringUri: String,
        absoluteDestinationFilePath: String,
        destinationWidth: Int,
        destinationHeight: Int,
        maxImageByteSize: Long,
        bitmapToScaleSizeMultiplier: Double,
    ): SimpleResource {
        Log.d(
            tag,
            "scale | isCachedLocalFile: $isCachedLocalFile, " +
                    "stringUri: $stringUri, " +
                    "absoluteDestinationFilePath: $absoluteDestinationFilePath, " +
                    "destinationWidth: $destinationWidth, " +
                    "destinationHeight: $destinationHeight, " +
                    "maxImageByteSize: $maxImageByteSize, " +
                    "bitmapToScaleSizeMultiplier: $bitmapToScaleSizeMultiplier"
        )

        val scaleAnimatedResult = scaleAnimated(
            isCachedLocalFile = isCachedLocalFile,
            stringUri = stringUri,
            absoluteDestinationFilePath = absoluteDestinationFilePath,
            destinationWidth = destinationWidth,
            destinationHeight = destinationHeight,
            maxImageByteSize = maxImageByteSize,
            bitmapToScaleSizeMultiplier = bitmapToScaleSizeMultiplier
        )


        when (scaleAnimatedResult) {
            is Resource.Success -> {
                scaleAnimatedResult.data?.let { scaleAnimatedTotalBytes: Int ->
                    Log.d(tag, "scale | Got Bytes: $scaleAnimatedTotalBytes")

                    if (scaleAnimatedTotalBytes > maxImageByteSize) {
                        Log.d(
                            tag,
                            "scale | scaleAnimatedTotalBytes ($scaleAnimatedTotalBytes) > maxImageByteSize ($maxImageByteSize)"
                        )

                        val newBitmapToScaleSizeMultiplier =
                            (bitmapToScaleSizeMultiplier - (bitmapToScaleSizeMultiplier * 0.05))

                        if (newBitmapToScaleSizeMultiplier > 0.25) {
                            return scale(
                                isCachedLocalFile = isCachedLocalFile,
                                stringUri = stringUri,
                                absoluteDestinationFilePath = absoluteDestinationFilePath,
                                destinationWidth = destinationWidth,
                                destinationHeight = destinationHeight,
                                maxImageByteSize = maxImageByteSize,
                                bitmapToScaleSizeMultiplier = newBitmapToScaleSizeMultiplier
                            )
                        } else {
                            return Resource.Error(
                                uiText = UiText.DynamicString(
                                    value = "scale | Bitmap is getting too small"
                                )
                            )
                        }
                    } else {
                        // TODO: DOWNLOAD
                        Log.d(
                            tag,
                            "scale | scaleAnimatedTotalBytes: $scaleAnimatedTotalBytes is " +
                                    "smaller than maxImageByteSize: $maxImageByteSize -> DOWNLOAD (TODO)"
                        )
                        return Resource.Success(data = null)
                    }
                } ?: kotlin.run {
                    return Resource.Error(
                        uiText = UiText.DynamicString(
                            value = "scale | ScaleAnimatedTotalBytes is undefined"
                        )
                    )
                }
            }

            is Resource.Error -> {
                Log.e(tag, "ERR?")

                return Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "scale | ${scaleAnimatedResult.uiText}"
                    )
                )
            }
        }
    }

    // Thanks to https://stackoverflow.com/a/64744296
    private suspend fun scaleAnimated(
        isCachedLocalFile: Boolean,
        stringUri: String,
        absoluteDestinationFilePath: String,
        destinationWidth: Int,
        destinationHeight: Int,
        maxImageByteSize: Long,
        bitmapToScaleSizeMultiplier: Double,
    ): Resource<Int> = withContext(Dispatchers.IO) {
        Log.d(
            tag,
            "scaleAnimated | isCachedLocalFile: $isCachedLocalFile, " +
                    "stringUri: $stringUri, " +
                    "absoluteDestinationFilePath: $absoluteDestinationFilePath, " +
                    "destinationWidth: $destinationWidth, " +
                    "destinationHeight: $destinationHeight, " +
                    "maxImageByteSize: $maxImageByteSize, " +
                    "bitmapToScaleSizeMultiplier: $bitmapToScaleSizeMultiplier"
        )

        // TODO: Implement check for local or remote -> Add File.fetch for remote
        val fileFromStringUri: File = stringUri.toPath().toFile()

        if (fileFromStringUri.exists()) {
            FileInputStream(fileFromStringUri)
        } else {
            return@withContext Resource.Error(
                uiText = UiText.DynamicString(
                    value = "scaleAnimated | File doesn't exist"
                )
            )

        }

        val drawable =
            Glide
                .with(appContext)
                .load(fileFromStringUri)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .submit()
                .get() as WebpDrawable

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, bitmap.width, bitmap.height)
        drawable.loopCount = 1

        var totalByteArraySize = 0
        var returnResult = false

        val bitmapFrameList = mutableListOf<Bitmap>()

        val callback = object : Drawable.Callback {
            override fun invalidateDrawable(who: Drawable) {
                val webp = who as WebpDrawable
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                who.draw(canvas)
                Log.d(
                    tag,
                    "frameIndex:${webp.frameIndex} frameCount:${webp.frameCount} firstFrame:${webp.firstFrame}"
                )

                // Image is available here on the bitmap object
//                val frameBitmap: Bitmap = webp.toBitmap()

                bitmapFrameList.add(webp.toBitmap())

                if (webp.frameIndex == webp.frameCount - 1 || webp.frameIndex >= 299) {
                    Log.d(tag, "FRAME INDEX == FRAME COUNT")
                    returnResult = true
                }
            }

            override fun scheduleDrawable(who: Drawable, p1: Runnable, p2: Long) {
                TODO("Not yet implemented")
            }

            override fun unscheduleDrawable(who: Drawable, p1: Runnable) {
                TODO("Not yet implemented")
            }
        }

        drawable.callback = callback
        drawable.start()

        while (!returnResult) {
            Log.d(tag, "Waiting for result...")
            kotlinx.coroutines.delay(100L)
        }

        // TODO: Idk if this actually works already...lol
        //  https://github.com/b4rtaz/android-webp-encoder/tree/master
        val webpBitmapEncoder = WebpBitmapEncoder(file = File(absoluteDestinationFilePath))
        webpBitmapEncoder.setLoops(loops = 0)
        webpBitmapEncoder.setDuration(duration = 90)

        Log.d(tag, "bitmapFrameListSize: ${bitmapFrameList.size}")
        bitmapFrameList.forEachIndexed { index: Int, bitmapFrame: Bitmap ->
            webpBitmapEncoder.writeFrame(frame = bitmapFrame, compress = 100)
            // TODO: IMPORTANT!!!
            //  IMPLEMENT:
            //  https://github.com/b4rtaz/android-webp-encoder/pull/5/commits/fda68eec83e2dd589054e01c614f98e986dfa9e0
            //  MAYBE THESE CHANGES WILL HELP FINALLY!?

            // TODO: Add back once a solution is found for building a new animated WEBP from the bitmap-list
            /*
                        val scaleBitmapFrameByteArrayResult: Resource<ByteArray> = scaleBitmapFrame(
                            imageBitmap = bitmapFrame,
                            destinationWidth = destinationWidth,
                            destinationHeight = destinationHeight,
                            bitmapToScaleSizeMultiplier = bitmapToScaleSizeMultiplier,
                            maxImageByteSize = maxImageByteSize
                        )

                        when (scaleBitmapFrameByteArrayResult) {
                            is Resource.Success -> {
                                scaleBitmapFrameByteArrayResult.data?.let { scaleBitmapFrameByteArray: ByteArray ->
                                    Log.d(tag, "scaleAnimated.invalidateDrawable | Success")

            //                        webpBitmapEncoder.writeFrame(frame = bitmapFrame, compress = 100)
            //                        webpBitmapEncoder.writeFrameWithByteArray(
            //                            byteArray = scaleBitmapFrameByteArray
            //                        )

            //                        totalByteArraySize += scaleBitmapFrameByteArray.size
            //                        if (totalByteArraySize > maxImageByteSize) {
            //                            Log.e(tag, "scaleAnimated | Already too big at index $index :(")
            //                            return@withContext Resource.Success(data = totalByteArraySize)
            //                        }
                                    // TODO: Add ByteArray as Bitmap to WEBP via Library - TRY THIS NEXT AND SEE RESULT BEFORE TRYING TO COMPRESS MORE
                                    //  PROBABLY SOMETHING WRONG WITH MY LOGIC ATM...MAYBE IT'S ACTUALLY NOT SO BIG??
                                } ?: kotlin.run {
                                    Log.e(
                                        tag,
                                        "scaleAnimated.invalidateDrawable | ScaleBitmapFrameByteArray is undefined"
                                    )
                                }
                            }

                            is Resource.Error -> {
                                Log.e(
                                    tag,
                                    "scaleAnimated.invalidateDrawable | ${scaleBitmapFrameByteArrayResult.uiText}"
                                )
                            }
                        }*/
        }

        webpBitmapEncoder.close()

        return@withContext Resource.Success(data = totalByteArraySize)
    }

    private fun scaleBitmapFrame(
        imageBitmap: Bitmap,
        destinationWidth: Int,
        destinationHeight: Int,
        bitmapToScaleSizeMultiplier: Double,
        maxImageByteSize: Long,
    ): Resource<ByteArray> {
        Log.d(
            tag,
            "scaleBitmapFrame | imageBitmap: $imageBitmap, " +
                    "destinationWidth: $destinationWidth, " +
                    "destinationHeight: $destinationHeight, " +
                    "maxImageByteSize: $maxImageByteSize, " +
                    "bitmapToScaleSizeMultiplier: $bitmapToScaleSizeMultiplier"
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
                    return Resource.Success(data = compressedImageByteArray)
                } ?: kotlin.run {
                    return Resource.Error(
                        uiText = UiText.DynamicString(
                            value = "scaleBitmapFrame | CompressedImageByteArray is undefined"
                        )
                    )
                }
            }

            is Resource.Error -> {
                return Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "scaleBitmapFrame | ${compressedImageByteArrayResult.uiText}"
                    )
                )
            }
        }
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
}

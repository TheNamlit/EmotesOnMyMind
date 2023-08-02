package com.thenamlit.emotesonmymind.core.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.Log
import java.io.ByteArrayOutputStream
import kotlin.math.floor
import kotlin.math.roundToInt


object BitmapHelper {
    private val tag = Logging.loggingPrefix + BitmapHelper::class.java.simpleName


    // Thanks to https://stackoverflow.com/a/32810187
    fun scalePreserveRatio(
        bitmapToScale: Bitmap,
        destinationWidth: Int,
        destinationHeight: Int,
        bitmapToScaleSizeMultiplier: Double,
    ): Bitmap {
        Log.d(
            tag,
            "scalePreserveRatio | bitmapToScale: $bitmapToScale, destinationWidth: $destinationWidth, destinationHeight: $destinationHeight, bitmapToScaleSizeMultiplier: $bitmapToScaleSizeMultiplier"
        )

        val width: Int = bitmapToScale.width
        val height: Int = bitmapToScale.height

        // Scaling the bitmap size
        val scaledBitmapDestinationWidth = destinationWidth * bitmapToScaleSizeMultiplier
        val scaledBitmapDestinationHeight = destinationHeight * bitmapToScaleSizeMultiplier

        // Calculate the max changing amount and decide which dimension to use
        val widthRatio: Float = scaledBitmapDestinationWidth.toFloat() / width.toFloat()
        val heightRatio: Float = scaledBitmapDestinationHeight.toFloat() / height.toFloat()

        // Use the ratio that will fit the image into the desired sizes
        var finalWidth: Int = floor(width.toFloat() * widthRatio).roundToInt()
        var finalHeight: Int = floor(height.toFloat() * widthRatio).roundToInt()
        if (finalWidth > scaledBitmapDestinationWidth || finalHeight > scaledBitmapDestinationHeight) {
            finalWidth = floor(width.toFloat() * heightRatio).roundToInt()
            finalHeight = floor(height.toFloat() * heightRatio).roundToInt()
        }

        // Scale given bitmap to fit into the desired area
        val scaleBitmapToScale =
            Bitmap.createScaledBitmap(bitmapToScale, finalWidth, finalHeight, true)

        // Created a bitmap with desired sizes
        val scaledImage: Bitmap =
            Bitmap.createBitmap(destinationWidth, destinationHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(scaledImage)

        // Draw background color
        val paint = Paint()
        paint.color = Color.TRANSPARENT
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)

        // Calculate the ratios and decide which part will have empty areas (width or height)
        val ratioBitmap: Float = finalWidth.toFloat() / finalHeight.toFloat()
        val destinationRatio: Float = destinationWidth.toFloat() / destinationHeight.toFloat()
        val left: Float =
            if (ratioBitmap > destinationRatio) {
                0f
            } else {
                (destinationWidth - finalWidth).toFloat() / 2
            }
        val top: Float =
            if (ratioBitmap < destinationRatio) {
                0f
            } else {
                (destinationHeight - finalHeight).toFloat() / 2
            }

        canvas.drawBitmap(scaleBitmapToScale, left, top, null)

        return scaledImage
    }

    fun webpFullEffortCompression(
        bitmapToCompress: Bitmap,
        maxImageByteSize: Long,
    ): Resource<ByteArray> {
        Log.d(
            tag,
            "webpFullEffortCompression | bitmapToCompress: $bitmapToCompress, maxImageByteSize: $maxImageByteSize"
        )

        // Quality with WEBP_LOSSLESS is measured as effort for the compression and not actual quality
        // So it's probably best to just go for 100 and if that doesn't work, re-scale the image and try again
        // https://developer.android.com/reference/android/graphics/Bitmap.CompressFormat#WEBP_LOSSLESS

        var outputBytes: ByteArray
        val compressionEffortQuality = 100
        val byteOutputStream = ByteArrayOutputStream()
        byteOutputStream.use { outputStream ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                bitmapToCompress.compress(
                    Bitmap.CompressFormat.WEBP_LOSSLESS,
                    compressionEffortQuality,
                    outputStream
                )
            } else {
                bitmapToCompress.compress(
                    Bitmap.CompressFormat.WEBP,
                    compressionEffortQuality,
                    outputStream
                )
            }
            outputBytes = outputStream.toByteArray()
        }

        return Resource.Success(data = outputBytes)
    }
}

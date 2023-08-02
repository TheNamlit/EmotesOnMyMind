package com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection_details

import android.util.Log
import com.thenamlit.emotesonmymind.core.domain.repository.DeviceStorageRepository
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import java.io.File
import javax.inject.Inject


class GetLocalStickerImageFileUseCase @Inject constructor(
    private val deviceStorageRepository: DeviceStorageRepository,
) {
    private val tag = Logging.loggingPrefix + GetLocalStickerImageFileUseCase::class.java.simpleName

    operator fun invoke(path: String): Resource<File> {
        Log.d(tag, "invoke | path: $path")

        return deviceStorageRepository.getFile(path = path)
    }
}

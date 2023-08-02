package com.thenamlit.emotesonmymind.features.sticker.domain.use_case

import android.util.Log
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.repository.StickerRepository
import com.thenamlit.emotesonmymind.core.util.Logging
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetStickerUseCase @Inject constructor(
    private val stickerRepository: StickerRepository,
) {
    private val tag = Logging.loggingPrefix + GetStickerUseCase::class.java.simpleName

    fun getAllAnimated(): Flow<List<Sticker>> {
        Log.d(tag, "getAllAnimated")

        return stickerRepository.getAllAnimated()
    }

    fun getAllNotAnimated(): Flow<List<Sticker>> {
        Log.d(tag, "getAllNotAnimated")

        return stickerRepository.getAllNotAnimated()
    }

    fun getAllNotInIdList(idList: List<String>, animated: Boolean): Flow<List<Sticker>> {
        Log.d(tag, "getAllNotInIdList | idList: $idList, animated: $animated")

        return stickerRepository.getAllNotInIdList(idList = idList, animated = animated)
    }
}

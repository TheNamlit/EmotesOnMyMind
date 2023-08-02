package com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection

import android.util.Log
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.domain.repository.StickerCollectionRepository
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import com.thenamlit.emotesonmymind.core.util.UiText
import javax.inject.Inject


class CreateCollectionUseCase @Inject constructor(
    private val stickerCollectionRepository: StickerCollectionRepository,
) {
    private val tag = Logging.loggingPrefix + CreateCollectionUseCase::class.java.simpleName

    suspend operator fun invoke(name: String, animated: Boolean): SimpleResource {
        Log.d(tag, "invoke | name: $name, animated: $animated")

        return if (name.isNotBlank()) {
            stickerCollectionRepository.createStickerCollection(name = name, animated = animated)
        } else {
            Resource.Error(
                uiText = UiText.StringResource(
                    id = R.string.create_collection_use_case_invoke_sticker_collection_name_blank_error
                ),
                logging = "invoke | New Collection-Name was blank"
            )
        }
    }
}

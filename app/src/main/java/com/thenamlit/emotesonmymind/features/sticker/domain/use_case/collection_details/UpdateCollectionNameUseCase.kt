package com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection_details

import android.util.Log
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.domain.repository.StickerCollectionRepository
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import com.thenamlit.emotesonmymind.core.util.UiText
import javax.inject.Inject


class UpdateCollectionNameUseCase @Inject constructor(
    private val stickerCollectionRepository: StickerCollectionRepository,
) {
    private val tag = Logging.loggingPrefix + UpdateCollectionNameUseCase::class.java.simpleName

    suspend operator fun invoke(id: String, name: String): SimpleResource {
        Log.d(tag, "invoke | id: $id, name: $name")

        return if (name.isNotBlank()) {
            return stickerCollectionRepository.updateName(id = id, name = name)
        } else {
            Resource.Error(
                uiText = UiText.StringResource(
                    id = R.string.update_collection_name_use_case_invoke_sticker_collection_name_blank_error
                ),
                logging = "invoke | Updated Collection-Name was blank"
            )
        }
    }
}

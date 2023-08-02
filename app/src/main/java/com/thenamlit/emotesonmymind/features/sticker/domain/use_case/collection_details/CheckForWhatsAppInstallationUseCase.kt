package com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection_details

import android.content.Context
import android.content.Intent
import android.util.Log
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import com.thenamlit.emotesonmymind.core.util.UiText
import com.thenamlit.emotesonmymind.core.util.WhatsAppSettings
import javax.inject.Inject


class CheckForWhatsAppInstallationUseCase @Inject constructor(
    private val context: Context,
) {
    private val tag =
        Logging.loggingPrefix + CheckForWhatsAppInstallationUseCase::class.java.simpleName

    operator fun invoke(): SimpleResource {
        Log.d(tag, "invoke")

        // https://youtu.be/2hIY1xuImuQ?t=629
        val whatsAppIntent = Intent()
        whatsAppIntent.also {
            it.`package` = WhatsAppSettings.CONSUMER_WHATSAPP_PACKAGE_NAME
        }
        val packageManager = context.packageManager

        return if (whatsAppIntent.resolveActivity(packageManager) != null) {
            Resource.Success(data = null)
        } else {
            // TODO: Add Link to WhatsApp installation on PlayStore?
            return Resource.Error(
                uiText = UiText.StringResource(
                    id = R.string.check_for_whats_app_installation_use_case_error
                ),
                logging = "CheckForWhatsAppInstallationUseCase | Couldn't find WhatsApp-Installation."
            )
        }
    }
}

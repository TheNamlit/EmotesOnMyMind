package com.thenamlit.emotesonmymind.features.auth.domain.use_case

import android.util.Log
import com.thenamlit.emotesonmymind.core.util.Logging
import kotlinx.coroutines.delay
import javax.inject.Inject


class AuthenticateUseCase @Inject constructor(

) {
    private val tag = Logging.loggingPrefix + AuthenticateUseCase::class.java.simpleName

    suspend fun execute(): Boolean {
        Log.d(tag, "execute")
        // TODO: Actually authenticate, just dummy atm

        delay(4000L)
        return true
    }
}

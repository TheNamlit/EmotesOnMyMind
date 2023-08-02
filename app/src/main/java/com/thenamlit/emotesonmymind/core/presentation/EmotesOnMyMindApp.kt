package com.thenamlit.emotesonmymind.core.presentation

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.thenamlit.emotesonmymind.core.util.Logging
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class EmotesOnMyMindApp : Application(), Configuration.Provider {
    private val tag = Logging.loggingPrefix + EmotesOnMyMindApp::class.java.simpleName

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration {
        Log.d(tag, "getWorkManagerConfiguration")

        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}

package com.thenamlit.emotesonmymind.features.profile.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import com.thenamlit.emotesonmymind.core.util.Logging
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ProfileScreenViewModel @Inject constructor(

) : ViewModel() {
    private val tag = Logging.loggingPrefix + ProfileScreenViewModel::class.java.simpleName

    init {
        Log.d(tag, "init")
    }
}

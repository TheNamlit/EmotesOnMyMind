package com.thenamlit.emotesonmymind.features.auth.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thenamlit.emotesonmymind.core.presentation.util.NavigationEvent
import com.thenamlit.emotesonmymind.features.auth.domain.use_case.AuthenticateUseCase
import com.thenamlit.emotesonmymind.features.destinations.MainFeedScreenDestination
import com.thenamlit.emotesonmymind.features.destinations.ProfileScreenDestination
import com.thenamlit.emotesonmymind.core.util.Logging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val authenticateUseCase: AuthenticateUseCase,
) : ViewModel() {
    private val tag = Logging.loggingPrefix + SplashScreenViewModel::class.java.simpleName

    private val _splashScreenEventFlow = MutableSharedFlow<NavigationEvent>()
    val splashScreenEventFlow = _splashScreenEventFlow.asSharedFlow()

    init {
        Log.d(tag, "init")

        simulateStartup()
    }

    private fun simulateStartup() {
        Log.d(tag, "simulateStartup")

        viewModelScope.launch(Dispatchers.IO) {
            if (authenticateUseCase.execute()) {
                _splashScreenEventFlow.emit(NavigationEvent.Navigate(destination = MainFeedScreenDestination()))
            } else {
                // TODO: Change to LoginScreen or so
                _splashScreenEventFlow.emit(NavigationEvent.Navigate(destination = ProfileScreenDestination))
            }
        }
    }
}

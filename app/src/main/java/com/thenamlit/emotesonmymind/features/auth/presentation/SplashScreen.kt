package com.thenamlit.emotesonmymind.features.auth.presentation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
import com.thenamlit.emotesonmymind.core.presentation.util.UiEvent
import com.thenamlit.emotesonmymind.core.util.Logging
import kotlinx.coroutines.flow.SharedFlow


private const val tag = "${Logging.loggingPrefix}SplashScreen"

@RootNavGraph(start = true)
@Destination
@Composable
fun SplashScreen(
    navigator: DestinationsNavigator,
    navController: NavController,
    viewModel: SplashScreenViewModel = hiltViewModel(),
) {
    Log.d(
        tag,
        "SplashScreen | navigator: $navigator, navController: $navController, viewModel: $viewModel"
    )

    CollectSplashScreenEvents(
        splashScreenEventFlow = viewModel.splashScreenEventFlow,
        onNavigate = { direction: Direction ->
            navController.popBackStack()
            navigator.navigate(direction = direction)
        }
    )

    SplashScreenScaffold()
}

@Composable
private fun CollectSplashScreenEvents(
    splashScreenEventFlow: SharedFlow<UiEvent>,
    onNavigate: (Direction) -> Unit,
) {
    Log.d(
        tag,
        "collectSplashScreenEvents | splashScreenEventFlow: $splashScreenEventFlow, onNavigate: $onNavigate"
    )

    // key1 = true -> This is only called once in the entire Screen-Composition
    // When using a variable as key instead -> whenever variable changes, recomposition is triggered
    // https://youtu.be/gxWcfz3V2QE?t=214
    LaunchedEffect(key1 = true) {
        splashScreenEventFlow.collect { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    Log.d(tag, "CollectSplashScreenEvents | Navigate")
                    onNavigate(event.destination)
                }

                else -> {
                    Log.d(tag, "CollectSplashScreenEvents | Other Event")
                }
            }
        }
    }
}

@Composable
private fun SplashScreenScaffold() {
    Log.d(tag, "SplashScreenScaffold")

    Scaffold { innerPadding ->
        SplashScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

@Composable
private fun SplashScreenContent(modifier: Modifier) {
    Log.d(tag, "SplashScreenContent | modifier: $modifier")

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(text = "Two, Four, Seven")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(text = "Three, Six, Five")
        }
    }
}

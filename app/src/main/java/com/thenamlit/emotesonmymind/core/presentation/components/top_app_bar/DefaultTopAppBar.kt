package com.thenamlit.emotesonmymind.core.presentation.components.top_app_bar

import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.thenamlit.emotesonmymind.core.util.Logging


private const val tag = "${Logging.loggingPrefix}DefaultTopAppBar"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopAppBar(
    titleText: String,
    navigationIcon: @Composable () -> Unit = {},
) {
    Log.d(tag, "DefaultTopAppBar | titleText: $titleText")

    TopAppBar(
        title = {
            Text(text = titleText)
        },
        navigationIcon = { navigationIcon() }
    )
}

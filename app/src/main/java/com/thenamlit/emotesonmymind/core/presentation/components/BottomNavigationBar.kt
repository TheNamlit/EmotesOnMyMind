package com.thenamlit.emotesonmymind.core.presentation.components

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.features.NavGraphs
import com.thenamlit.emotesonmymind.features.appCurrentDestinationAsState
import com.thenamlit.emotesonmymind.features.destinations.Destination
import com.thenamlit.emotesonmymind.features.destinations.LibraryScreenDestination
import com.thenamlit.emotesonmymind.features.destinations.MainFeedScreenDestination
import com.thenamlit.emotesonmymind.features.destinations.ProfileScreenDestination
import com.thenamlit.emotesonmymind.features.startAppDestination


private const val tag = "${Logging.loggingPrefix}BottomNavigationBar"

@Composable
fun BottomNavigationBar(navController: NavController, navigator: DestinationsNavigator) {
    Log.d(tag, "BottomNavigationBar | navController: $navController, navigator: $navigator")

    val currentDestination: Destination =
        navController.appCurrentDestinationAsState().value ?: NavGraphs.root.startAppDestination

    NavigationBar(modifier = Modifier.fillMaxWidth()) {
        BottomNavigationBarDestination.values()
            .forEach { bottomNavigationBarDestination: BottomNavigationBarDestination ->
                Log.d(
                    tag,
                    "BottomNavigationBar | currentDestination: $currentDestination, " +
                            "bottomNavigationBarDestination: ${bottomNavigationBarDestination.direction}"
                )

                val selected = currentDestination == bottomNavigationBarDestination.direction
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            Log.d(
                                tag,
                                "Navigating to ${bottomNavigationBarDestination.direction}"
                            )

                            navigator.navigate(bottomNavigationBarDestination.direction)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = bottomNavigationBarDestination.icon,
                            contentDescription = stringResource(
                                id = bottomNavigationBarDestination.label
                            ),
                        )
                    },
                )
            }
    }
}

enum class BottomNavigationBarDestination(
    val direction: DirectionDestinationSpec,
    val icon: ImageVector,
    @StringRes val label: Int,
) {
    LibraryScreen(
        LibraryScreenDestination,
        Icons.Filled.List,
        R.string.bottom_navigation_sticker_collections
    ),
    MainFeedScreen(
        MainFeedScreenDestination,
        Icons.Default.Home,
        R.string.bottom_navigation_main_feed
    ),
    ProfileScreen(
        ProfileScreenDestination,
        Icons.Filled.Person,
        R.string.bottom_navigation_profile
    )
}

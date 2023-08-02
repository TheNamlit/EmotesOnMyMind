package com.thenamlit.emotesonmymind.features.profile.presentation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.presentation.components.BottomNavigationBar
import com.thenamlit.emotesonmymind.core.util.Logging

/*
 Idea on how to get Collections from UserProfile (while they are in another feature)

Project-Flow (SocialMediaApp from Philipp Lackner) :
feature/Profile.presentation -> ProfileScreen
feature/Profile.presentation -> ProfileViewModel -> getPostsForProfile
feature/Profile.domain.use_case -> GetPostsForProfileUseCase.execute()
core.domain.repository -> ProfileRepository.getPostsPaged()
core.data.repository -> ProfileRepositoryImpl.getPostsPaged()
feature/Post.data.remote -> PostApi.getPostsForProfile

Conclusion:
It is okay to go from feature -> core -> back to feature
 */


private const val tag = "${Logging.loggingPrefix}ProfileScreen"

@Destination
@Composable
fun ProfileScreen(
    navigator: DestinationsNavigator,
    navController: NavController,
    viewModel: ProfileScreenViewModel = hiltViewModel(),
) {
    Log.d(
        tag,
        "ProfileScreen | navigator: $navigator, " +
                "navController: $navController, " +
                "viewModel: $viewModel"
    )

    ProfileScreenScaffold(
        navigationBar = {
            BottomNavigationBar(
                navController = navController,
                navigator = navigator,
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreenScaffold(
    navigationBar: @Composable () -> Unit,
) {
    Log.d(
        tag,
        "MainFeedScreenScaffold | navigationBar: $navigationBar"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.profile_screen_top_app_bar_title_text))
                },
            )
        },
        bottomBar = {
            navigationBar()
        }
    ) { innerPadding ->
        ProfileScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
private fun ProfileScreenContent(modifier: Modifier) {
    Log.d(tag, "ProfileScreenContent | modifier: $modifier")

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "ProfileScreen")
        Text(text = "Coming soon... :)")
    }
}

package com.thenamlit.emotesonmymind.features.sticker.presentation.collection_details

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.presentation.components.DefaultFloatingActionButton
import com.thenamlit.emotesonmymind.core.presentation.components.top_app_bar.DefaultTopAppBar
import com.thenamlit.emotesonmymind.core.presentation.components.top_app_bar.DefaultTopAppBarNavigationIcon
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.WhatsAppSettings


private const val tag = "${Logging.loggingPrefix}StickerCollectionDetailsScreenScaffold"

@Composable
fun StickerCollectionDetailsScreenScaffold(
    modifier: Modifier = Modifier,
    mode: StickerCollectionDetailsMode,
    collectionName: String,
    stickerAmount: Int,
    editModeCollectionName: String,
    onNavigationIconClicked: () -> Unit,
    onBottomAppBarAddButtonClicked: () -> Unit,
    onBottomAppBarRemoveButtonClicked: () -> Unit,
    onBottomAppBarEditButtonClicked: () -> Unit,
    onCollectionNameChanged: (newCollectionName: String) -> Unit,
    onEditModeSaveButtonClicked: () -> Unit,
    onEditModeDeleteButtonClicked: () -> Unit,
    onEditModeCancelButtonClicked: () -> Unit,
    onDeleteStickerModeSaveButtonClicked: () -> Unit,
    onDeleteStickerModeCancelButtonClicked: () -> Unit,
    onAddToWhatsAppButtonClicked: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Log.d(
        tag, "StickerCollectionDetailsScreenScaffold | modifier: $modifier, " +
                "mode: $mode, " +
                "collectionName: $collectionName, " +
                "stickerAmount: $stickerAmount, " +
                "editModeCollectionName: $editModeCollectionName"
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            when (mode) {
                is StickerCollectionDetailsMode.Normal -> {
                    DefaultTopAppBar(
                        titleText = "$collectionName " +
                                "($stickerAmount/${WhatsAppSettings.MAXIMUM_STICKER_AMOUNT})",
                        navigationIcon = {
                            DefaultTopAppBarNavigationIcon(
                                onClick = { onNavigationIconClicked() },
                                icon = Icons.Default.ArrowBack,
                                iconContentDescription = R.string.sticker_collection_details_screen_top_app_bar_navigation_icon_content_description
                            )
                        }
                    )
                }

                is StickerCollectionDetailsMode.Edit -> {
                    StickerCollectionDetailsTopAppBarInEditMode(
                        collectionName = editModeCollectionName,
                        onCollectionNameChanged = onCollectionNameChanged,
                        onEditModeSaveButtonClicked = onEditModeSaveButtonClicked,
                        onEditModeCancelButtonClicked = onEditModeCancelButtonClicked,
                        onEditModeDeleteButtonClicked = onEditModeDeleteButtonClicked
                    )
                }

                is StickerCollectionDetailsMode.DeleteSticker -> {
                    StickerCollectionDetailsTopAppBarInDeleteStickerMode(
                        onDeleteStickerModeSaveButtonClicked = onDeleteStickerModeSaveButtonClicked,
                        onDeleteStickerModeCancelButtonClicked = onDeleteStickerModeCancelButtonClicked,
                    )
                }
            }
        },
        bottomBar = {
            when (mode) {
                is StickerCollectionDetailsMode.Normal -> {
                    StickerCollectionDetailsBottomAppBar(
                        onBottomAppBarAddButtonClicked = onBottomAppBarAddButtonClicked,
                        onBottomAppBarRemoveButtonClicked = onBottomAppBarRemoveButtonClicked,
                        onBottomAppBarEditButtonClicked = onBottomAppBarEditButtonClicked,
                        onAddToWhatsAppButtonClicked = onAddToWhatsAppButtonClicked
                    )
                }

                is StickerCollectionDetailsMode.Edit -> {}

                is StickerCollectionDetailsMode.DeleteSticker -> {}
            }
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}

@Composable
private fun StickerCollectionDetailsBottomAppBar(
    onBottomAppBarAddButtonClicked: () -> Unit,
    onBottomAppBarRemoveButtonClicked: () -> Unit,
    onBottomAppBarEditButtonClicked: () -> Unit,
    onAddToWhatsAppButtonClicked: () -> Unit,
) {
    Log.d(tag, "StickerCollectionDetailsBottomAppBar")

    BottomAppBar(
        actions = {
            IconButton(onClick = { onBottomAppBarAddButtonClicked() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Icon")
            }

            IconButton(onClick = { onBottomAppBarRemoveButtonClicked() }) {
                Icon(Icons.Default.Remove, contentDescription = "Remove Icon")
            }

            IconButton(onClick = { onBottomAppBarEditButtonClicked() }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Icon")
            }
        },
        floatingActionButton = {
            DefaultFloatingActionButton(
                onFabClicked = { onAddToWhatsAppButtonClicked() },
                icon = Icons.Default.Whatsapp,
                iconContentDescription = "WhatsApp Icon"
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StickerCollectionDetailsTopAppBarInEditMode(
    collectionName: String,
    onCollectionNameChanged: (newCollectionName: String) -> Unit,
    onEditModeSaveButtonClicked: () -> Unit,
    onEditModeDeleteButtonClicked: () -> Unit,
    onEditModeCancelButtonClicked: () -> Unit,
) {
    Log.d(tag, "StickerCollectionDetailsTopAppBarInEditMode | collectionName: $collectionName")

    TopAppBar(
        title = {
            TextField(
                value = collectionName,
                onValueChange = { newCollectionName: String ->
                    onCollectionNameChanged(newCollectionName)
                },
                singleLine = true,
                keyboardActions = KeyboardActions(
                    onDone = {
                        onEditModeSaveButtonClicked()
                    }
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = { onEditModeCancelButtonClicked() }) {
                Icon(Icons.Default.Cancel, contentDescription = "Cancel Icon")
            }
        },
        actions = {
            IconButton(onClick = { onEditModeSaveButtonClicked() }) {
                Icon(Icons.Default.Save, contentDescription = "Save Icon")
            }

            IconButton(onClick = { onEditModeDeleteButtonClicked() }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Icon")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StickerCollectionDetailsTopAppBarInDeleteStickerMode(
    onDeleteStickerModeSaveButtonClicked: () -> Unit,
    onDeleteStickerModeCancelButtonClicked: () -> Unit,
) {
    Log.d(tag, "StickerCollectionDetailsTopAppBarInDeleteStickerMode")

    TopAppBar(
        title = { Text(text = "Remove Sticker") },
        navigationIcon = {
            IconButton(onClick = { onDeleteStickerModeCancelButtonClicked() }) {
                Icon(Icons.Default.Cancel, contentDescription = "Cancel Icon")
            }
        },
        actions = {
            IconButton(onClick = { onDeleteStickerModeSaveButtonClicked() }) {
                Icon(Icons.Default.Save, contentDescription = "Save Icon")
            }
        }
    )
}

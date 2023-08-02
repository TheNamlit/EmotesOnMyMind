package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.add_sticker_to_collection

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thenamlit.emotesonmymind.core.util.Logging


private const val tag = "${Logging.loggingPrefix}AddStickerToCollectionAlertDialogContent"

@Composable
fun AddStickerToCollectionAlertDialogContent(
    modifier: Modifier = Modifier,
    stickerCollectionSelectedStates: List<StickerCollectionSelectedState>,
    onStickerCollectionRowClicked: (StickerCollectionSelectedState) -> Unit,
    selectAllCollectionsButtonState: SelectAllCollectionsButtonState,
    addToAllCollections: () -> Unit,
    removeFromAllCollections: () -> Unit,
    onCreateCollectionClick: () -> Unit,
) {
    Log.d(tag, "AddStickerToCollectionAlertDialogContent")

    LazyColumn(
        modifier = modifier
    ) {

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(25.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = { onCreateCollectionClick() }) {
                    Text(text = "Create Collection")
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                horizontalArrangement = Arrangement.End
            ) {
                when (selectAllCollectionsButtonState) {
                    is SelectAllCollectionsButtonState.AddAll -> {
                        Button(onClick = { addToAllCollections() }) {
                            Text(text = selectAllCollectionsButtonState.buttonText)
                        }
                    }

                    is SelectAllCollectionsButtonState.RemoveAll -> {
                        Button(onClick = { removeFromAllCollections() }) {
                            Text(text = selectAllCollectionsButtonState.buttonText)
                        }
                    }

                    is SelectAllCollectionsButtonState.NoCollectionsAvailable -> {

                    }
                }
            }
        }

        items(items = stickerCollectionSelectedStates) { stickerCollectionSelectedState: StickerCollectionSelectedState ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        Log.d(
                            tag,
                            "Clicked ${stickerCollectionSelectedState.stickerCollection.name}"
                        )
                        onStickerCollectionRowClicked(stickerCollectionSelectedState)
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stickerCollectionSelectedState.stickerCollection.name,
                    style = MaterialTheme.typography.labelLarge
                )

                if (stickerCollectionSelectedState.currentlySelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Check Icon",
                    )
                }
            }

            Divider()
        }
    }
}

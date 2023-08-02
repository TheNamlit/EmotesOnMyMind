package com.thenamlit.emotesonmymind.core.presentation.components

import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isUnspecified
import com.thenamlit.emotesonmymind.core.util.Logging


private const val tag = "${Logging.loggingPrefix}AutoResizedText"

@Composable
fun AutoResizedText(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    titleText: String,
) {
    Log.d(tag, "AutoResizedText | modifier: $modifier, style: $style, titleText: $titleText")

    var resizedTextStyle by remember {
        mutableStateOf(style)
    }
    var shouldDraw by remember {
        mutableStateOf(false)
    }
    val defaultFontSize = style.fontSize
    // The following is used in the video but maybe it's better to use the style from function parameters instead?
//    val defaultFontSize = MaterialTheme.typography.bodyLarge.fontSize


// Auto Resize Text -> https://www.youtube.com/watch?v=ntlyrFw0F9U
    Text(
        modifier = modifier.drawWithContent {
            if (shouldDraw) {
                drawContent()
            }
        },
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        softWrap = false,
        text = titleText,
        style = resizedTextStyle,
        onTextLayout = { result ->
            if (result.didOverflowWidth) {
                if (style.fontSize.isUnspecified) {
                    resizedTextStyle = resizedTextStyle.copy(
                        fontSize = defaultFontSize
                    )
                }

                resizedTextStyle =
                    resizedTextStyle.copy(fontSize = resizedTextStyle.fontSize * 0.95)
            } else {
                shouldDraw = true
            }
        }
    )
}

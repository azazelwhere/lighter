package com.lighter.browser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.lighter.browser.ui.theme.*

/**
 * AOSP Holo-style URL bar: dark with cyan underline, monospace-ish text,
 * lock icon shown when HTTPS, red warning triangle when SSL error.
 */
@Composable
fun UrlBar(
    url: String,
    loading: Boolean,
    sslError: Boolean,
    incognito: Boolean,
    onUrlSubmit: (String) -> Unit,
    onReload: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textState by remember(url) {
        mutableStateOf(TextFieldValue(url))
    }
    var isEditing by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(if (incognito) IncognitoPurple else HoloActionBar)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Lock / SSL indicator
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = if (incognito) Color.White else HoloBlueBright
            )
        } else if (sslError) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "SSL error",
                tint = HoloRed,
                modifier = Modifier.size(20.dp)
            )
        } else if (url.startsWith("https://")) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Secure",
                tint = if (incognito) Color.White else HoloBlueBright,
                modifier = Modifier.size(18.dp)
            )
        } else if (url.startsWith("http://")) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Insecure",
                tint = HoloOrange,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        OutlinedTextField(
            value = textState,
            onValueChange = { textState = it; isEditing = true },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    "Cari atau ketik URL",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HoloTextHint
                )
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = if (incognito) Color.White else HoloTextPrimary
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                cursorColor = if (incognito) Color.White else HoloBlueBright,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(
                onGo = {
                    onUrlSubmit(textState.text)
                    isEditing = false
                    keyboard?.hide()
                }
            ),
            trailingIcon = {
                if (isEditing && textState.text.isNotEmpty()) {
                    IconButton(onClick = { textState = TextFieldValue("") }) {
                        Icon(Icons.Filled.Clear, "Clear", tint = HoloTextSecondary)
                    }
                } else if (loading) {
                    IconButton(onClick = onStop) {
                        Icon(Icons.Filled.Close, "Stop", tint = HoloRed)
                    }
                } else {
                    IconButton(onClick = onReload) {
                        Icon(Icons.Filled.Refresh, "Reload", tint = HoloBlueBright)
                    }
                }
            }
        )

        Spacer(Modifier.width(4.dp))
    }

    // Cyan divider under URL bar
    Box(
        Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(if (incognito) IncognitoPurpleLight else HoloBlue)
    )
}

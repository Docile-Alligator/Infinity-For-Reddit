package ml.docilealligator.infinityforreddit.customviews.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString

@Composable
fun CustomAlert(
    modifier: Modifier = Modifier,
    @DrawableRes iconDrawableRes: Int? = null,
    title: String,
    text: String,
    confirmText: String,
    dismissText: String? = null,
    cancelable: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
) {
    AlertDialog(
        modifier = modifier,
        icon = iconDrawableRes?.let {
            @Composable {
                Icon(painterResource(it), contentDescription = "Example Icon")
            }
        },
        title = {
            PrimaryText(text = title)
        },
        text = {
            Column(
                modifier = modifier.verticalScroll(rememberScrollState())
            ) {
                SecondaryText(text = text)
            }
        },
        onDismissRequest = {
            if (cancelable) {
                onDismiss?.invoke()
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                }
            ) {
                Text(
                    confirmText,
                    color = Color(LocalAppTheme.current.colorAccent)
                )
            }
        },
        dismissButton = {
            dismissText?.let {
                TextButton(
                    onClick = {
                        onDismiss?.invoke()
                    }
                ) {
                    PrimaryText(dismissText)
                }
            }
        }
    )
}

@Composable
fun CustomAlert(
    modifier: Modifier = Modifier,
    @DrawableRes iconDrawableRes: Int? = null,
    title: String,
    text: AnnotatedString,
    confirmText: String,
    dismissText: String? = null,
    cancelable: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
) {
    AlertDialog(
        modifier = modifier,
        icon = iconDrawableRes?.let {
            @Composable {
                Icon(painterResource(it), contentDescription = "Example Icon")
            }
        },
        title = {
            PrimaryText(text = title)
        },
        text = {
            Column(
                modifier = modifier.verticalScroll(rememberScrollState())
            ) {
                SecondaryText(text = text)
            }
        },
        onDismissRequest = {
            if (cancelable) {
                onDismiss?.invoke()
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                }
            ) {
                Text(
                    confirmText,
                    color = Color(LocalAppTheme.current.colorAccent)
                )
            }
        },
        dismissButton = {
            dismissText?.let {
                TextButton(
                    onClick = {
                        onDismiss?.invoke()
                    }
                ) {
                    PrimaryText(dismissText)
                }
            }
        }
    )
}
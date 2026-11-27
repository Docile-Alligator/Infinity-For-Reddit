package ml.docilealligator.infinityforreddit.extensions

import android.net.Uri
import android.text.SpannableString
import android.text.style.URLSpan
import android.text.util.Linkify
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.core.net.toUri
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun String.linkify(mask: Int, linkStyle: SpanStyle, onUrlClick: (Uri) -> Unit) = buildAnnotatedString {
    append(this@linkify)

    val spannable = SpannableString(this@linkify)
    Linkify.addLinks(spannable, mask)

    val spans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
    for (span in spans) {
        addLink(
            clickable = LinkAnnotation.Clickable(
                tag = "URL",
                styles = TextLinkStyles(linkStyle),
                linkInteractionListener = {
                    onUrlClick(span.url.toUri())
                }
            ),
            start = spannable.getSpanStart(span),
            end = spannable.getSpanEnd(span)
        )
    }
}

fun String.getFileNameFromUrlString(): String? {
    return toUri().lastPathSegment
}

fun String.getExtensionFromFileName(): String? {
    val extension = substringAfterLast(".", "")
    if (extension.isEmpty()) {
        return null
    }

    return extension.lowercase()
}

fun String.getLowercaseExtensionForFileName(extension: String): String {
    val index = lastIndexOf(".")
    return if (index >= 0) {
        "${this.substring(0, index)}.$extension"
    } else {
        "$this.$extension"
    }
}

fun String.toEpochMilliForModMail(): Long {
    try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSxx")
        return OffsetDateTime.parse(this, formatter).toInstant().toEpochMilli()
    } catch (_: Exception) {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSxxx")
            return OffsetDateTime.parse(this, formatter).toInstant().toEpochMilli()
        } catch (_: Exception) {
            return 0
        }
    }
}
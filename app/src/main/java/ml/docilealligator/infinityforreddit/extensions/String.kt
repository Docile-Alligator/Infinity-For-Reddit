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
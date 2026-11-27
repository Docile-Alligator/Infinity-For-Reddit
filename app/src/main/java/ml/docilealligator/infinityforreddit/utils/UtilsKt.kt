@file:JvmName("UtilsKt")
package ml.docilealligator.infinityforreddit.utils

import android.content.Context
import androidx.browser.customtabs.CustomTabsClient
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun getChromeCustomTabPackageName(context: Context): String? {
    return CustomTabsClient.getPackageName(
        context, listOf("com.android.chrome", "com.chrome.beta", "com.chrome.dev", "com.chrome.canary", "org.mozilla.firefox", "org.mozilla.focus"), true
    )
}

fun getRandomString(length: Int = 6) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun getFormattedTime(millis: Long, pattern: String): String {
    val formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(millis))
}

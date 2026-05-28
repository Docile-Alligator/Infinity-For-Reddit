@file:JvmName("UtilsKt")
package ml.docilealligator.infinityforreddit.utils

import android.content.Context
import androidx.browser.customtabs.CustomTabsClient

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

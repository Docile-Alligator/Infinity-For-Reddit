@file:JvmName("UtilsKt")
package ml.docilealligator.infinityforreddit.utils

import android.content.Context
import androidx.browser.customtabs.CustomTabsClient

fun getChromeCustomTabPackageName(context: Context): String? {
    return CustomTabsClient.getPackageName(
        context, listOf("com.android.chrome", "com.chrome.beta", "com.chrome.dev", "com.chrome.canary"), true
    )
}
package ml.docilealligator.infinityforreddit.network

import okhttp3.Interceptor
import okhttp3.Response

class RefreshCookieInterceptor(
    private val appCookieJar: AppCookieJar
) : Interceptor {
    override fun intercept(
        chain: Interceptor.Chain
    ): Response {
        val request = chain.request()

        appCookieJar.getCookies()?.let {
            if (it.isEmpty()) {
                appCookieJar.refreshCookies()
            }
        } ?: run {
            appCookieJar.refreshCookies()
        }

        return chain.proceed(request)
    }
}
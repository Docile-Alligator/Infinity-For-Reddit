package ml.docilealligator.infinityforreddit.network

import android.content.SharedPreferences
import androidx.core.content.edit
import ml.docilealligator.infinityforreddit.utils.APIUtils
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class AppCookieJar(
    private val cookiesSharedPreferences: SharedPreferences,
    private val okHttpClient: OkHttpClient
) : CookieJar {
    private val cookieLock = Any()

    override fun saveFromResponse(
        url: HttpUrl,
        cookies: List<Cookie>
    ) {
        saveCookies(cookies)
    }

    override fun loadForRequest(
        url: HttpUrl
    ): List<Cookie> {
        return getCookies() ?: emptyList()
    }

    fun refreshCookies() {
        getCookies()?.let {
            if (!it.isEmpty()) {
                return
            }
        }

        synchronized(cookieLock) {
            getCookies()?.let {
                if (!it.isEmpty()) {
                    return
                }
            }

            val refreshRequest = Request.Builder()
                .url("https://reddit.com/best")
                .header("User-Agent", APIUtils.ANONYMOUS_USER_AGENT)
                .get()
                .build()

            val response = okHttpClient
                .newBuilder()
                .followRedirects(false)
                .addInterceptor { chain ->
                    var request = chain.request()
                    var response = chain.proceed(request)

                    while (response.isRedirect) {
                        val cookies = Cookie.parseAll(
                            refreshRequest.url,
                            response.headers
                        )
                        saveCookies(cookies)

                        val location = response.header("Location") ?: break

                        response.close()

                        request.url.resolve(location)?.let { httpUrl ->
                            val cookiesForUrl: List<Cookie> =
                                loadForRequest(httpUrl)

                            val cookieHeaderBuilder = StringBuilder()
                            for (cookie in cookiesForUrl) {
                                cookieHeaderBuilder.append(cookie.name).append("=").append(cookie.value)
                                    .append("; ")
                            }

                            request = request.newBuilder()
                                .url(httpUrl)
                                .header("Referer", request.url.toString())
                                .header("Cookie", cookieHeaderBuilder.toString())
                                .build()

                            response = chain.proceed(request)
                        } ?: break
                    }

                    val cookies = Cookie.parseAll(
                        refreshRequest.url,
                        response.headers
                    )
                    saveCookies(cookies)

                    response
                }
                .build()
                .newCall(refreshRequest).execute()

            val cookies = Cookie.parseAll(
                refreshRequest.url,
                response.headers
            )

            saveCookies(cookies)
        }
    }

    fun saveCookies(cookies: List<Cookie>) {
        cookiesSharedPreferences.edit {
            cookies.forEach { cookie ->
                putString(cookie.name, cookie.toString())
                putLong("${cookie.name}_expires", cookie.expiresAt)
            }
        }
    }

    fun getCookies(): List<Cookie>? {
        val cookies = mutableListOf<Cookie>()
        for ((key, value) in cookiesSharedPreferences.all) {
            if (key.endsWith("_expires")) {
                continue
            }

            val cookieString = value as? String ?: continue
            if (isExpired(key)) {
                clearCookies()
                return null
            }

            Cookie.parse("https://www.reddit.com".toHttpUrl(), cookieString)?.let {
                cookies.add(it)
            }
        }

        return cookies
    }

    fun clearCookies() {
        cookiesSharedPreferences.edit { clear() }
    }

    fun isExpired(name: String): Boolean {
        val expiry = cookiesSharedPreferences.getLong("${name}_expires", 0L)
        return System.currentTimeMillis() > expiry
    }
}
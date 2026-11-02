package ml.docilealligator.infinityforreddit.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Finds an i.redd.it image filed under a different extension than the one asked for.
 *
 * i.redd.it serves a file only under its own extension: `<id>.png` answers 200 and `<id>.jpeg`
 * answers 404 for the same image. Normally the URL comes from Reddit and is right. A gallery item or
 * body image Reddit's listing reports as `"status": "failed"` comes with no URL and no MIME type at
 * all, so the parser builds one from the media id and can only guess the extension (see
 * `FailedRedditMedia`). When the guess is wrong, this tries the other extensions Reddit files
 * images under and hands back the first that exists, so the feed, the post body, the full-screen
 * viewer and a download all get the picture without each of them knowing about guessed URLs.
 *
 * Only a 404 from i.redd.it for a bare `/<id>.<extension>` path is retried; every other response
 * passes through untouched. A deleted image costs the retries and still ends as a 404.
 */
object RedditMediaExtensionInterceptor : Interceptor {

    /** Every extension Reddit files a gallery image under on i.redd.it, most common first. */
    @JvmField
    val EXTENSIONS = listOf("jpeg", "png", "jpg", "gif", "webp")

    private const val HOST = "i.redd.it"
    private val MEDIA_PATH = Regex("/([A-Za-z0-9]+)\\.([A-Za-z]+)")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code != 404 || request.method != "GET" || request.url.host != HOST) {
            return response
        }
        val match = MEDIA_PATH.matchEntire(request.url.encodedPath) ?: return response
        val (mediaId, requested) = match.destructured
        if (requested.lowercase() !in EXTENSIONS) {
            return response
        }

        // OkHttp refuses another request on this chain while a response body is still open.
        response.close()
        for (extension in EXTENSIONS) {
            if (extension == requested.lowercase()) {
                continue
            }
            val url = request.url.newBuilder().encodedPath("/$mediaId.$extension").build()
            val retry = chain.proceed(request.newBuilder().url(url).build())
            if (retry.isSuccessful) {
                return retry
            }
            retry.close()
        }
        // Nothing exists under any extension: report the original 404 for the URL that was asked
        // for, with an empty body in place of the one already closed.
        return response.newBuilder().body("".toResponseBody(null)).build()
    }

    /**
     * The extension this served [requestedUrl] under instead of its own, or null when the file came
     * back as asked or is not an i.redd.it image. A download names its file before the request, from
     * the guessed extension, and uses this to name it for the bytes that actually arrived.
     */
    @JvmStatic
    fun substitutedExtension(requestedUrl: String?, servedUrl: HttpUrl): String? {
        val requested = requestedUrl?.toHttpUrlOrNull() ?: return null
        if (requested.host != HOST || servedUrl.host != HOST) {
            return null
        }
        val asked = MEDIA_PATH.matchEntire(requested.encodedPath) ?: return null
        val served = MEDIA_PATH.matchEntire(servedUrl.encodedPath) ?: return null
        if (asked.groupValues[1] != served.groupValues[1]) {
            return null
        }
        val extension = served.groupValues[2].lowercase()
        return if (extension == asked.groupValues[2].lowercase()) null else extension
    }
}

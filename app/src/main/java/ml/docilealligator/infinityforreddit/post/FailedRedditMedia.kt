package ml.docilealligator.infinityforreddit.post

import android.net.Uri
import ml.docilealligator.infinityforreddit.network.RedditMediaExtensionInterceptor
import ml.docilealligator.infinityforreddit.thing.MediaMetadata
import ml.docilealligator.infinityforreddit.utils.JSONUtils
import org.json.JSONArray
import org.json.JSONObject

/**
 * Reddit-hosted images the listing JSON reports as failed when they are actually fine.
 *
 * Images uploaded through the API's media lease with a `filepath` (`/api/media/asset.json`, as
 * Infinity, PRAW and Apollo still do and Continuum did until `UploadImageUtils.imageLeaseParams`)
 * come back in `media_metadata` as just `{"status": "failed"}` -- no `m`, no `s`, no previews -- and
 * a body that embeds one says `*Processing img <id>...*` where the image should be. Reddit's own
 * clients read the same posts through GraphQL, which reports those items `VALID`, and the files are
 * served from i.redd.it as usual. So such a gallery showed on reddit.com but was demoted to a text
 * post here, and an image put in a post or comment body stayed a placeholder. The same shape, with
 * the files present, goes back to at least January 2026 (r/Leathercraft 1qo3jrv).
 *
 * What the listing still gives is the media id, and i.redd.it names files `<id>.<extension>`. The
 * extension is the one thing missing. The post's `thumbnail`, when Reddit generated one, is a
 * preview of one of the post's images and names it with its extension, and a post's images are
 * usually all one format, so that extension is the guess for every failed item; without a thumbnail
 * it is JPEG, which Reddit files as `.jpeg`. A wrong guess is corrected at load time by
 * [RedditMediaExtensionInterceptor].
 */
object FailedRedditMedia {

    private const val STATUS_FAILED = "failed"

    /** What Reddit files a JPEG upload under now; it is also the most common upload format. */
    private const val DEFAULT_EXTENSION = "jpeg"

    /** Whether a `media_metadata` entry is the listing's misreported failure rather than real data. */
    @JvmStatic
    fun isReportedFailed(entry: JSONObject): Boolean =
        STATUS_FAILED == entry.optString(JSONUtils.STATUS_KEY) && !entry.has(JSONUtils.S_KEY)

    /**
     * The media id the post's thumbnail is a preview of, when it is one of this gallery's items, or
     * null. A crosspost can carry its parent's thumbnail, and an old post a `b.thumbs` one, so the
     * name has to match an item before anything is read from it.
     */
    @JvmStatic
    fun thumbnailMediaId(thumbnailUrl: String?, galleryItems: JSONArray): String? {
        val file = thumbnailFile(thumbnailUrl) ?: return null
        for (i in 0 until galleryItems.length()) {
            if (file.first == galleryItems.optJSONObject(i)?.optString(JSONUtils.MEDIA_ID_KEY)) {
                return file.first
            }
        }
        return null
    }

    /** The same for a post's `media_metadata`: the thumbnail must name one of its keys. */
    @JvmStatic
    fun thumbnailMediaId(thumbnailUrl: String?, mediaMetadata: JSONObject): String? {
        val file = thumbnailFile(thumbnailUrl) ?: return null
        return if (mediaMetadata.has(file.first)) file.first else null
    }

    /** The extension to try for every failed item of a post whose thumbnail names [thumbnailMediaId]. */
    @JvmStatic
    fun extension(thumbnailUrl: String?, thumbnailMediaId: String?): String {
        if (thumbnailMediaId == null) {
            return DEFAULT_EXTENSION
        }
        return thumbnailFile(thumbnailUrl)?.second ?: DEFAULT_EXTENSION
    }

    /**
     * Whether the thumbnail shows its whole image, so its size is that image's aspect ratio. A text
     * post's thumbnail is a `crop=1:1` square of the image, which says nothing about its shape.
     */
    @JvmStatic
    fun thumbnailIsUncropped(thumbnailUrl: String?): Boolean =
        !thumbnailUrl.isNullOrEmpty() && Uri.parse(thumbnailUrl).getQueryParameter("crop") == null

    @JvmStatic
    fun sourceUrl(mediaId: String, extension: String): String = "https://i.redd.it/$mediaId.$extension"

    /**
     * What `JSONUtils.parseMediaMetadata` keeps for a body image the listing reports as failed: an
     * image at its i.redd.it URL, of unknown size, which the markdown block sizes from the loaded
     * picture.
     */
    @JvmStatic
    fun mediaMetadata(mediaId: String, extension: String): MediaMetadata {
        val item = MediaMetadata.MediaItem(0, 0, sourceUrl(mediaId, extension))
        return MediaMetadata(mediaId, "Image", item, item)
    }

    /** `<id>` and `<extension>` from a `preview.redd.it/<id>.<extension>?...` thumbnail, or null. */
    private fun thumbnailFile(thumbnailUrl: String?): Pair<String, String>? {
        if (thumbnailUrl.isNullOrEmpty()) {
            return null
        }
        val name = Uri.parse(thumbnailUrl).lastPathSegment ?: return null
        val dot = name.lastIndexOf('.')
        if (dot <= 0) {
            return null
        }
        val extension = name.substring(dot + 1).lowercase()
        if (extension !in RedditMediaExtensionInterceptor.EXTENSIONS) {
            return null
        }
        return name.substring(0, dot) to extension
    }
}

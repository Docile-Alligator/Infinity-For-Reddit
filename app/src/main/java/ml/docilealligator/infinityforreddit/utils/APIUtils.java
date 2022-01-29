package ml.docilealligator.infinityforreddit.utils;

import android.util.Base64;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by alex on 2/23/18.
 */

public class APIUtils {
    public static final String OAUTH_URL = "https://www.reddit.com/api/v1/authorize.compact";
    public static final String OAUTH_API_BASE_URI = "https://oauth.reddit.com";
    public static final String API_BASE_URI = "https://www.reddit.com";
    public static final String API_UPLOAD_MEDIA_URI = "https://reddit-uploaded-media.s3-accelerate.amazonaws.com";
    public static final String API_UPLOAD_VIDEO_URI = "https://reddit-uploaded-video.s3-accelerate.amazonaws.com";
    public static final String GFYCAT_API_BASE_URI = "https://api.gfycat.com/v1/gfycats/";
    public static final String REDGIFS_API_BASE_URI = "https://api.redgifs.com/v1/gfycats/";
    public static final String IMGUR_API_BASE_URI = "https://api.imgur.com/3/";
    public static final String PUSHSHIFT_API_BASE_URI = "https://api.pushshift.io/";
    public static final String REVEDDIT_API_BASE_URI = "https://api.reveddit.com/";
    public static final String STRAPI_BASE_URI = "https://strapi.reddit.com";
    public static final String STREAMABLE_API_BASE_URI = "https://api.streamable.com";

    public static final String CLIENT_ID_KEY = "client_id";
    public static final String CLIENT_ID = "NOe2iKrPPzwscA";
    public static final String IMGUR_CLIENT_ID = "Client-ID cc671794e0ab397";
    public static final String RESPONSE_TYPE_KEY = "response_type";
    public static final String RESPONSE_TYPE = "code";
    public static final String STATE_KEY = "state";
    public static final String STATE = "23ro8xlxvzp4asqd";
    public static final String REDIRECT_URI_KEY = "redirect_uri";
    public static final String REDIRECT_URI = "infinity://localhost";
    public static final String DURATION_KEY = "duration";
    public static final String DURATION = "permanent";
    public static final String SCOPE_KEY = "scope";
    public static final String SCOPE = "identity edit flair history modconfig modflair modlog modposts modwiki mysubreddits privatemessages read report save submit subscribe vote wikiedit wikiread creddits modcontributors modmail modothers livemanage account modself";
    public static final String ACCESS_TOKEN_KEY = "access_token";

    public static final String AUTHORIZATION_KEY = "Authorization";
    public static final String AUTHORIZATION_BASE = "bearer ";
    public static final String USER_AGENT_KEY = "User-Agent";
    public static final String USER_AGENT = "android:ml.docilealligator.infinityforreddit:v5.1.7 (by /u/Hostilenemy)";

    public static final String GRANT_TYPE_KEY = "grant_type";
    public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    public static final String REFRESH_TOKEN_KEY = "refresh_token";

    public static final String DIR_KEY = "dir";
    public static final String ID_KEY = "id";
    public static final String RANK_KEY = "rank";
    public static final String DIR_UPVOTE = "1";
    public static final String DIR_UNVOTE = "0";
    public static final String DIR_DOWNVOTE = "-1";
    public static final String RANK = "10";

    public static final String ACTION_KEY = "action";
    public static final String SR_NAME_KEY = "sr_name";

    public static final String API_TYPE_KEY = "api_type";
    public static final String API_TYPE_JSON = "json";
    public static final String RETURN_RTJSON_KEY = "return_rtjson";
    public static final String TEXT_KEY = "text";
    public static final String URL_KEY = "url";
    public static final String VIDEO_POSTER_URL_KEY = "video_poster_url";
    public static final String THING_ID_KEY = "thing_id";

    public static final String SR_KEY = "sr";
    public static final String TITLE_KEY = "title";
    public static final String FLAIR_TEXT_KEY = "flair_text";
    public static final String SPOILER_KEY = "spoiler";
    public static final String NSFW_KEY = "nsfw";
    public static final String CROSSPOST_FULLNAME_KEY = "crosspost_fullname";
    public static final String SEND_REPLIES_KEY = "sendreplies";
    public static final String KIND_KEY = "kind";
    public static final String KIND_SELF = "self";
    public static final String KIND_LINK = "link";
    public static final String KIND_IMAGE = "image";
    public static final String KIND_VIDEO = "video";
    public static final String KIND_VIDEOGIF = "videogif";
    public static final String KIND_CROSSPOST = "crosspost";

    public static final String FILEPATH_KEY = "filepath";
    public static final String MIMETYPE_KEY = "mimetype";

    public static final String LINK_KEY = "link";
    public static final String FLAIR_TEMPLATE_ID_KEY = "flair_template_id";
    public static final String FLAIR_ID_KEY = "flair_id";

    public static final String MAKE_FAVORITE_KEY = "make_favorite";

    public static final String MULTIPATH_KEY = "multipath";
    public static final String MODEL_KEY = "model";

    public static final String REASON_KEY = "reason";

    public static final String SUBJECT_KEY = "subject";
    public static final String TO_KEY = "to";

    public static final String NAME_KEY = "name";

    public static final String GILD_TYPE = "gild_type";
    public static final String IS_ANONYMOUS = "is_anonymous";

    public static final String ORIGIN_KEY = "Origin";
    public static final String REVEDDIT_ORIGIN = "https://www.reveddit.com";
    public static final String REFERER_KEY = "Referer";
    public static final String REVEDDIT_REFERER = "https://www.reveddit.com/";

    public static Map<String, String> getHttpBasicAuthHeader() {
        Map<String, String> params = new HashMap<>();
        String credentials = String.format("%s:%s", APIUtils.CLIENT_ID, "");
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        params.put(APIUtils.AUTHORIZATION_KEY, auth);
        return params;
    }

    public static Map<String, String> getOAuthHeader(String accessToken) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.AUTHORIZATION_KEY, APIUtils.AUTHORIZATION_BASE + accessToken);
        params.put(APIUtils.USER_AGENT_KEY, APIUtils.USER_AGENT);
        return params;
    }

    public static RequestBody getRequestBody(String s) {
        return RequestBody.create(s, MediaType.parse("text/plain"));
    }

    public static Map<String, String> getRevedditHeader() {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ORIGIN_KEY, APIUtils.REVEDDIT_ORIGIN);
        params.put(APIUtils.REFERER_KEY, APIUtils.REVEDDIT_REFERER);
        params.put(APIUtils.USER_AGENT_KEY, APIUtils.USER_AGENT);
        return params;
    }
}

package ml.docilealligator.infinityforreddit;

import android.util.Base64;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by alex on 2/23/18.
 */

public class RedditUtils {
    static final String OAUTH_URL ="https://www.reddit.com/api/v1/authorize.compact";
    static final String OAUTH_API_BASE_URI = "https://oauth.reddit.com";
    static final String API_BASE_URI = "https://www.reddit.com";
    static final String API_UPLOAD_MEDIA_URI = "https://reddit-uploaded-media.s3-accelerate.amazonaws.com";
    static final String API_UPLOAD_VIDEO_URI = "https://reddit-uploaded-video.s3-accelerate.amazonaws.com";

    static final String CLIENT_ID_KEY = "client_id";
    static final String CLIENT_ID = "";
    static final String RESPONSE_TYPE_KEY = "response_type";
    static final String RESPONSE_TYPE = "code";
    static final String STATE_KEY = "state";
    static final String STATE = "";
    static final String REDIRECT_URI_KEY = "redirect_uri";
    static final String REDIRECT_URI = "";
    static final String DURATION_KEY = "duration";
    static final String DURATION = "permanent";
    static final String SCOPE_KEY = "scope";
    static final String SCOPE = "identity edit flair history modconfig modflair modlog modposts modwiki mysubreddits privatemessages read report save submit subscribe vote wikiedit wikiread";
    static final String ACCESS_TOKEN_KEY = "access_token";

    static final String AUTHORIZATION_KEY = "Authorization";
    static final String AUTHORIZATION_BASE = "bearer ";
    static final String USER_AGENT_KEY = "User-Agent";
    static final String USER_AGENT = "";

    static final String GRANT_TYPE_KEY = "grant_type";
    static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    static final String REFRESH_TOKEN_KEY = "refresh_token";

    static final String DIR_KEY = "dir";
    static final String ID_KEY = "id";
    static final String RANK_KEY = "rank";
    static final String DIR_UPVOTE = "1";
    static final String DIR_UNVOTE = "0";
    static final String DIR_DOWNVOTE = "-1";
    static final String RANK = "10";

    static final String ACTION_KEY = "action";
    static final String SR_NAME_KEY = "sr_name";

    static final String API_TYPE_KEY = "api_type";
    static final String API_TYPE_JSON = "json";
    static final String RETURN_RTJSON_KEY = "return_rtjson";
    static final String TEXT_KEY = "text";
    static final String URL_KEY = "url";
    static final String VIDEO_POSTER_URL_KEY = "video_poster_url";
    static final String THING_ID_KEY = "thing_id";

    static final String SR_KEY = "sr";
    static final String TITLE_KEY = "title";
    static final String FLAIR_TEXT_KEY = "flair_text";
    static final String SPOILER_KEY = "spoiler";
    static final String NSFW_KEY = "nsfw";
    static final String KIND_KEY = "kind";
    static final String KIND_SELF = "self";
    static final String KIND_LINK = "link";
    static final String KIND_IMAGE = "image";
    static final String KIND_VIDEO = "video";
    static final String KIND_VIDEOGIF = "videogif";

    static final String FILEPATH_KEY = "filepath";
    static final String MIMETYPE_KEY = "mimetype";

    static final String LINK_KEY = "link";
    static final String FLAIR_TEMPLATE_ID_KEY = "flair_template_id";

    static Map<String, String> getHttpBasicAuthHeader() {
        Map<String, String> params = new HashMap<>();
        String credentials = String.format("%s:%s", RedditUtils.CLIENT_ID, "");
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        params.put(RedditUtils.AUTHORIZATION_KEY, auth);
        return params;
    }

    public static Map<String, String> getOAuthHeader(String accessToken) {
        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.AUTHORIZATION_KEY, RedditUtils.AUTHORIZATION_BASE + accessToken);
        params.put(RedditUtils.USER_AGENT_KEY, RedditUtils.USER_AGENT);
        return params;
    }

    static RequestBody getRequestBody(String s) {
        return RequestBody.create(MediaType.parse("text/plain"), s);
    }
}

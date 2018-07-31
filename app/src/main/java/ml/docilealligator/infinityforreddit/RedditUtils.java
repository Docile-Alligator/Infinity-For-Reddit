package ml.docilealligator.infinityforreddit;

import android.util.Base64;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alex on 2/23/18.
 */

class RedditUtils {
    static final String OAUTH_URL ="https://www.reddit.com/api/v1/authorize.compact";
    static final String ACQUIRE_ACCESS_TOKEN_URL = "https://www.reddit.com/api/v1/access_token";
    static final String OAUTH_API_BASE_URI = "https://oauth.reddit.com";
    static final String API_BASE_URI = "https://www.reddit.com";
    static final String RAW_JSON_KEY ="raw_json";
    static final String RAW_JSON_VALUE = "1";
    static final String BEST_POST_SUFFIX = "/best";
    static final String VOTE_SUFFIX = "/api/vote";
    static final String USER_INFO_SUFFIX = "/api/v1/me";
    static final String SUBSCRIBED_SUBREDDITS = "/subreddits/mine/subscriber";

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

    static final String AFTER_KEY = "after";

    static Map<String, String> getHttpBasicAuthHeader() {
        Map<String, String> params = new HashMap<>();
        String credentials = String.format("%s:%s", RedditUtils.CLIENT_ID, "");
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        params.put(RedditUtils.AUTHORIZATION_KEY, auth);
        return params;
    }

    static Map<String, String> getOAuthHeader(String accessToken) {
        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.AUTHORIZATION_KEY, RedditUtils.AUTHORIZATION_BASE + accessToken);
        params.put(RedditUtils.USER_AGENT_KEY, RedditUtils.USER_AGENT);
        return params;
    }

    static String getQueryCommentUri(String subredditName, String article) {
        return API_BASE_URI + "/" + subredditName + "/comments/" + article + ".json";
    }

    static String getQuerySubredditDataUri(String subredditName) {
        return API_BASE_URI + "/r/" + subredditName + "/about.json";
    }
}

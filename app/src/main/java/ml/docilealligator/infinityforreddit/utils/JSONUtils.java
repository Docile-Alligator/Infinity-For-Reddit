package ml.docilealligator.infinityforreddit.utils;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ml.docilealligator.infinityforreddit.thing.MediaMetadata;

/**
 * Created by alex on 2/25/18.
 */

public class JSONUtils {
    public static final String KIND_KEY = "kind";
    public static final String KIND_VALUE_MORE = "more";
    public static final String DATA_KEY = "data";
    public static final String AFTER_KEY = "after";
    public static final String CHILDREN_KEY = "children";
    public static final String COUNT_KEY = "count";
    public static final String TITLE_KEY = "title";
    public static final String NAME_KEY = "name";
    public static final String SUBREDDIT_NAME_PREFIX_KEY = "subreddit_name_prefixed";
    public static final String SELFTEXT_KEY = "selftext";
    public static final String SELFTEXT_HTML_KEY = "selftext_html";
    public static final String AUTHOR_KEY = "author";
    public static final String AUTHOR_FLAIR_RICHTEXT_KEY = "author_flair_richtext";
    public static final String AUTHOR_FLAIR_TEXT_KEY = "author_flair_text";
    public static final String E_KEY = "e";
    public static final String T_KEY = "t";
    public static final String U_KEY = "u";
    public static final String LINK_KEY = "link";
    public static final String LINK_AUTHOR_KEY = "link_author";
    public static final String LINK_FLAIR_TEXT_KEY = "link_flair_text";
    public static final String LINK_FLAIR_RICHTEXT_KEY = "link_flair_richtext";
    public static final String SCORE_KEY = "score";
    public static final String LIKES_KEY = "likes";
    public static final String NSFW_KEY = "over_18";
    public static final String PERMALINK_KEY = "permalink";
    public static final String CREATED_UTC_KEY = "created_utc";
    public static final String PREVIEW_KEY = "preview";
    public static final String IMAGES_KEY = "images";
    public static final String WIDTH_KEY = "width";
    public static final String HEIGHT_KEY = "height";
    public static final String SOURCE_KEY = "source";
    public static final String URL_KEY = "url";
    public static final String MEDIA_KEY = "media";
    public static final String REDDIT_VIDEO_KEY = "reddit_video";
    public static final String HLS_URL_KEY = "hls_url";
    public static final String FALLBACK_URL_KEY = "fallback_url";
    public static final String IS_VIDEO_KEY = "is_video";
    public static final String CROSSPOST_PARENT_LIST = "crosspost_parent_list";
    public static final String REDDIT_VIDEO_PREVIEW_KEY = "reddit_video_preview";
    public static final String STICKIED_KEY = "stickied";
    public static final String BODY_KEY = "body";
    public static final String BODY_HTML_KEY = "body_html";
    public static final String COLLAPSED_KEY = "collapsed";
    public static final String IS_SUBMITTER_KEY = "is_submitter";
    public static final String REPLIES_KEY = "replies";
    public static final String DEPTH_KEY = "depth";
    public static final String ID_KEY = "id";
    public static final String SCORE_HIDDEN_KEY = "score_hidden";
    public static final String SUBREDDIT_KEY = "subreddit";
    public static final String BANNER_IMG_KEY = "banner_img";
    public static final String BANNER_BACKGROUND_IMAGE_KEY = "banner_background_image";
    public static final String ICON_IMG_KEY = "icon_img";
    public static final String ICON_URL_KEY = "icon_url";
    public static final String COMMUNITY_ICON_KEY = "community_icon";
    public static final String LINK_KARMA_KEY = "link_karma";
    public static final String COMMENT_KARMA_KEY = "comment_karma";
    public static final String DISPLAY_NAME_KEY = "display_name";
    public static final String SUBREDDIT_TYPE_KEY = "subreddit_type";
    public static final String SUBREDDIT_TYPE_VALUE_USER = "user";
    public static final String SUBSCRIBERS_KEY = "subscribers";
    public static final String PUBLIC_DESCRIPTION_KEY = "public_description";
    public static final String ACTIVE_USER_COUNT_KEY = "active_user_count";
    public static final String IS_GOLD_KEY = "is_gold";
    public static final String IS_FRIEND_KEY = "is_friend";
    public static final String JSON_KEY = "json";
    public static final String PARENT_ID_KEY = "parent_id";
    public static final String LINK_ID_KEY = "link_id";
    public static final String LINK_TITLE_KEY = "link_title";
    public static final String ERRORS_KEY = "errors";
    public static final String ARGS_KEY = "args";
    public static final String FIELDS_KEY = "fields";
    public static final String VALUE_KEY = "value";
    public static final String TEXT_KEY = "text";
    public static final String SPOILER_KEY = "spoiler";
    public static final String RULES_KEY = "rules";
    public static final String SHORT_NAME_KEY = "short_name";
    public static final String DESCRIPTION_KEY = "description";
    public static final String DESCRIPTION_HTML_KEY = "description_html";
    public static final String DESCRIPTION_MD_KEY = "description_md";
    public static final String ARCHIVED_KEY = "archived";
    public static final String LOCKED_KEY = "locked";
    public static final String SAVED_KEY = "saved";
    public static final String REMOVED_KEY = "removed";
    public static final String REMOVED_BY_CATEGORY_KEY = "removed_by_category";
    public static final String TEXT_EDITABLE_KEY = "text_editable";
    public static final String SUBJECT_KEY = "subject";
    public static final String CONTEXT_KEY = "context";
    public static final String EDITED_KEY = "edited";
    public static final String DISTINGUISHED_KEY = "distinguished";
    public static final String WAS_COMMENT_KEY = "was_comment";
    public static final String NEW_KEY = "new";
    public static final String NUM_COMMENTS_KEY = "num_comments";
    public static final String HIDDEN_KEY = "hidden";
    public static final String USER_HAS_FAVORITED_KEY = "user_has_favorited";
    public static final String RESOLUTIONS_KEY = "resolutions";
    public static final String NUM_SUBSCRIBERS_KEY = "num_subscribers";
    public static final String COPIED_FROM_KEY = "copied_from";
    public static final String VISIBILITY_KEY = "visibility";
    public static final String OVER_18_KEY = "over_18";
    public static final String OWNER_KEY = "owner";
    public static final String IS_SUBSCRIBER_KEY = "is_subscriber";
    public static final String IS_FAVORITED_KEY = "is_favorited";
    public static final String SUBREDDITS_KEY = "subreddits";
    public static final String PATH_KEY = "path";
    public static final String RESIZED_ICONS_KEY = "resized_icons";
    public static final String GFY_ITEM_KEY = "gfyItem";
    public static final String MP4_URL_KEY = "mp4Url";
    public static final String TYPE_KEY = "type";
    public static final String MP4_KEY = "mp4";
    public static final String THINGS_KEY = "things";
    public static final String MEDIA_METADATA_KEY = "media_metadata";
    public static final String GALLERY_DATA_KEY = "gallery_data";
    public static final String ITEMS_KEY = "items";
    public static final String M_KEY = "m";
    public static final String MEDIA_ID_KEY = "media_id";
    public static final String S_KEY = "s";
    public static final String X_KEY = "x";
    public static final String Y_KEY = "y";
    public static final String DEST_KEY = "dest";
    public static final String GIF_KEY = "gif";
    public static final String MAX_EMOJIS_KEY = "max_emojis";
    public static final String RICHTEXT_KEY = "richtext";
    public static final String SUGGESTED_COMMENT_SORT_KEY = "suggested_comment_sort";
    public static final String OVER18_KEY = "over18";
    public static final String TOTAL_KARMA_KEY = "total_karma";
    public static final String AWARDER_KARMA_KEY = "awarder_karma";
    public static final String AWARDEE_KARMA_KEY = "awardee_karma";
    public static final String CONTENT_URLS_KEY = "content_urls";
    public static final String WEBM_KEY = "webm";
    public static final String WEBM_URL_KEY = "webmUrl";
    public static final String UPVOTE_RATIO_KEY = "upvote_ratio";
    public static final String INBOX_COUNT_KEY = "inbox_count";
    public static final String NEXT_CURSOR_KEY = "next_cursor";
    public static final String POST_KEY = "post";
    public static final String STYLES_KEY = "styles";
    public static final String AUTHOR_INFO_KEY= "authorInfo";
    public static final String VOTE_STATE_KEY = "voteState";
    public static final String UPVOTE_RATIO_CAMEL_CASE_KEY = "upvoteRatio";
    public static final String OUTBOUND_LINK_KEY = "outboundLink";
    public static final String IS_NSFW_KEY = "isNsfw";
    public static final String IS_LOCKED_KEY = "isLocked";
    public static final String IS_ARCHIVED_KEY = "isArchived";
    public static final String IS_SPOILER = "isSpoiler";
    public static final String SUGGESTED_COMMENT_SORT_CAMEL_CASE_KEY = "suggestedCommentSort";
    public static final String LIVE_COMMENTS_WEBSOCKET_KEY = "liveCommentsWebsocket";
    public static final String ICON_KEY = "icon";
    public static final String STREAM_KEY = "stream";
    public static final String STREAM_ID_KEY = "stream_id";
    public static final String THUMBNAIL_KEY = "thumbnail";
    public static final String PUBLISH_AT_KEY = "publish_at";
    public static final String STATE_KEY = "state";
    public static final String UPVOTES_KEY = "upvotes";
    public static final String DOWNVOTES_KEY = "downvotes";
    public static final String UNIQUE_WATCHERS_KEY = "unique_watchers";
    public static final String CONTINUOUS_WATCHERS_KEY = "continuous_watchers";
    public static final String TOTAL_CONTINUOUS_WATCHERS_KEY = "total_continuous_watchers";
    public static final String CHAT_DISABLED_KEY = "chat_disabled";
    public static final String BROADCAST_TIME_KEY = "broadcast_time";
    public static final String ESTIMATED_REMAINING_TIME_KEY = "estimated_remaining_time";
    public static final String PAYLOAD_KEY = "payload";
    public static final String AUTHOR_ICON_IMAGE = "author_icon_img";
    public static final String ASSET_KEY = "asset";
    public static final String ASSET_ID_KEY = "asset_id";
    public static final String TRENDING_SEARCHES_KEY = "trending_searches";
    public static final String QUERY_STRING_KEY = "query_string";
    public static final String DISPLAY_STRING_KEY = "display_string";
    public static final String RESULTS_KEY = "results";
    public static final String CONTENT_MD_KEY = "content_md";
    public static final String CAPTION_KEY = "caption";
    public static final String CAPTION_URL_KEY = "outbound_url";
    public static final String FILES_KEY = "files";
    public static final String MP4_MOBILE_KEY = "mp4-mobile";
    public static final String STATUS_KEY = "status";
    public static final String URLS_KEY = "urls";
    public static final String HD_KEY = "hd";
    public static final String SUGGESTED_SORT_KEY = "suggested_sort";
    public static final String P_KEY = "p";
    public static final String VARIANTS_KEY = "variants";
    public static final String PAGE_KEY = "page";
    public static final String SEND_REPLIES_KEY = "send_replies";
    public static final String PROFILE_IMG_KEY = "profile_img";
    public static final String AUTHOR_FULLNAME_KEY = "author_fullname";
    public static final String IS_MOD_KEY = "is_mod";
    public static final String CAN_MOD_POST_KEY = "can_mod_post";
    public static final String APPROVED_KEY = "approved";
    public static final String APPROVED_AT_UTC_KEY = "approved_at_utc";
    public static final String APPROVED_BY_KEY = "approved_by";
    public static final String SPAM_KEY = "spam";

    @Nullable
    public static Map<String, MediaMetadata> parseMediaMetadata(JSONObject data) {
        try {
            if (data.has(JSONUtils.MEDIA_METADATA_KEY)) {
                Map<String, MediaMetadata> mediaMetadataMap = new HashMap<>();
                JSONObject mediaMetadataJSON = data.getJSONObject(JSONUtils.MEDIA_METADATA_KEY);
                for (Iterator<String> it = mediaMetadataJSON.keys(); it.hasNext();) {
                    try {
                        String k = it.next();
                        JSONObject media = mediaMetadataJSON.getJSONObject(k);
                        String e = media.getString(JSONUtils.E_KEY);

                        JSONObject originalItemJSON = media.getJSONObject(JSONUtils.S_KEY);
                        MediaMetadata.MediaItem originalItem;
                        if (e.equalsIgnoreCase("Image")) {
                            originalItem = new MediaMetadata.MediaItem(originalItemJSON.getInt(JSONUtils.X_KEY),
                                    originalItemJSON.getInt(JSONUtils.Y_KEY), originalItemJSON.getString(JSONUtils.U_KEY));
                        } else {
                            if (originalItemJSON.has(JSONUtils.MP4_KEY)) {
                                originalItem = new MediaMetadata.MediaItem(originalItemJSON.getInt(JSONUtils.X_KEY),
                                        originalItemJSON.getInt(JSONUtils.Y_KEY), originalItemJSON.getString(JSONUtils.GIF_KEY),
                                        originalItemJSON.getString(JSONUtils.MP4_KEY));
                            } else {
                                originalItem = new MediaMetadata.MediaItem(originalItemJSON.getInt(JSONUtils.X_KEY),
                                        originalItemJSON.getInt(JSONUtils.Y_KEY), originalItemJSON.getString(JSONUtils.GIF_KEY));
                            }
                        }

                        MediaMetadata.MediaItem downscaledItem;
                        if (media.has(JSONUtils.P_KEY)) {
                            JSONArray downscales = media.getJSONArray(JSONUtils.P_KEY);
                            JSONObject downscaledItemJSON;
                            if (downscales.length() <= 0) {
                                downscaledItem = originalItem;
                            } else {
                                if (downscales.length() <= 3) {
                                    downscaledItemJSON = downscales.getJSONObject(downscales.length() - 1);
                                } else {
                                    downscaledItemJSON = downscales.getJSONObject(3);
                                }
                                downscaledItem = new MediaMetadata.MediaItem(downscaledItemJSON.getInt(JSONUtils.X_KEY),
                                        downscaledItemJSON.getInt(JSONUtils.Y_KEY), downscaledItemJSON.getString(JSONUtils.U_KEY));
                            }
                        } else {
                            downscaledItem = originalItem;
                        }

                        String id = media.getString(JSONUtils.ID_KEY);
                        mediaMetadataMap.put(id, new MediaMetadata(id, e, originalItem, downscaledItem));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return mediaMetadataMap;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}

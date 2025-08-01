package ml.docilealligator.infinityforreddit.post;

import android.net.Uri;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.readpost.ReadPostsListInterface;
import ml.docilealligator.infinityforreddit.thing.MediaMetadata;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

/**
 * Created by alex on 3/21/18.
 */

public class ParsePost {
    @WorkerThread
    public static LinkedHashSet<Post> parsePostsSync(String response, int nPosts, PostFilter postFilter, @Nullable ReadPostsListInterface readPostsList) {
        LinkedHashSet<Post> newPosts = new LinkedHashSet<>();
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray allPostsData = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);

            //Posts listing
            int numberOfPosts = (nPosts < 0 || nPosts > allPostsData.length()) ?
                    allPostsData.length() : nPosts;

            ArrayList<String> newPostsIds = new ArrayList<>();
            for (int i = 0; i < numberOfPosts; i++) {
                try {
                    if (!allPostsData.getJSONObject(i).getString(JSONUtils.KIND_KEY).equals("t3")) {
                        continue;
                    }
                    JSONObject data = allPostsData.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
                    Post post = parseBasicData(data);
                    if (PostFilter.isPostAllowed(post, postFilter)) {
                        newPosts.add(post);
                        newPostsIds.add(post.getId());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (readPostsList != null) {
                Set<String> readPostsIds = readPostsList.getReadPostsIdsByIds(newPostsIds);
                for (Post post: newPosts) {
                    if (readPostsIds.contains(post.getId())) {
                        post.markAsRead();
                    }
                }
            }

            return newPosts;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getLastItem(String response) {
        try {
            JSONObject object = new JSONObject(response).getJSONObject(JSONUtils.DATA_KEY);
            return object.isNull(JSONUtils.AFTER_KEY) ? null : object.getString(JSONUtils.AFTER_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void parsePost(Executor executor, Handler handler, String response, ParsePostListener parsePostListener) {
        executor.execute(() -> {
            try {
                JSONArray allData = new JSONArray(response).getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                if (allData.length() == 0) {
                    handler.post(parsePostListener::onParsePostFail);
                    return;
                }
                JSONObject data = allData.getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY);
                Post post = parseBasicData(data);
                handler.post(() -> parsePostListener.onParsePostSuccess(post));
            } catch (JSONException e) {
                e.printStackTrace();
                handler.post(parsePostListener::onParsePostFail);
            }
        });
    }

    @WorkerThread
    @Nullable
    public static Post parsePostSync(String response) {
        try {
            JSONArray allData = new JSONArray(response).getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
            if (allData.length() == 0) {
                return null;
            }
            JSONObject data = allData.getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY);
            return parseBasicData(data);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void parseRandomPost(Executor executor, Handler handler, String response, boolean isNSFW,
                                       ParseRandomPostListener parseRandomPostListener) {
        executor.execute(() -> {
            try {
                JSONArray postsArray = new JSONObject(response).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                if (postsArray.length() == 0) {
                    handler.post(parseRandomPostListener::onParseRandomPostFailed);
                } else {
                    JSONObject post = postsArray.getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY);
                    String subredditName = post.getString(JSONUtils.SUBREDDIT_KEY);
                    String postId;
                    if (isNSFW) {
                        postId = post.getString(JSONUtils.ID_KEY);
                    } else {
                        postId = post.getString(JSONUtils.LINK_ID_KEY).substring("t3_".length());
                    }
                    handler.post(() -> parseRandomPostListener.onParseRandomPostSuccess(postId, subredditName));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                handler.post(parseRandomPostListener::onParseRandomPostFailed);
            }
        });
    }

    @WorkerThread
    public static Post parseBasicData(JSONObject data) throws JSONException {
        String id = data.getString(JSONUtils.ID_KEY);
        String fullName = data.getString(JSONUtils.NAME_KEY);
        String subredditName = data.getString(JSONUtils.SUBREDDIT_KEY);
        String subredditNamePrefixed = data.getString(JSONUtils.SUBREDDIT_NAME_PREFIX_KEY);
        String author = data.getString(JSONUtils.AUTHOR_KEY);
        StringBuilder authorFlairHTMLBuilder = new StringBuilder();
        if (data.has(JSONUtils.AUTHOR_FLAIR_RICHTEXT_KEY)) {
            JSONArray flairArray = data.getJSONArray(JSONUtils.AUTHOR_FLAIR_RICHTEXT_KEY);
            for (int i = 0; i < flairArray.length(); i++) {
                JSONObject flairObject = flairArray.getJSONObject(i);
                String e = flairObject.getString(JSONUtils.E_KEY);
                if (e.equals("text")) {
                    authorFlairHTMLBuilder.append(flairObject.getString(JSONUtils.T_KEY));
                } else if (e.equals("emoji")) {
                    authorFlairHTMLBuilder.append("<img src=\"").append(Html.escapeHtml(flairObject.getString(JSONUtils.U_KEY))).append("\">");
                }
            }
        }
        String authorFlair = data.isNull(JSONUtils.AUTHOR_FLAIR_TEXT_KEY) ? "" : data.getString(JSONUtils.AUTHOR_FLAIR_TEXT_KEY);
        String distinguished = data.getString(JSONUtils.DISTINGUISHED_KEY);
        String suggestedSort = data.has(JSONUtils.SUGGESTED_SORT_KEY) ? data.getString(JSONUtils.SUGGESTED_SORT_KEY) : null;
        long postTime = data.getLong(JSONUtils.CREATED_UTC_KEY) * 1000;
        String title = data.getString(JSONUtils.TITLE_KEY);
        int score = data.getInt(JSONUtils.SCORE_KEY);
        int voteType;
        int nComments = data.getInt(JSONUtils.NUM_COMMENTS_KEY);
        int upvoteRatio = (int) (data.getDouble(JSONUtils.UPVOTE_RATIO_KEY) * 100);
        boolean hidden = data.getBoolean(JSONUtils.HIDDEN_KEY);
        boolean spoiler = data.getBoolean(JSONUtils.SPOILER_KEY);
        boolean nsfw = data.getBoolean(JSONUtils.NSFW_KEY);
        boolean stickied = data.getBoolean(JSONUtils.STICKIED_KEY);
        boolean archived = data.getBoolean(JSONUtils.ARCHIVED_KEY);
        boolean locked = data.getBoolean(JSONUtils.LOCKED_KEY);
        boolean saved = data.getBoolean(JSONUtils.SAVED_KEY);
        boolean deleted = !data.isNull(JSONUtils.REMOVED_BY_CATEGORY_KEY) && data.getString(JSONUtils.REMOVED_BY_CATEGORY_KEY).equals("deleted");
        boolean removed = !data.isNull(JSONUtils.REMOVED_BY_CATEGORY_KEY) && data.getString(JSONUtils.REMOVED_BY_CATEGORY_KEY).equals("moderator");
        boolean canModPost = data.getBoolean(JSONUtils.CAN_MOD_POST_KEY);
        boolean approved = data.has(JSONUtils.APPROVED_KEY) && data.getBoolean(JSONUtils.APPROVED_KEY);
        long approvedAtUTC = data.has(JSONUtils.APPROVED_AT_UTC_KEY) ? (data.isNull(JSONUtils.APPROVED_AT_UTC_KEY) ? 0 : data.getLong(JSONUtils.APPROVED_AT_UTC_KEY) * 1000) : 0;
        String approvedBy = data.has(JSONUtils.APPROVED_BY_KEY) ? data.getString(JSONUtils.APPROVED_BY_KEY) : null;
        boolean spam = data.has(JSONUtils.SPAM_KEY) && data.getBoolean(JSONUtils.SPAM_KEY);

        StringBuilder postFlairHTMLBuilder = new StringBuilder();
        String flair = "";
        if (data.has(JSONUtils.LINK_FLAIR_RICHTEXT_KEY)) {
            JSONArray flairArray = data.getJSONArray(JSONUtils.LINK_FLAIR_RICHTEXT_KEY);
            for (int i = 0; i < flairArray.length(); i++) {
                JSONObject flairObject = flairArray.getJSONObject(i);
                String e = flairObject.getString(JSONUtils.E_KEY);
                if (e.equals("text")) {
                    postFlairHTMLBuilder.append(Html.escapeHtml(flairObject.getString(JSONUtils.T_KEY)));
                } else if (e.equals("emoji")) {
                    postFlairHTMLBuilder.append("<img src=\"").append(Html.escapeHtml(flairObject.getString(JSONUtils.U_KEY))).append("\">");
                }
            }
            flair = postFlairHTMLBuilder.toString();
        }

        if (flair.equals("") && data.has(JSONUtils.LINK_FLAIR_TEXT_KEY) && !data.isNull(JSONUtils.LINK_FLAIR_TEXT_KEY)) {
            flair = data.getString(JSONUtils.LINK_FLAIR_TEXT_KEY);
        }

        if (data.isNull(JSONUtils.LIKES_KEY)) {
            voteType = 0;
        } else {
            voteType = data.getBoolean(JSONUtils.LIKES_KEY) ? 1 : -1;
            score -= voteType;
        }

        String permalink = Html.fromHtml(data.getString(JSONUtils.PERMALINK_KEY)).toString();

        ArrayList<Post.Preview> previews = new ArrayList<>();
        if (data.has(JSONUtils.PREVIEW_KEY)) {
            JSONObject images = data.getJSONObject(JSONUtils.PREVIEW_KEY).getJSONArray(JSONUtils.IMAGES_KEY).getJSONObject(0);
            String previewUrl = images.getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY);
            int previewWidth = images.getJSONObject(JSONUtils.SOURCE_KEY).getInt(JSONUtils.WIDTH_KEY);
            int previewHeight = images.getJSONObject(JSONUtils.SOURCE_KEY).getInt(JSONUtils.HEIGHT_KEY);
            previews.add(new Post.Preview(previewUrl, previewWidth, previewHeight, "", ""));

            JSONArray thumbnailPreviews = images.getJSONArray(JSONUtils.RESOLUTIONS_KEY);
            for (int i = 0; i < thumbnailPreviews.length(); i++) {
                JSONObject thumbnailPreview = thumbnailPreviews.getJSONObject(i);
                String thumbnailPreviewUrl = thumbnailPreview.getString(JSONUtils.URL_KEY);
                int thumbnailPreviewWidth = thumbnailPreview.getInt(JSONUtils.WIDTH_KEY);
                int thumbnailPreviewHeight = thumbnailPreview.getInt(JSONUtils.HEIGHT_KEY);

                previews.add(new Post.Preview(thumbnailPreviewUrl, thumbnailPreviewWidth, thumbnailPreviewHeight, "", ""));
            }
        }

        Map<String, MediaMetadata> mediaMetadataMap = JSONUtils.parseMediaMetadata(data);
        if (data.has(JSONUtils.CROSSPOST_PARENT_LIST)) {
            //Cross post
            //data.getJSONArray(JSONUtils.CROSSPOST_PARENT_LIST).getJSONObject(0) out of bounds????????????
            data = data.getJSONArray(JSONUtils.CROSSPOST_PARENT_LIST).getJSONObject(0);
            Post crosspostParent = parseBasicData(data);
            Post post = parseData(data, permalink, id, fullName, subredditName, subredditNamePrefixed,
                    author, authorFlair, authorFlairHTMLBuilder.toString(),
                    postTime, title, previews, mediaMetadataMap,
                    score, voteType, nComments, upvoteRatio, flair, hidden,
                    spoiler, nsfw, stickied, archived, locked, saved, deleted, removed, true, canModPost,
                    approved, approvedAtUTC, approvedBy, spam, distinguished, suggestedSort);
            post.setCrosspostParentId(crosspostParent.getId());
            return post;
        } else {
            return parseData(data, permalink, id, fullName, subredditName, subredditNamePrefixed,
                    author, authorFlair, authorFlairHTMLBuilder.toString(),
                    postTime, title, previews, mediaMetadataMap,
                    score, voteType, nComments, upvoteRatio, flair, hidden,
                    spoiler, nsfw, stickied, archived, locked, saved, deleted, removed, false, canModPost,
                    approved, approvedAtUTC, approvedBy, spam, distinguished, suggestedSort);
        }
    }

    private static Post parseData(JSONObject data, String permalink, String id, String fullName,
                                  String subredditName, String subredditNamePrefixed, String author,
                                  String authorFlair, String authorFlairHTML, long postTimeMillis, String title,
                                  ArrayList<Post.Preview> previews, Map<String, MediaMetadata> mediaMetadataMap,
                                  int score, int voteType, int nComments, int upvoteRatio, String flair,
                                  boolean hidden, boolean spoiler, boolean nsfw,
                                  boolean stickied, boolean archived, boolean locked, boolean saved,
                                  boolean deleted, boolean removed, boolean isCrosspost, boolean canModPost,
                                  boolean approved, long approvedAtUTC, String approvedBy, boolean spam,
                                  String distinguished, String suggestedSort) throws JSONException {
        Post post;

        boolean isVideo = data.getBoolean(JSONUtils.IS_VIDEO_KEY);
        String url = Html.fromHtml(data.getString(JSONUtils.URL_KEY)).toString();
        Uri uri = Uri.parse(url);
        String path = uri.getPath();

        if (!data.has(JSONUtils.PREVIEW_KEY) && previews.isEmpty()) {
            if (url.contains(permalink)) {
                //Text post
                int postType = Post.TEXT_TYPE;
                post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                        authorFlair, authorFlairHTML, postTimeMillis, title, permalink, score, postType,
                        voteType, nComments, upvoteRatio, flair, hidden, spoiler, nsfw,
                        stickied, archived, locked, saved, isCrosspost, canModPost, approved, approvedAtUTC,
                        approvedBy, removed, spam, distinguished, suggestedSort);
            } else {
                if (path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".jpeg")) {
                    //Image post
                    int postType = Post.IMAGE_TYPE;

                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                            authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                            postType, voteType, nComments, upvoteRatio, flair, hidden,
                            spoiler, nsfw, stickied, archived, locked, saved, isCrosspost, canModPost, approved,
                            approvedAtUTC, approvedBy, removed, spam, distinguished, suggestedSort);

                    if (previews.isEmpty()) {
                        if ("i.redgifs.com".equals(uri.getAuthority())) {
                            //No preview link (Not able to load redgifs image)
                            post.setPostType(Post.NO_PREVIEW_LINK_TYPE);
                        } else {
                            previews.add(new Post.Preview(url, 0, 0, "", ""));
                        }
                    } else if ("i.redgifs.com".equals(uri.getAuthority())) {
                        post.setUrl(previews.get(previews.size() - 1).getPreviewUrl());
                    }
                    post.setPreviews(previews);
                } else {
                    if (isVideo) {
                        //No preview video post
                        /*
                            TODO a removed crosspost may not have media JSONObject. This happens in crosspost_parent_list
                            e.g. https://www.reddit.com/r/hitmanimals/comments/1l6pv0m/mission_failed_agent_47/
                         */
                        JSONObject redditVideoObject = data.getJSONObject(JSONUtils.MEDIA_KEY).getJSONObject(JSONUtils.REDDIT_VIDEO_KEY);
                        int postType = Post.VIDEO_TYPE;
                        String videoUrl = Html.fromHtml(redditVideoObject.getString(JSONUtils.HLS_URL_KEY)).toString();
                        String videoDownloadUrl = redditVideoObject.getString(JSONUtils.FALLBACK_URL_KEY);

                        post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, authorFlair,
                                authorFlairHTML, postTimeMillis, title, permalink, score, postType, voteType,
                                nComments, upvoteRatio, flair, hidden, spoiler, nsfw, stickied,
                                archived, locked, saved, isCrosspost, canModPost, approved, approvedAtUTC,
                                approvedBy, removed, spam, distinguished, suggestedSort);

                        post.setVideoUrl(videoUrl);
                        post.setVideoDownloadUrl(videoDownloadUrl);
                    } else {
                        //No preview link post
                        int postType = Post.NO_PREVIEW_LINK_TYPE;
                        post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                                authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                                postType, voteType, nComments, upvoteRatio, flair, hidden,
                                spoiler, nsfw, stickied, archived, locked, saved, isCrosspost, canModPost,
                                approved, approvedAtUTC, approvedBy, removed, spam, distinguished,
                                suggestedSort);
                        if (data.isNull(JSONUtils.SELFTEXT_KEY)) {
                            post.setSelfText("");
                        } else {
                            post.setSelfText(Utils.parseRedditImagesBlock(Utils.modifyMarkdown(Utils.trimTrailingWhitespace(data.getString(JSONUtils.SELFTEXT_KEY))), mediaMetadataMap));
                        }

                        String authority = uri.getAuthority();

                        if (authority != null) {
                            /*if (authority.contains("redgifs.com")) {
                                String redgifsId = url.substring(url.lastIndexOf("/") + 1).toLowerCase();
                                post.setPostType(Post.VIDEO_TYPE);
                                post.setIsRedgifs(true);
                                post.setVideoUrl(url);
                                post.setRedgifsId(redgifsId);
                            } else */
                            if (authority.equals("streamable.com")) {
                                String shortCode = url.substring(url.lastIndexOf("/") + 1);
                                post.setPostType(Post.VIDEO_TYPE);
                                post.setIsStreamable(true);
                                post.setVideoUrl(url);
                                post.setStreamableShortCode(shortCode);
                            }
                        }
                    }
                }
            }
        } else {
            if (previews.isEmpty()) {
                if (data.has(JSONUtils.PREVIEW_KEY)) {
                    JSONObject images = data.getJSONObject(JSONUtils.PREVIEW_KEY).getJSONArray(JSONUtils.IMAGES_KEY).getJSONObject(0);
                    String previewUrl = images.getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY);
                    int previewWidth = images.getJSONObject(JSONUtils.SOURCE_KEY).getInt(JSONUtils.WIDTH_KEY);
                    int previewHeight = images.getJSONObject(JSONUtils.SOURCE_KEY).getInt(JSONUtils.HEIGHT_KEY);
                    previews.add(new Post.Preview(previewUrl, previewWidth, previewHeight, "", ""));

                    JSONArray thumbnailPreviews = images.getJSONArray(JSONUtils.RESOLUTIONS_KEY);
                    for (int i = 0; i < thumbnailPreviews.length(); i++) {
                        JSONObject thumbnailPreview = images.getJSONArray(JSONUtils.RESOLUTIONS_KEY).getJSONObject(i);
                        String thumbnailPreviewUrl = thumbnailPreview.getString(JSONUtils.URL_KEY);
                        int thumbnailPreviewWidth = thumbnailPreview.getInt(JSONUtils.WIDTH_KEY);
                        int thumbnailPreviewHeight = thumbnailPreview.getInt(JSONUtils.HEIGHT_KEY);

                        previews.add(new Post.Preview(thumbnailPreviewUrl, thumbnailPreviewWidth, thumbnailPreviewHeight, "", ""));
                    }
                }
            }

            if (isVideo) {
                //Video post
                JSONObject redditVideoObject = data.getJSONObject(JSONUtils.MEDIA_KEY).getJSONObject(JSONUtils.REDDIT_VIDEO_KEY);
                int postType = Post.VIDEO_TYPE;
                String videoUrl = Html.fromHtml(redditVideoObject.getString(JSONUtils.HLS_URL_KEY)).toString();
                String videoDownloadUrl = redditVideoObject.getString(JSONUtils.FALLBACK_URL_KEY);

                post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, authorFlair,
                        authorFlairHTML, postTimeMillis, title, permalink, score, postType, voteType,
                        nComments, upvoteRatio, flair, hidden, spoiler, nsfw, stickied,
                        archived, locked, saved, isCrosspost, canModPost, approved, approvedAtUTC,
                        approvedBy, removed, spam, distinguished, suggestedSort);

                post.setPreviews(previews);
                post.setVideoUrl(videoUrl);
                post.setVideoDownloadUrl(videoDownloadUrl);
            } else if (data.has(JSONUtils.PREVIEW_KEY)) {
                if (data.getJSONObject(JSONUtils.PREVIEW_KEY).has(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY)) {
                    int postType = Post.VIDEO_TYPE;
                    String authority = uri.getAuthority();
                    // The hls stream inside REDDIT_VIDEO_PREVIEW_KEY can sometimes lack an audio track
                    if (authority.contains("imgur.com") && (path.endsWith(".gifv") || path.endsWith(".mp4"))) {
                        if (path.endsWith(".gifv")) {
                            url = url.substring(0, url.length() - 5) + ".mp4";
                        }

                        post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, authorFlair,
                                authorFlairHTML, postTimeMillis, title, permalink, score, postType, voteType,
                                nComments, upvoteRatio, flair, hidden, spoiler, nsfw, stickied,
                                archived, locked, saved, isCrosspost, canModPost, approved, approvedAtUTC,
                                approvedBy, removed, spam, distinguished, suggestedSort);
                        post.setPreviews(previews);
                        post.setVideoUrl(url);
                        post.setVideoDownloadUrl(url);
                        post.setIsImgur(true);
                    } else {
                        //Gif video post (HLS) and maybe Redgifs

                        String videoUrl = Html.fromHtml(data.getJSONObject(JSONUtils.PREVIEW_KEY)
                                .getJSONObject(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY).getString(JSONUtils.HLS_URL_KEY)).toString();
                        String videoDownloadUrl = data.getJSONObject(JSONUtils.PREVIEW_KEY)
                                .getJSONObject(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY).getString(JSONUtils.FALLBACK_URL_KEY);

                        post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, authorFlair,
                                authorFlairHTML, postTimeMillis, title, permalink, score, postType, voteType,
                                nComments, upvoteRatio, flair, hidden, spoiler, nsfw, stickied,
                                archived, locked, saved, isCrosspost, canModPost, approved, approvedAtUTC,
                                approvedBy, removed, spam, distinguished, suggestedSort);
                        post.setPreviews(previews);
                        post.setVideoUrl(videoUrl);
                        post.setVideoDownloadUrl(videoDownloadUrl);
                    }
                    post.setVideoFallBackDirectUrl(Html.fromHtml(data.getJSONObject(JSONUtils.PREVIEW_KEY)
                            .getJSONObject(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY).getString(JSONUtils.FALLBACK_URL_KEY)).toString());
                } else {
                    if (path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".jpeg")) {
                        //Image post
                        int postType = Post.IMAGE_TYPE;

                        post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                                authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                                postType, voteType, nComments, upvoteRatio, flair,
                                hidden, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost, canModPost,
                                approved, approvedAtUTC, approvedBy, removed, spam, distinguished,
                                suggestedSort);

                        if (previews.isEmpty()) {
                            if ("i.redgifs.com".equals(uri.getAuthority())) {
                                //No preview link (Not able to load redgifs image)
                                post.setPostType(Post.NO_PREVIEW_LINK_TYPE);
                            } else {
                                previews.add(new Post.Preview(url, 0, 0, "", ""));
                            }
                        } else if ("i.redgifs.com".equals(uri.getAuthority())) {
                            post.setUrl(previews.get(previews.size() - 1).getPreviewUrl());
                        }
                        post.setPreviews(previews);
                    } else if (path.endsWith(".gif")) {
                        //Gif post
                        int postType = Post.GIF_TYPE;
                        post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                                authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                                postType, voteType, nComments, upvoteRatio, flair,
                                hidden, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost, canModPost,
                                approved, approvedAtUTC, approvedBy, removed, spam, distinguished,
                                suggestedSort);

                        post.setPreviews(previews);
                        post.setVideoUrl(url);

                        try {
                            String mp4Variant = data.getJSONObject(JSONUtils.PREVIEW_KEY)
                                    .getJSONArray(JSONUtils.IMAGES_KEY).getJSONObject(0)
                                    .getJSONObject(JSONUtils.VARIANTS_KEY).getJSONObject(JSONUtils.MP4_KEY)
                                    .getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY);
                            if (!mp4Variant.isEmpty()) {
                                post.setMp4Variant(mp4Variant);
                            }
                        } catch (Exception ignore) {}
                    } else if (uri.getAuthority().contains("imgur.com") && (path.endsWith(".gifv") || path.endsWith(".mp4"))) {
                        // Imgur gifv/mp4
                        int postType = Post.VIDEO_TYPE;

                        if (url.endsWith("gifv")) {
                            url = url.substring(0, url.length() - 5) + ".mp4";
                        }

                        post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                                authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                                postType, voteType, nComments, upvoteRatio, flair,
                                hidden, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost, canModPost,
                                approved, approvedAtUTC, approvedBy, removed, spam, distinguished,
                                suggestedSort);
                        post.setPreviews(previews);
                        post.setVideoUrl(url);
                        post.setVideoDownloadUrl(url);
                        post.setIsImgur(true);
                    } else if (path.endsWith(".mp4")) {
                        //Video post
                        int postType = Post.VIDEO_TYPE;

                        post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                                authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                                postType, voteType, nComments, upvoteRatio, flair,
                                hidden, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost, canModPost,
                                approved, approvedAtUTC, approvedBy, removed, spam, distinguished,
                                suggestedSort);
                        post.setPreviews(previews);
                        post.setVideoUrl(url);
                        post.setVideoDownloadUrl(url);
                    } else {
                        if (url.contains(permalink)) {
                            //Text post but with a preview
                            int postType = Post.TEXT_TYPE;

                            post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                                    authorFlair, authorFlairHTML, postTimeMillis, title, permalink, score,
                                    postType, voteType, nComments, upvoteRatio, flair,
                                    hidden, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost, canModPost,
                                    approved, approvedAtUTC, approvedBy, removed, spam, distinguished,
                                    suggestedSort);

                            //Need attention
                            post.setPreviews(previews);
                        } else {
                            //Link post
                            int postType = Post.LINK_TYPE;

                            post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                                    authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                                    postType, voteType, nComments, upvoteRatio, flair,
                                    hidden, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost, canModPost,
                                    approved, approvedAtUTC, approvedBy, removed, spam, distinguished,
                                    suggestedSort);
                            if (data.isNull(JSONUtils.SELFTEXT_KEY)) {
                                post.setSelfText("");
                            } else {
                                post.setSelfText(Utils.parseRedditImagesBlock(Utils.modifyMarkdown(Utils.trimTrailingWhitespace(data.getString(JSONUtils.SELFTEXT_KEY))), mediaMetadataMap));
                            }

                            post.setPreviews(previews);

                            String authority = uri.getAuthority();

                            if (authority != null) {
                                /*if (authority.contains("redgifs.com")) {
                                    String redgifsId = url.substring(url.lastIndexOf("/") + 1).toLowerCase();
                                    post.setPostType(Post.VIDEO_TYPE);
                                    post.setIsRedgifs(true);
                                    post.setVideoUrl(url);
                                    post.setRedgifsId(redgifsId);
                                } else*/
                                if (authority.equals("streamable.com")) {
                                    String shortCode = url.substring(url.lastIndexOf("/") + 1);
                                    post.setPostType(Post.VIDEO_TYPE);
                                    post.setIsStreamable(true);
                                    post.setVideoUrl(url);
                                    post.setStreamableShortCode(shortCode);
                                }
                            }
                        }
                    }
                }
            } else {
                if (path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".jpeg")) {
                    //Image post
                    int postType = Post.IMAGE_TYPE;

                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                            authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                            postType, voteType, nComments, upvoteRatio, flair, hidden,
                            spoiler, nsfw, stickied, archived, locked, saved, isCrosspost, canModPost,
                            approved, approvedAtUTC, approvedBy, removed, spam, distinguished,
                            suggestedSort);

                    if (previews.isEmpty()) {
                        if ("i.redgifs.com".equals(uri.getAuthority())) {
                            //No preview link (Not able to load redgifs image)
                            post.setPostType(Post.NO_PREVIEW_LINK_TYPE);
                        } else {
                            previews.add(new Post.Preview(url, 0, 0, "", ""));
                        }
                    } else if ("i.redgifs.com".equals(uri.getAuthority())) {
                        post.setUrl(previews.get(previews.size() - 1).getPreviewUrl());
                    }
                    post.setPreviews(previews);
                } else if (path.endsWith(".mp4")) {
                    //Video post
                    int postType = Post.VIDEO_TYPE;

                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                            authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                            postType, voteType, nComments, upvoteRatio, flair, hidden,
                            spoiler, nsfw, stickied, archived, locked, saved, isCrosspost, canModPost,
                            approved, approvedAtUTC, approvedBy, removed, spam, distinguished, suggestedSort);
                    post.setPreviews(previews);
                    post.setVideoUrl(url);
                    post.setVideoDownloadUrl(url);
                } else {
                    //CP No Preview Link post
                    int postType = Post.NO_PREVIEW_LINK_TYPE;

                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                            authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                            postType, voteType, nComments, upvoteRatio, flair, hidden,
                            spoiler, nsfw, stickied, archived, locked, saved, isCrosspost, canModPost,
                            approved, approvedAtUTC, approvedBy, removed, spam, distinguished, suggestedSort);
                    //Need attention
                    if (data.isNull(JSONUtils.SELFTEXT_KEY)) {
                        post.setSelfText("");
                    } else {
                        post.setSelfText(Utils.parseRedditImagesBlock(Utils.modifyMarkdown(Utils.trimTrailingWhitespace(data.getString(JSONUtils.SELFTEXT_KEY))), mediaMetadataMap));
                    }

                    String authority = uri.getAuthority();

                    if (authority != null) {
                        /*if (authority.contains("redgifs.com")) {
                            String redgifsId = url.substring(url.lastIndexOf("/") + 1).toLowerCase();
                            post.setPostType(Post.VIDEO_TYPE);
                            post.setIsRedgifs(true);
                            post.setVideoUrl(url);
                            post.setRedgifsId(redgifsId);
                        } else*/
                        if (authority.equals("streamable.com")) {
                            String shortCode = url.substring(url.lastIndexOf("/") + 1);
                            post.setPostType(Post.VIDEO_TYPE);
                            post.setIsStreamable(true);
                            post.setVideoUrl(url);
                            post.setStreamableShortCode(shortCode);
                        }
                    }
                }
            }
        }

        if (post.getPostType() == Post.VIDEO_TYPE) {
            try {
                String authority = uri.getAuthority();
                if (authority != null) {
                    /*if (authority.contains("redgifs.com")) {
                        String redgifsId = url.substring(url.lastIndexOf("/") + 1);
                        if (redgifsId.contains("-")) {
                            redgifsId = redgifsId.substring(0, redgifsId.indexOf('-'));
                        }
                        post.setIsRedgifs(true);
                        post.setVideoUrl(url);
                        post.setRedgifsId(redgifsId.toLowerCase());
                    } else*/
                    if (authority.equals("streamable.com")) {
                        String shortCode = url.substring(url.lastIndexOf("/") + 1);
                        post.setPostType(Post.VIDEO_TYPE);
                        post.setIsStreamable(true);
                        post.setVideoUrl(url);
                        post.setStreamableShortCode(shortCode);
                    }
                }
            } catch (IllegalArgumentException ignore) { }
        } else if (post.getPostType() == Post.LINK_TYPE || post.getPostType() == Post.NO_PREVIEW_LINK_TYPE) {
            if (!data.isNull(JSONUtils.GALLERY_DATA_KEY)) {
                JSONArray galleryIdsArray = data.getJSONObject(JSONUtils.GALLERY_DATA_KEY).getJSONArray(JSONUtils.ITEMS_KEY);
                JSONObject galleryObject = data.getJSONObject(JSONUtils.MEDIA_METADATA_KEY);
                ArrayList<Post.Gallery> gallery = new ArrayList<>();
                for (int i = 0; i < galleryIdsArray.length(); i++) {
                    String galleryId = galleryIdsArray.getJSONObject(i).getString(JSONUtils.MEDIA_ID_KEY);
                    JSONObject singleGalleryObject = galleryObject.getJSONObject(galleryId);
                    String mimeType = singleGalleryObject.getString(JSONUtils.M_KEY);
                    String galleryItemUrl;
                    if (mimeType.contains("jpg") || mimeType.contains("png")) {
                        galleryItemUrl = singleGalleryObject.getJSONObject(JSONUtils.S_KEY).getString(JSONUtils.U_KEY);
                    } else {
                        JSONObject sourceObject = singleGalleryObject.getJSONObject(JSONUtils.S_KEY);
                        if (mimeType.contains("gif")) {
                            galleryItemUrl = sourceObject.getString(JSONUtils.GIF_KEY);
                        } else {
                            galleryItemUrl = sourceObject.getString(JSONUtils.MP4_KEY);
                        }
                    }

                    JSONObject galleryItem = galleryIdsArray.getJSONObject(i);
                    String galleryItemCaption = "";
                    String galleryItemCaptionUrl = "";
                    if (galleryItem.has(JSONUtils.CAPTION_KEY)) {
                        galleryItemCaption = galleryItem.getString(JSONUtils.CAPTION_KEY).trim();
                    }

                    if (galleryItem.has(JSONUtils.CAPTION_URL_KEY)) {
                        galleryItemCaptionUrl = galleryItem.getString(JSONUtils.CAPTION_URL_KEY).trim();
                    }

                    if (previews.isEmpty() && (mimeType.contains("jpg") || mimeType.contains("png"))) {
                        previews.add(new Post.Preview(galleryItemUrl, singleGalleryObject.getJSONObject(JSONUtils.S_KEY).getInt(JSONUtils.X_KEY),
                                singleGalleryObject.getJSONObject(JSONUtils.S_KEY).getInt(JSONUtils.Y_KEY), galleryItemCaption, galleryItemCaptionUrl));
                    }
                    
                    Post.Gallery postGalleryItem = new Post.Gallery(mimeType, galleryItemUrl, "", subredditName + "-" + galleryId + "." + mimeType.substring(mimeType.lastIndexOf("/") + 1), galleryItemCaption, galleryItemCaptionUrl);

                    // For issue #558
                    // Construct a fallback image url
                    if (!TextUtils.isEmpty(galleryItemUrl) && !TextUtils.isEmpty(mimeType) && (mimeType.contains("jpg") || mimeType.contains("png"))) {
                        postGalleryItem.setFallbackUrl("https://i.redd.it/" + galleryId + "." +  mimeType.substring(mimeType.lastIndexOf("/") + 1));
                        postGalleryItem.setHasFallback(true);
                    }

                    gallery.add(postGalleryItem);
                }

                if (!gallery.isEmpty()) {
                    post.setPostType(Post.GALLERY_TYPE);
                    post.setGallery(gallery);
                    post.setPreviews(previews);
                }
            } else if (post.getPostType() == Post.LINK_TYPE) {
                String authority = uri.getAuthority();

                if (authority != null) {
                    /*if (authority.contains("redgifs.com")) {
                        String redgifsId = url.substring(url.lastIndexOf("/") + 1).toLowerCase();
                        post.setPostType(Post.VIDEO_TYPE);
                        post.setIsRedgifs(true);
                        post.setVideoUrl(url);
                        post.setRedgifsId(redgifsId);
                    } else*/
                    if (authority.equals("streamable.com")) {
                        String shortCode = url.substring(url.lastIndexOf("/") + 1);
                        post.setPostType(Post.VIDEO_TYPE);
                        post.setIsStreamable(true);
                        post.setVideoUrl(url);
                        post.setStreamableShortCode(shortCode);
                    }
                }
            }
        }

        if (post.getPostType() != Post.LINK_TYPE && post.getPostType() != Post.NO_PREVIEW_LINK_TYPE) {
            if (data.isNull(JSONUtils.SELFTEXT_KEY)) {
                post.setSelfText("");
            } else {
                String selfText = Utils.parseRedditImagesBlock(Utils.modifyMarkdown(Utils.trimTrailingWhitespace(data.getString(JSONUtils.SELFTEXT_KEY))), mediaMetadataMap);
                post.setSelfText(selfText);
                if (data.isNull(JSONUtils.SELFTEXT_HTML_KEY)) {
                    post.setSelfTextPlainTrimmed("");
                } else {
                    String selfTextPlain = Utils.trimTrailingWhitespace(
                            Html.fromHtml(data.getString(JSONUtils.SELFTEXT_HTML_KEY))).toString();
                    post.setSelfTextPlain(selfTextPlain);
                    if (selfTextPlain.length() > 250) {
                        selfTextPlain = selfTextPlain.substring(0, 250);
                    }
                    if (!selfText.equals("")) {
                        Pattern p = Pattern.compile(">!.+!<");
                        Matcher m = p.matcher(selfText.substring(0, Math.min(selfText.length(), 400)));
                        if (m.find()) {
                            post.setSelfTextPlainTrimmed("");
                        } else {
                            post.setSelfTextPlainTrimmed(selfTextPlain);
                        }
                    } else {
                        post.setSelfTextPlainTrimmed(selfTextPlain);
                    }
                }
            }
        }

        post.setMediaMetadataMap(mediaMetadataMap);
        return post;
    }

    public interface ParsePostsListingListener {
        void onParsePostsListingSuccess(LinkedHashSet<Post> newPostData, String lastItem);
        void onParsePostsListingFail();
    }

    public interface ParsePostListener {
        void onParsePostSuccess(Post post);
        void onParsePostFail();
    }

    public interface ParseRandomPostListener {
        void onParseRandomPostSuccess(String postId, String subredditName);
        void onParseRandomPostFailed();
    }
}

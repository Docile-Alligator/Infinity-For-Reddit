package ml.docilealligator.infinityforreddit.post;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Html;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.readpost.ReadPost;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

/**
 * Created by alex on 3/21/18.
 */

public class ParsePost {
    public static void parsePosts(Executor executor, Handler handler, String response, int nPosts,
                                  PostFilter postFilter, List<ReadPost> readPostList,
                                  ParsePostsListingListener parsePostsListingListener) {
        executor.execute(() -> {
            boolean parseFailed = false;
            LinkedHashSet<Post> newPosts = new LinkedHashSet<>();
            String lastItem = null;
            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray allData = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                lastItem = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);

                //Posts listing
                int size;
                if (nPosts < 0 || nPosts > allData.length()) {
                    size = allData.length();
                } else {
                    size = nPosts;
                }

                HashSet<ReadPost> readPostHashSet = null;
                if (readPostList != null) {
                    readPostHashSet = new HashSet<>(readPostList);
                }
                for (int i = 0; i < size; i++) {
                    try {
                        if (allData.getJSONObject(i).getString(JSONUtils.KIND_KEY).equals("t3")) {
                            JSONObject data = allData.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
                            Post post = parseBasicData(data);
                            if (readPostHashSet != null && readPostHashSet.contains(ReadPost.convertPost(post))) {
                                post.markAsRead(false);
                            }
                            if (PostFilter.isPostAllowed(post, postFilter)) {
                                newPosts.add(post);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                parseFailed = true;
            }

            if (!parseFailed) {
                String finalLastItem = lastItem;
                handler.post(() -> parsePostsListingListener.onParsePostsListingSuccess(newPosts, finalLastItem));
            } else {
                handler.post(parsePostsListingListener::onParsePostsListingFail);
            }
        });
    }

    public static void parsePost(String response, ParsePostListener parsePostListener) {
        PostFilter postFilter = new PostFilter();
        postFilter.allowNSFW = true;
        new ParsePostDataAsyncTask(response, postFilter, parsePostListener).execute();
    }

    private static Post parseBasicData(JSONObject data) throws JSONException {
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
        boolean locked = data.getBoolean(JSONUtils.LOCKEC_KEY);
        boolean saved = data.getBoolean(JSONUtils.SAVED_KEY);
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
        
        StringBuilder awardingsBuilder = new StringBuilder();
        JSONArray awardingsArray = data.getJSONArray(JSONUtils.ALL_AWARDINGS_KEY);
        int nAwards = 0;
        for (int i = 0; i < awardingsArray.length(); i++) {
            JSONObject award = awardingsArray.getJSONObject(i);
            int count = award.getInt(JSONUtils.COUNT_KEY);
            nAwards += count;
            JSONArray icons = award.getJSONArray(JSONUtils.RESIZED_ICONS_KEY);
            if (icons.length() > 4) {
                String iconUrl = icons.getJSONObject(3).getString(JSONUtils.URL_KEY);
                awardingsBuilder.append("<img src=\"").append(Html.escapeHtml(iconUrl)).append("\"> ").append("x").append(count).append(" ");
            } else if (icons.length() > 0) {
                String iconUrl = icons.getJSONObject(icons.length() - 1).getString(JSONUtils.URL_KEY);
                awardingsBuilder.append("<img src=\"").append(Html.escapeHtml(iconUrl)).append("\"> ").append("x").append(count).append(" ");
            }
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
            previews.add(new Post.Preview(previewUrl, previewWidth, previewHeight));

            JSONArray thumbnailPreviews = images.getJSONArray(JSONUtils.RESOLUTIONS_KEY);
            for (int i = 0; i < thumbnailPreviews.length(); i++) {
                JSONObject thumbnailPreview = thumbnailPreviews.getJSONObject(i);
                String thumbnailPreviewUrl = thumbnailPreview.getString(JSONUtils.URL_KEY);
                int thumbnailPreviewWidth = thumbnailPreview.getInt(JSONUtils.WIDTH_KEY);
                int thumbnailPreviewHeight = thumbnailPreview.getInt(JSONUtils.HEIGHT_KEY);

                previews.add(new Post.Preview(thumbnailPreviewUrl, thumbnailPreviewWidth, thumbnailPreviewHeight));
            }
        }
        if (data.has(JSONUtils.CROSSPOST_PARENT_LIST)) {
            //Cross post
            //data.getJSONArray(JSONUtils.CROSSPOST_PARENT_LIST).getJSONObject(0) out of bounds????????????
            data = data.getJSONArray(JSONUtils.CROSSPOST_PARENT_LIST).getJSONObject(0);
            Post crosspostParent = parseBasicData(data);
            Post post = parseData(data, permalink, id, fullName, subredditName, subredditNamePrefixed,
                    author, authorFlair, authorFlairHTMLBuilder.toString(),
                    postTime, title, previews,
                    score, voteType, nComments, upvoteRatio, flair, awardingsBuilder.toString(), nAwards, hidden,
                    spoiler, nsfw, stickied, archived, locked, saved, true);
            post.setCrosspostParentId(crosspostParent.getId());
            return post;
        } else {
            return parseData(data, permalink, id, fullName, subredditName, subredditNamePrefixed,
                    author, authorFlair, authorFlairHTMLBuilder.toString(),
                    postTime, title, previews,
                    score, voteType, nComments, upvoteRatio, flair, awardingsBuilder.toString(), nAwards, hidden,
                    spoiler, nsfw, stickied, archived, locked, saved, false);
        }
    }

    private static Post parseData(JSONObject data, String permalink, String id, String fullName,
                                  String subredditName, String subredditNamePrefixed, String author,
                                  String authorFlair, String authorFlairHTML,
                                  long postTimeMillis, String title, ArrayList<Post.Preview> previews,
                                  int score, int voteType, int nComments, int upvoteRatio, String flair,
                                  String awards, int nAwards, boolean hidden, boolean spoiler,
                                  boolean nsfw, boolean stickied, boolean archived, boolean locked,
                                  boolean saved, boolean isCrosspost) throws JSONException {
        Post post;

        boolean isVideo = data.getBoolean(JSONUtils.IS_VIDEO_KEY);
        String url = Html.fromHtml(data.getString(JSONUtils.URL_KEY)).toString();

        if (!data.has(JSONUtils.PREVIEW_KEY) && previews.isEmpty()) {
            if (url.contains(permalink)) {
                //Text post
                int postType = Post.TEXT_TYPE;
                post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                        authorFlair, authorFlairHTML, postTimeMillis, title, permalink, score, postType,
                        voteType, nComments, upvoteRatio, flair, awards, nAwards, hidden, spoiler, nsfw,
                        stickied, archived, locked, saved, isCrosspost);
                if (data.isNull(JSONUtils.SELFTEXT_KEY)) {
                    post.setSelfText("");
                } else {
                    String selfText = Utils.modifyMarkdown(data.getString(JSONUtils.SELFTEXT_KEY).trim());
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
            } else {
                if (url.endsWith("jpg") || url.endsWith("png")) {
                    //Image post
                    int postType = Post.IMAGE_TYPE;

                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                            authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                            postType, voteType, nComments, upvoteRatio, flair, awards, nAwards, hidden,
                            spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);

                    if (previews.isEmpty()) {
                        previews.add(new Post.Preview(url, 0, 0));
                    }
                    post.setPreviews(previews);
                } else {
                    if (isVideo) {
                        //No preview video post
                        JSONObject redditVideoObject = data.getJSONObject(JSONUtils.MEDIA_KEY).getJSONObject(JSONUtils.REDDIT_VIDEO_KEY);
                        int postType = Post.VIDEO_TYPE;
                        String videoUrl = Html.fromHtml(redditVideoObject.getString(JSONUtils.HLS_URL_KEY)).toString();
                        String videoDownloadUrl = redditVideoObject.getString(JSONUtils.FALLBACK_URL_KEY);

                        post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, authorFlair,
                                authorFlairHTML, postTimeMillis, title, permalink, score, postType, voteType,
                                nComments, upvoteRatio, flair, awards, nAwards, hidden, spoiler, nsfw, stickied,
                                archived, locked, saved, isCrosspost);

                        post.setVideoUrl(videoUrl);
                        post.setVideoDownloadUrl(videoDownloadUrl);
                    } else {
                        //No preview link post
                        int postType = Post.NO_PREVIEW_LINK_TYPE;
                        post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                                authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                                postType, voteType, nComments, upvoteRatio, flair, awards, nAwards, hidden,
                                spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);
                        if (data.isNull(JSONUtils.SELFTEXT_KEY)) {
                            post.setSelfText("");
                        } else {
                            post.setSelfText(Utils.modifyMarkdown(data.getString(JSONUtils.SELFTEXT_KEY).trim()));
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
                    previews.add(new Post.Preview(previewUrl, previewWidth, previewHeight));

                    JSONArray thumbnailPreviews = images.getJSONArray(JSONUtils.RESOLUTIONS_KEY);
                    for (int i = 0; i < thumbnailPreviews.length(); i++) {
                        JSONObject thumbnailPreview = images.getJSONArray(JSONUtils.RESOLUTIONS_KEY).getJSONObject(i);
                        String thumbnailPreviewUrl = thumbnailPreview.getString(JSONUtils.URL_KEY);
                        int thumbnailPreviewWidth = thumbnailPreview.getInt(JSONUtils.WIDTH_KEY);
                        int thumbnailPreviewHeight = thumbnailPreview.getInt(JSONUtils.HEIGHT_KEY);

                        previews.add(new Post.Preview(thumbnailPreviewUrl, thumbnailPreviewWidth, thumbnailPreviewHeight));
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
                        nComments, upvoteRatio, flair, awards, nAwards, hidden, spoiler, nsfw, stickied,
                        archived, locked, saved, isCrosspost);

                post.setPreviews(previews);
                post.setVideoUrl(videoUrl);
                post.setVideoDownloadUrl(videoDownloadUrl);
            } else if (data.has(JSONUtils.PREVIEW_KEY)) {
                if (data.getJSONObject(JSONUtils.PREVIEW_KEY).has(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY)) {
                    //Gif video post (HLS)
                    int postType = Post.VIDEO_TYPE;
                    String videoUrl = Html.fromHtml(data.getJSONObject(JSONUtils.PREVIEW_KEY)
                            .getJSONObject(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY).getString(JSONUtils.HLS_URL_KEY)).toString();
                    String videoDownloadUrl = data.getJSONObject(JSONUtils.PREVIEW_KEY)
                            .getJSONObject(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY).getString(JSONUtils.FALLBACK_URL_KEY);

                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, authorFlair,
                            authorFlairHTML, postTimeMillis, title, permalink, score, postType, voteType,
                            nComments, upvoteRatio, flair, awards, nAwards, hidden, spoiler, nsfw, stickied,
                            archived, locked, saved, isCrosspost);
                    post.setPreviews(previews);
                    post.setVideoUrl(videoUrl);
                    post.setVideoDownloadUrl(videoDownloadUrl);
                } else {
                    if (url.endsWith("jpg") || url.endsWith("png")) {
                        //Image post
                        int postType = Post.IMAGE_TYPE;

                        post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                                authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                                postType, voteType, nComments, upvoteRatio, flair, awards, nAwards,
                                hidden, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);

                        if (previews.isEmpty()) {
                            previews.add(new Post.Preview(url, 0, 0));
                        }
                        post.setPreviews(previews);
                    } else if (url.endsWith("gif")){
                        //Gif post
                        int postType = Post.GIF_TYPE;
                        post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                                authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                                postType, voteType, nComments, upvoteRatio, flair, awards, nAwards,
                                hidden, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);

                        post.setPreviews(previews);
                        post.setVideoUrl(url);
                    } else if (url.endsWith("mp4")) {
                        //Video post
                        int postType = Post.VIDEO_TYPE;

                        post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                                authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                                postType, voteType, nComments, upvoteRatio, flair, awards, nAwards,
                                hidden, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);
                        post.setPreviews(previews);
                        post.setVideoUrl(url);
                        post.setVideoDownloadUrl(url);
                    } else {
                        if (url.contains(permalink)) {
                            //Text post but with a preview
                            int postType = Post.TEXT_TYPE;

                            post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                                    authorFlair, authorFlairHTML, postTimeMillis, title, permalink, score,
                                    postType, voteType, nComments, upvoteRatio, flair, awards, nAwards,
                                    hidden, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);

                            //Need attention
                            post.setPreviews(previews);

                            if (data.isNull(JSONUtils.SELFTEXT_KEY)) {
                                post.setSelfText("");
                            } else {
                                String selfText = Utils.modifyMarkdown(data.getString(JSONUtils.SELFTEXT_KEY).trim());
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
                        } else {
                            //Link post
                            int postType = Post.LINK_TYPE;

                            post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                                    authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                                    postType, voteType, nComments, upvoteRatio, flair, awards, nAwards,
                                    hidden, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);
                            if (data.isNull(JSONUtils.SELFTEXT_KEY)) {
                                post.setSelfText("");
                            } else {
                                post.setSelfText(Utils.modifyMarkdown(data.getString(JSONUtils.SELFTEXT_KEY).trim()));
                            }

                            post.setPreviews(previews);
                        }
                    }
                }
            } else {
                if (url.endsWith("jpg") || url.endsWith("png")) {
                    //Image post
                    int postType = Post.IMAGE_TYPE;

                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                            authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                            postType, voteType, nComments, upvoteRatio, flair, awards, nAwards, hidden,
                            spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);

                    if (previews.isEmpty()) {
                        previews.add(new Post.Preview(url, 0, 0));
                    }
                    post.setPreviews(previews);
                } else if (url.endsWith("mp4")) {
                    //Video post
                    int postType = Post.VIDEO_TYPE;

                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                            authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                            postType, voteType, nComments, upvoteRatio, flair, awards, nAwards, hidden,
                            spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);
                    post.setPreviews(previews);
                    post.setVideoUrl(url);
                    post.setVideoDownloadUrl(url);
                } else {
                    //CP No Preview Link post
                    int postType = Post.NO_PREVIEW_LINK_TYPE;

                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                            authorFlair, authorFlairHTML, postTimeMillis, title, url, permalink, score,
                            postType, voteType, nComments, upvoteRatio, flair, awards, nAwards, hidden,
                            spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);
                    //Need attention
                }
            }
        }

        if (post.getPostType() == Post.VIDEO_TYPE) {
            try {
                Uri uri = Uri.parse(url);
                String authority = uri.getAuthority();
                if (authority != null && (authority.contains("gfycat.com"))) {
                    post.setIsGfycat(true);
                    post.setVideoUrl(url);
                    String gfycatId = url.substring(url.lastIndexOf("/") + 1);
                    if (gfycatId.contains("-")) {
                        gfycatId = gfycatId.substring(0, gfycatId.indexOf('-'));
                    }
                    post.setGfycatId(gfycatId.toLowerCase());
                } else if (authority != null && authority.contains("redgifs.com")) {
                    String gfycatId = url.substring(url.lastIndexOf("/") + 1);
                    if (gfycatId.contains("-")) {
                        gfycatId = gfycatId.substring(0, gfycatId.indexOf('-'));
                    }
                    post.setIsRedgifs(true);
                    post.setVideoUrl(url);
                    post.setGfycatId(gfycatId.toLowerCase());
                }
            } catch (IllegalArgumentException ignore) { }
        } else if (post.getPostType() == Post.LINK_TYPE || post.getPostType() == Post.NO_PREVIEW_LINK_TYPE) {
            if (data.has(JSONUtils.GALLERY_DATA_KEY)) {
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
                    if ((previews.isEmpty()) && mimeType.contains("jpg") || mimeType.contains("png")) {
                        previews.add(new Post.Preview(galleryItemUrl, singleGalleryObject.getJSONObject(JSONUtils.S_KEY).getInt(JSONUtils.X_KEY),
                                singleGalleryObject.getJSONObject(JSONUtils.S_KEY).getInt(JSONUtils.Y_KEY)));
                    }
                    gallery.add(new Post.Gallery(mimeType, galleryItemUrl, subredditName + "-" + galleryId + "." + mimeType.substring(mimeType.lastIndexOf("/") + 1)));
                }

                if (!gallery.isEmpty()) {
                    post.setPostType(Post.GALLERY_TYPE);
                    post.setGallery(gallery);
                    post.setPreviews(previews);
                }
            } else if (post.getPostType() == Post.LINK_TYPE) {
                Uri uri = Uri.parse(url);
                String authority = uri.getAuthority();

                // Gyfcat ids must be lowercase to resolve to a video through the api, we are not
                // guaranteed to get an id that is all lowercase.
                String gfycatId = url.substring(url.lastIndexOf("/") + 1).toLowerCase();

                if (authority != null && (authority.contains("gfycat.com"))) {
                    post.setPostType(Post.VIDEO_TYPE);
                    post.setIsGfycat(true);
                    post.setVideoUrl(url);
                    post.setGfycatId(gfycatId);
                } else if (authority != null && authority.contains("redgifs.com")) {
                    post.setPostType(Post.VIDEO_TYPE);
                    post.setIsRedgifs(true);
                    post.setVideoUrl(url);
                    post.setGfycatId(gfycatId);
                }
            }
        }

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

    private static class ParsePostDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONArray allData;
        private int nPosts;
        private PostFilter postFilter;
        private List<ReadPost> readPostList;
        private ParsePostsListingListener parsePostsListingListener;
        private ParsePostListener parsePostListener;
        private LinkedHashSet<Post> newPosts;
        private Post post;
        private String lastItem;
        private boolean parseFailed;

        ParsePostDataAsyncTask(String response, int nPosts, PostFilter postFilter, List<ReadPost> readPostList,
                               ParsePostsListingListener parsePostsListingListener) {
            this.parsePostsListingListener = parsePostsListingListener;
            try {
                JSONObject jsonResponse = new JSONObject(response);
                allData = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                lastItem = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);
                this.nPosts = nPosts;
                this.postFilter = postFilter;
                this.readPostList = readPostList;
                newPosts = new LinkedHashSet<>();
                parseFailed = false;
            } catch (JSONException e) {
                e.printStackTrace();
                parseFailed = true;
            }
        }

        ParsePostDataAsyncTask(String response, PostFilter postFilter,
                               ParsePostListener parsePostListener) {
            this.parsePostListener = parsePostListener;
            try {
                allData = new JSONArray(response).getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                this.postFilter = postFilter;
                parseFailed = false;
            } catch (JSONException e) {
                e.printStackTrace();
                parseFailed = true;
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (parseFailed) {
                return null;
            }

            if (newPosts == null) {
                //Only one post
                if (allData.length() == 0) {
                    parseFailed = true;
                    return null;
                }

                try {
                    JSONObject data = allData.getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY);
                    post = parseBasicData(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                    parseFailed = true;
                }
            } else {
                //Posts listing
                int size;
                if (nPosts < 0 || nPosts > allData.length()) {
                    size = allData.length();
                } else {
                    size = nPosts;
                }

                HashSet<ReadPost> readPostHashSet = null;
                if (readPostList != null) {
                    readPostHashSet = new HashSet<>(readPostList);
                }
                for (int i = 0; i < size; i++) {
                    try {
                        if (allData.getJSONObject(i).getString(JSONUtils.KIND_KEY).equals("t3")) {
                            JSONObject data = allData.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
                            Post post = parseBasicData(data);
                            if (readPostHashSet != null && readPostHashSet.contains(ReadPost.convertPost(post))) {
                                post.markAsRead(false);
                            }
                            if (PostFilter.isPostAllowed(post, postFilter)) {
                                newPosts.add(post);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!parseFailed) {
                if (newPosts != null) {
                    parsePostsListingListener.onParsePostsListingSuccess(newPosts, lastItem);
                } else {
                    parsePostListener.onParsePostSuccess(post);
                }
            } else {
                if (parsePostsListingListener != null) {
                    parsePostsListingListener.onParsePostsListingFail();
                } else {
                    parsePostListener.onParsePostFail();
                }
            }
        }
    }

    public static class ParseRandomPostAsyncTask extends AsyncTask<Void, Void, Void> {

        private String response;
        private boolean isNSFW;
        private ParseRandomPostListener parseRandomPostListener;
        private String subredditName;
        private String postId;
        private boolean parseFailed = false;

        ParseRandomPostAsyncTask(String response, boolean isNSFW, ParseRandomPostListener parseRandomPostListener) {
            this.response = response;
            this.isNSFW = isNSFW;
            this.parseRandomPostListener = parseRandomPostListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONArray postsArray = new JSONObject(response).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                if (postsArray.length() == 0) {
                    parseFailed = true;
                } else {
                    JSONObject post = postsArray.getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY);
                    subredditName = post.getString(JSONUtils.SUBREDDIT_KEY);
                    if (isNSFW) {
                        postId = post.getString(JSONUtils.ID_KEY);
                    } else {
                        postId = post.getString(JSONUtils.LINK_ID_KEY).substring("t3_".length());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                parseFailed = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (parseFailed) {
                parseRandomPostListener.onParseRandomPostFailed();
            } else {
                parseRandomPostListener.onParseRandomPostSuccess(postId, subredditName);
            }
        }
    }
}

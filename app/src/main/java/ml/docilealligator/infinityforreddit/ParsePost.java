package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by alex on 3/21/18.
 */

class ParsePost {
    interface ParsePostsListingListener {
        void onParsePostsListingSuccess(ArrayList<Post> newPostData, String lastItem);
        void onParsePostsListingFail();
    }

    interface ParsePostListener {
        void onParsePostSuccess(Post post);
        void onParsePostFail();
    }

    static void parsePosts(String response, Locale locale, int nPosts, int filter, boolean nsfw,
                           ParsePostsListingListener parsePostsListingListener) {
        new ParsePostDataAsyncTask(response, locale, nPosts, filter, nsfw, parsePostsListingListener).execute();
    }

    static void parsePost(String response, Locale locale, ParsePostListener parsePostListener) {
        new ParsePostDataAsyncTask(response, locale, true, parsePostListener).execute();
    }

    private static class ParsePostDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONArray allData;
        private Locale locale;
        private int nPosts;
        private int filter;
        private boolean nsfw;
        private ParsePostsListingListener parsePostsListingListener;
        private ParsePostListener parsePostListener;
        private ArrayList<Post> newPosts;
        private Post post;
        private String lastItem;
        private boolean parseFailed;

        ParsePostDataAsyncTask(String response, Locale locale, int nPosts, int filter, boolean nsfw,
                               ParsePostsListingListener parsePostsListingListener) {
            this.parsePostsListingListener = parsePostsListingListener;
            try {
                JSONObject jsonResponse = new JSONObject(response);
                allData = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                lastItem = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);
                this.locale = locale;
                this.nPosts = nPosts;
                this.filter = filter;
                this.nsfw = nsfw;
                newPosts = new ArrayList<>();
                parseFailed = false;
            } catch (JSONException e) {
                e.printStackTrace();
                parseFailed = true;
            }
        }

        ParsePostDataAsyncTask(String response, Locale locale, boolean nsfw,
                               ParsePostListener parsePostListener) {
            this.parsePostListener = parsePostListener;
            try {
                allData = new JSONArray(response).getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                this.locale = locale;
                this.nsfw = nsfw;
                parseFailed = false;
            } catch (JSONException e) {
                e.printStackTrace();
                parseFailed = true;
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(parseFailed) {
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
                    post = parseBasicData(data, locale);
                } catch (JSONException e) {
                    Log.e("parsing post error", "message: " + e.getMessage());
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

                for (int i = 0; i < size; i++) {
                    try {
                        JSONObject data = allData.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
                        Post post = parseBasicData(data, locale);
                        if(!(!nsfw && post.isNSFW())) {
                            if (filter == PostFragment.EXTRA_NO_FILTER) {
                                newPosts.add(post);
                            } else if (filter == post.getPostType()) {
                                newPosts.add(post);
                            } else if (filter == Post.LINK_TYPE && post.getPostType() == Post.NO_PREVIEW_LINK_TYPE) {
                                newPosts.add(post);
                            } else if(filter == Post.NSFW_TYPE && post.isNSFW()) {
                                newPosts.add(post);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("parsing post error", "message: " + e.getMessage());
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!parseFailed) {
                if(newPosts != null) {
                    parsePostsListingListener.onParsePostsListingSuccess(newPosts, lastItem);
                } else {
                    parsePostListener.onParsePostSuccess(post);
                }
            } else {
                if(parsePostsListingListener != null) {
                    parsePostsListingListener.onParsePostsListingFail();
                } else {
                    parsePostListener.onParsePostFail();
                }
            }
        }
    }

    private static Post parseBasicData(JSONObject data, Locale locale) throws JSONException {
        String id = data.getString(JSONUtils.ID_KEY);
        String fullName = data.getString(JSONUtils.NAME_KEY);
        String subredditName = data.getString(JSONUtils.SUBREDDIT_KEY);
        String subredditNamePrefixed = data.getString(JSONUtils.SUBREDDIT_NAME_PREFIX_KEY);
        String author = data.getString(JSONUtils.AUTHOR_KEY);
        long postTime = data.getLong(JSONUtils.CREATED_UTC_KEY) * 1000;
        String title = data.getString(JSONUtils.TITLE_KEY);
        int score = data.getInt(JSONUtils.SCORE_KEY);
        int voteType;
        int gilded = data.getInt(JSONUtils.GILDED_KEY);
        boolean spoiler = data.getBoolean(JSONUtils.SPOILER_KEY);
        boolean nsfw = data.getBoolean(JSONUtils.NSFW_KEY);
        boolean stickied = data.getBoolean(JSONUtils.STICKIED_KEY);
        boolean archived = data.getBoolean(JSONUtils.ARCHIVED_KEY);
        boolean locked = data.getBoolean(JSONUtils.LOCKEC_KEY);
        boolean saved = data.getBoolean(JSONUtils.SAVED_KEY);
        String flair = null;
        if(!data.isNull(JSONUtils.LINK_FLAIR_TEXT_KEY)) {
            flair = data.getString(JSONUtils.LINK_FLAIR_TEXT_KEY);
        }

        if(data.isNull(JSONUtils.LIKES_KEY)) {
            voteType = 0;
        } else {
            voteType = data.getBoolean(JSONUtils.LIKES_KEY) ? 1 : -1;
            score -= voteType;
        }

        Calendar postTimeCalendar = Calendar.getInstance();
        postTimeCalendar.setTimeInMillis(postTime);
        String formattedPostTime = new SimpleDateFormat("MMM d, YYYY, HH:mm",
                locale).format(postTimeCalendar.getTime());
        String permalink = Html.fromHtml(data.getString(JSONUtils.PERMALINK_KEY)).toString();

        String previewUrl = "";
        int previewWidth = -1;
        int previewHeight = -1;
        if(data.has(JSONUtils.PREVIEW_KEY)) {
            previewUrl = data.getJSONObject(JSONUtils.PREVIEW_KEY).getJSONArray(JSONUtils.IMAGES_KEY).getJSONObject(0)
                    .getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY);
            previewWidth = data.getJSONObject(JSONUtils.PREVIEW_KEY).getJSONArray(JSONUtils.IMAGES_KEY).getJSONObject(0)
                    .getJSONObject(JSONUtils.SOURCE_KEY).getInt(JSONUtils.WIDTH_KEY);
            previewHeight = data.getJSONObject(JSONUtils.PREVIEW_KEY).getJSONArray(JSONUtils.IMAGES_KEY).getJSONObject(0)
                    .getJSONObject(JSONUtils.SOURCE_KEY).getInt(JSONUtils.HEIGHT_KEY);
        }
        if(data.has(JSONUtils.CROSSPOST_PARENT_LIST)) {
            //Cross post
            data = data.getJSONArray(JSONUtils.CROSSPOST_PARENT_LIST).getJSONObject(0);
            Post crosspostParent = parseBasicData(data, locale);
            Post post = parseData(data, permalink, id, fullName, subredditName, subredditNamePrefixed,
                    author, formattedPostTime, title, previewUrl, previewWidth, previewHeight,
                    score, voteType, gilded, flair, spoiler, nsfw, stickied, archived, locked, saved,
                    true);
            post.setCrosspostParentId(crosspostParent.getId());
            return post;
        } else {
            return parseData(data, permalink, id, fullName, subredditName, subredditNamePrefixed,
                    author, formattedPostTime, title, previewUrl, previewWidth, previewHeight,
                    score, voteType, gilded, flair, spoiler, nsfw, stickied, archived, locked, saved,
                    false);
        }
    }

    private static Post parseData(JSONObject data, String permalink, String id, String fullName,
                                  String subredditName, String subredditNamePrefixed, String author,
                                  String formattedPostTime, String title, String previewUrl, int previewWidth,
                                  int previewHeight, int score, int voteType, int gilded, String flair,
                                  boolean spoiler, boolean nsfw, boolean stickied, boolean archived,
                                  boolean locked, boolean saved, boolean isCrosspost) throws JSONException {
        Post post;

        boolean isVideo = data.getBoolean(JSONUtils.IS_VIDEO_KEY);
        String url = Html.fromHtml(data.getString(JSONUtils.URL_KEY)).toString();

        if(!data.has(JSONUtils.PREVIEW_KEY) && previewUrl.equals("")) {
            if(url.contains(permalink)) {
                //Text post
                int postType = Post.TEXT_TYPE;
                post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, formattedPostTime,
                        title, permalink, score, postType, voteType, gilded, flair, spoiler, nsfw,
                        stickied, archived, locked, saved, isCrosspost);
                if(data.isNull(JSONUtils.SELFTEXT_KEY)) {
                    post.setSelfText("");
                } else {
                    post.setSelfText(Utils.addSubredditAndUserLink(data.getString(JSONUtils.SELFTEXT_KEY).trim()));
                }
            } else {
                //No preview link post
                int postType = Post.NO_PREVIEW_LINK_TYPE;
                post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, formattedPostTime,
                        title, previewUrl, url, permalink, score, postType,
                        voteType, gilded, flair, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);
                if(data.isNull(JSONUtils.SELFTEXT_KEY)) {
                    post.setSelfText("");
                } else {
                    post.setSelfText(Utils.addSubredditAndUserLink(data.getString(JSONUtils.SELFTEXT_KEY).trim()));
                }
            }
        } else {
            if(previewUrl.equals("")) {
                previewUrl = Html.fromHtml(data.getJSONObject(JSONUtils.PREVIEW_KEY).getJSONArray(JSONUtils.IMAGES_KEY).getJSONObject(0)
                        .getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY)).toString();
            }

            if(isVideo) {
                //Video post
                JSONObject redditVideoObject = data.getJSONObject(JSONUtils.MEDIA_KEY).getJSONObject(JSONUtils.REDDIT_VIDEO_KEY);
                int postType = Post.VIDEO_TYPE;
                String videoUrl = Html.fromHtml(redditVideoObject.getString(JSONUtils.DASH_URL_KEY)).toString();

                post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, formattedPostTime,
                        title, previewUrl, permalink, score, postType, voteType,
                        gilded, flair, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost, true);

                post.setPreviewWidth(previewWidth);
                post.setPreviewHeight(previewHeight);
                post.setVideoUrl(videoUrl);
                post.setDownloadableGifOrVideo(false);
            } else if(data.has(JSONUtils.PREVIEW_KEY)){
                JSONObject variations = data.getJSONObject(JSONUtils.PREVIEW_KEY).getJSONArray(JSONUtils.IMAGES_KEY).getJSONObject(0);
                if (variations.has(JSONUtils.VARIANTS_KEY) && variations.getJSONObject(JSONUtils.VARIANTS_KEY).has(JSONUtils.MP4_KEY)) {
                    //Gif video post (MP4)
                    int postType = Post.GIF_VIDEO_TYPE;
                    String videoUrl = Html.fromHtml(variations.getJSONObject(JSONUtils.VARIANTS_KEY).getJSONObject(JSONUtils.MP4_KEY).getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY)).toString();
                    String gifDownloadUrl = Html.fromHtml(variations.getJSONObject(JSONUtils.VARIANTS_KEY).getJSONObject(JSONUtils.GIF_KEY).getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY)).toString();

                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, formattedPostTime, title,
                            previewUrl, permalink, score, postType, voteType,
                            gilded, flair, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost, false);
                    post.setPreviewWidth(previewWidth);
                    post.setPreviewHeight(previewHeight);
                    post.setVideoUrl(videoUrl);
                    post.setDownloadableGifOrVideo(true);
                    post.setGifOrVideoDownloadUrl(gifDownloadUrl);
                } else if(data.getJSONObject(JSONUtils.PREVIEW_KEY).has(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY)) {
                    //Gif video post (Dash)
                    int postType = Post.GIF_VIDEO_TYPE;
                    String videoUrl = Html.fromHtml(data.getJSONObject(JSONUtils.PREVIEW_KEY)
                            .getJSONObject(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY).getString(JSONUtils.DASH_URL_KEY)).toString();

                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, formattedPostTime, title,
                            previewUrl, permalink, score, postType, voteType,
                            gilded, flair, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost, true);
                    post.setPreviewWidth(previewWidth);
                    post.setPreviewHeight(previewHeight);
                    post.setVideoUrl(videoUrl);
                    post.setDownloadableGifOrVideo(false);
                } else {
                    if (url.endsWith("jpg") || url.endsWith("png")) {
                        //Image post
                        int postType = Post.IMAGE_TYPE;

                        post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, formattedPostTime,
                                title, url, url, permalink, score, postType,
                                voteType, gilded, flair, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);

                        post.setPreviewWidth(previewWidth);
                        post.setPreviewHeight(previewHeight);
                    } else {
                        if (url.contains(permalink)) {
                            //Text post but with a preview
                            int postType = Post.TEXT_TYPE;

                            post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, formattedPostTime,
                                    title, permalink, score, postType, voteType, gilded, flair, spoiler,
                                    nsfw, stickied, archived, locked, saved, isCrosspost);

                            post.setPreviewWidth(previewWidth);
                            post.setPreviewHeight(previewHeight);

                            if(data.isNull(JSONUtils.SELFTEXT_KEY)) {
                                post.setSelfText("");
                            } else {
                                post.setSelfText(Utils.addSubredditAndUserLink(data.getString(JSONUtils.SELFTEXT_KEY).trim()));
                            }
                        } else {
                            //Link post
                            int postType = Post.LINK_TYPE;

                            post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, formattedPostTime,
                                    title, previewUrl, url, permalink, score, postType, voteType, gilded,
                                    flair, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);
                            if(data.isNull(JSONUtils.SELFTEXT_KEY)) {
                                post.setSelfText("");
                            } else {
                                post.setSelfText(Utils.addSubredditAndUserLink(data.getString(JSONUtils.SELFTEXT_KEY).trim()));
                            }

                            post.setPreviewWidth(previewWidth);
                            post.setPreviewHeight(previewHeight);
                        }
                    }
                }
            } else {
                if (url.endsWith("jpg") || url.endsWith("png")) {
                    //Image post
                    int postType = Post.IMAGE_TYPE;

                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, formattedPostTime,
                            title, previewUrl, url, permalink, score, postType,
                            voteType, gilded, flair, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);
                    post.setPreviewWidth(previewWidth);
                    post.setPreviewHeight(previewHeight);
                } else {
                    //CP No Preview Link post
                    int postType = Post.NO_PREVIEW_LINK_TYPE;

                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, formattedPostTime, title,
                            url, url, permalink, score, postType, voteType,
                            gilded, flair, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);
                }
            }
        }

        return post;
    }
}

package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;
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

    static void parsePosts(String response, Locale locale, ParsePostsListingListener parsePostsListingListener) {
        new ParsePostDataAsyncTask(response, locale, parsePostsListingListener).execute();
    }

    static void parsePost(String response, Locale locale, ParsePostListener parsePostListener) {
        new ParsePostDataAsyncTask(response, locale, parsePostListener).execute();
    }

    private static class ParsePostDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONArray allData;
        private Locale locale;
        private ParsePostsListingListener parsePostsListingListener;
        private ParsePostListener parsePostListener;
        private ArrayList<Post> newPosts;
        private Post post;
        private String lastItem;
        private boolean parseFailed;

        ParsePostDataAsyncTask(String response, Locale locale,
                               ParsePostsListingListener parsePostsListingListener) {
            this.parsePostsListingListener = parsePostsListingListener;
            try {
                JSONObject jsonResponse = new JSONObject(response);
                allData = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                lastItem = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);
                this.locale = locale;
                newPosts = new ArrayList<>();
                parseFailed = false;
            } catch (JSONException e) {
                e.printStackTrace();
                parseFailed = true;
            }
        }

        ParsePostDataAsyncTask(String response, Locale locale,
                               ParsePostListener parsePostListener) {
            this.parsePostListener = parsePostListener;
            try {
                allData = new JSONArray(response).getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                this.locale = locale;
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

            try {
                if(newPosts == null) {
                    //Only one post
                    if(allData.length() == 0) {
                        parseFailed = true;
                        return null;
                    }

                    String kind = allData.getJSONObject(0).getString(JSONUtils.KIND_KEY);
                    if(kind.equals("t3")) {
                        //It's a post
                        JSONObject data = allData.getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY);
                        post = parseBasicData(data, locale, -1);
                    } else {
                        parseFailed = true;
                        return null;
                    }
                } else {
                    //Posts listing
                    for(int i = 0; i < allData.length(); i++) {
                        String kind = allData.getJSONObject(i).getString(JSONUtils.KIND_KEY);
                        if(kind.equals("t3")) {
                            //It's a post
                            JSONObject data = allData.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
                            newPosts.add(parseBasicData(data, locale, i));
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("parsing post error", e.getMessage());
                parseFailed = true;
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
                if(newPosts != null) {
                    parsePostsListingListener.onParsePostsListingFail();
                } else {
                    parsePostListener.onParsePostFail();
                }
            }
        }
    }

    private static Post parseBasicData(JSONObject data, Locale locale, int i) throws JSONException {
        String id = data.getString(JSONUtils.ID_KEY);
        String fullName = data.getString(JSONUtils.NAME_KEY);
        String subredditNamePrefixed = data.getString(JSONUtils.SUBREDDIT_NAME_PREFIX_KEY);
        String author = data.getString(JSONUtils.AUTHOR_KEY);
        long postTime = data.getLong(JSONUtils.CREATED_UTC_KEY) * 1000;
        String title = data.getString(JSONUtils.TITLE_KEY);
        int score = data.getInt(JSONUtils.SCORE_KEY);
        int voteType;
        int gilded = data.getInt(JSONUtils.GILDED_KEY);
        boolean nsfw = data.getBoolean(JSONUtils.NSFW_KEY);
        boolean stickied = data.getBoolean(JSONUtils.STICKIED_KEY);

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
        String permalink = data.getString(JSONUtils.PERMALINK_KEY);

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
            return parseData(data, permalink, id, fullName, subredditNamePrefixed,
                    author, formattedPostTime, title, previewUrl, previewWidth, previewHeight,
                    score, voteType, gilded, nsfw, stickied, true, i);
        } else {
            return parseData(data, permalink, id, fullName, subredditNamePrefixed,
                    author, formattedPostTime, title, previewUrl, previewWidth, previewHeight,
                    score, voteType, gilded, nsfw, stickied, false, i);
        }
    }

    private static Post parseData(JSONObject data, String permalink, String id, String fullName,
                                  String subredditNamePrefixed, String author, String formattedPostTime,
                                  String title, String previewUrl, int previewWidth, int previewHeight,
                                  int score, int voteType, int gilded, boolean nsfw, boolean stickied,
                                  boolean isCrosspost, int i) throws JSONException {
        Post post;

        boolean isVideo = data.getBoolean(JSONUtils.IS_VIDEO_KEY);
        String url = data.getString(JSONUtils.URL_KEY);

        if(!data.has(JSONUtils.PREVIEW_KEY) && previewUrl.equals("")) {
            if(url.contains(permalink)) {
                //Text post
                Log.i("text", Integer.toString(i));
                int postType = Post.TEXT_TYPE;
                post = new Post(id, fullName, subredditNamePrefixed, author, formattedPostTime,
                        title, permalink, score, postType, voteType, gilded, nsfw, stickied, isCrosspost);
                if(data.isNull(JSONUtils.SELFTEXT_HTML_KEY)) {
                    post.setSelfText("");
                } else {
                    post.setSelfText(data.getString(JSONUtils.SELFTEXT_HTML_KEY).trim());
                }
            } else {
                //No preview link post
                Log.i("no preview link", Integer.toString(i));
                int postType = Post.NO_PREVIEW_LINK_TYPE;
                post = new Post(id, fullName, subredditNamePrefixed, author, formattedPostTime,
                        title, previewUrl, url, permalink, score, postType,
                        voteType, gilded, nsfw, stickied, isCrosspost);
                if(data.isNull(JSONUtils.SELFTEXT_HTML_KEY)) {
                    post.setSelfText("");
                } else {
                    post.setSelfText(data.getString(JSONUtils.SELFTEXT_HTML_KEY).trim());
                }
            }
        } else {
            if(previewUrl.equals("")) {
                previewUrl = data.getJSONObject(JSONUtils.PREVIEW_KEY).getJSONArray(JSONUtils.IMAGES_KEY).getJSONObject(0)
                        .getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY);
            }

            if(isVideo) {
                //Video post
                Log.i("video", Integer.toString(i));
                JSONObject redditVideoObject = data.getJSONObject(JSONUtils.MEDIA_KEY).getJSONObject(JSONUtils.REDDIT_VIDEO_KEY);
                int postType = Post.VIDEO_TYPE;
                String videoUrl = redditVideoObject.getString(JSONUtils.DASH_URL_KEY);

                post = new Post(id, fullName, subredditNamePrefixed, author, formattedPostTime,
                        title, previewUrl, permalink, score, postType, voteType,
                        gilded, nsfw, stickied, isCrosspost, true);

                post.setPreviewWidth(previewWidth);
                post.setPreviewHeight(previewHeight);
                post.setVideoUrl(videoUrl);
                post.setDownloadableGifOrVideo(false);
            } else if(data.has(JSONUtils.PREVIEW_KEY)){
                JSONObject variations = data.getJSONObject(JSONUtils.PREVIEW_KEY).getJSONArray(JSONUtils.IMAGES_KEY).getJSONObject(0);
                if (variations.has(JSONUtils.VARIANTS_KEY) && variations.getJSONObject(JSONUtils.VARIANTS_KEY).has(JSONUtils.MP4_KEY)) {
                    //Gif video post (MP4)
                    Log.i("gif video mp4", Integer.toString(i));
                    int postType = Post.GIF_VIDEO_TYPE;
                    String videoUrl = variations.getJSONObject(JSONUtils.VARIANTS_KEY).getJSONObject(JSONUtils.MP4_KEY).getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY);
                    String gifDownloadUrl = variations.getJSONObject(JSONUtils.VARIANTS_KEY).getJSONObject(JSONUtils.GIF_KEY).getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY);

                    post = new Post(id, fullName, subredditNamePrefixed, author, formattedPostTime, title,
                            previewUrl, permalink, score, postType, voteType,
                            gilded, nsfw, stickied, isCrosspost, false);
                    post.setPreviewWidth(previewWidth);
                    post.setPreviewHeight(previewHeight);
                    post.setVideoUrl(videoUrl);
                    post.setDownloadableGifOrVideo(true);
                    post.setGifOrVideoDownloadUrl(gifDownloadUrl);
                } else if(data.getJSONObject(JSONUtils.PREVIEW_KEY).has(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY)) {
                    //Gif video post (Dash)
                    Log.i("gif video dash", Integer.toString(i));
                    int postType = Post.GIF_VIDEO_TYPE;
                    String videoUrl = data.getJSONObject(JSONUtils.PREVIEW_KEY)
                            .getJSONObject(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY).getString(JSONUtils.DASH_URL_KEY);

                    post = new Post(id, fullName, subredditNamePrefixed, author, formattedPostTime, title,
                            previewUrl, permalink, score, postType, voteType,
                            gilded, nsfw, stickied, isCrosspost, true);
                    post.setPreviewWidth(previewWidth);
                    post.setPreviewHeight(previewHeight);
                    post.setVideoUrl(videoUrl);
                    post.setDownloadableGifOrVideo(false);
                } else {
                    if (url.endsWith("jpg") || url.endsWith("png")) {
                        //Image post
                        Log.i("image", Integer.toString(i));
                        int postType = Post.IMAGE_TYPE;

                        post = new Post(id, fullName, subredditNamePrefixed, author, formattedPostTime,
                                title, url, url, permalink, score, postType,
                                voteType, gilded, nsfw, stickied, isCrosspost);

                        post.setPreviewWidth(previewWidth);
                        post.setPreviewHeight(previewHeight);
                    } else {
                        if (url.contains(permalink)) {
                            //Text post but with a preview
                            Log.i("text with image", Integer.toString(i));
                            int postType = Post.TEXT_TYPE;

                            post = new Post(id, fullName, subredditNamePrefixed, author, formattedPostTime,
                                    title, permalink, score, postType, voteType, gilded, nsfw, stickied, isCrosspost);

                            post.setPreviewWidth(previewWidth);
                            post.setPreviewHeight(previewHeight);

                            if(data.isNull(JSONUtils.SELFTEXT_HTML_KEY)) {
                                post.setSelfText("");
                            } else {
                                post.setSelfText(data.getString(JSONUtils.SELFTEXT_HTML_KEY).trim());
                            }
                        } else {
                            //Link post
                            Log.i("link", Integer.toString(i));
                            int postType = Post.LINK_TYPE;

                            post = new Post(id, fullName, subredditNamePrefixed, author, formattedPostTime,
                                    title, previewUrl, url, permalink, score,
                                    postType, voteType, gilded, nsfw, stickied, isCrosspost);
                            if(data.isNull(JSONUtils.SELFTEXT_HTML_KEY)) {
                                post.setSelfText("");
                            } else {
                                post.setSelfText(data.getString(JSONUtils.SELFTEXT_HTML_KEY).trim());
                            }

                            post.setPreviewWidth(previewWidth);
                            post.setPreviewHeight(previewHeight);
                        }
                    }
                }
            } else {
                if (url.endsWith("jpg") || url.endsWith("png")) {
                    //Image post
                    Log.i("CP image", Integer.toString(i));
                    int postType = Post.IMAGE_TYPE;

                    post = new Post(id, fullName, subredditNamePrefixed, author, formattedPostTime,
                            title, previewUrl, url, permalink, score, postType,
                            voteType, gilded, nsfw, stickied, isCrosspost);
                    post.setPreviewWidth(previewWidth);
                    post.setPreviewHeight(previewHeight);
                } else {
                    //CP No Preview Link post
                    Log.i("CP no preview link", Integer.toString(i));
                    int postType = Post.NO_PREVIEW_LINK_TYPE;

                    post = new Post(id, fullName, subredditNamePrefixed, author, formattedPostTime, title,
                            url, url, permalink, score, postType, voteType,
                            gilded, nsfw, stickied, isCrosspost);
                }
            }
        }

        return post;
    }
}

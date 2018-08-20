package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by alex on 3/21/18.
 */

class ParsePost {

    interface ParsePostListener {
        void onParsePostSuccess(ArrayList<PostData> postData, String lastItem);
        void onParsePostFail();
    }

    private Context mContext;
    private ParsePostListener mParseBetPostListener;

    ParsePost(Context context, ParsePostListener parsePostListener) {
        mContext = context;
        mParseBetPostListener = parsePostListener;
    }

    void parsePost(String response, ArrayList<PostData> postData) {
        new ParsePostDataAsyncTask(response, postData).execute();
    }

    private class ParsePostDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONObject jsonResponse;
        private ArrayList<PostData> postData;
        private ArrayList<PostData> newPostData;
        private String lastItem;
        private boolean parseFailed;

        ParsePostDataAsyncTask(String response, ArrayList<PostData> postData) {
            try {
                jsonResponse = new JSONObject(response);
                this.postData = postData;
                newPostData = new ArrayList<>();
                parseFailed = false;
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(mContext, "Error converting response to JSON", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONArray allData = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);

                lastItem = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);
                for(int i = 0; i < allData.length(); i++) {
                    JSONObject data = allData.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
                    String id = data.getString(JSONUtils.ID_KEY);
                    String fullName = data.getString(JSONUtils.NAME_KEY);
                    String subredditName = data.getString(JSONUtils.SUBREDDIT_NAME_PREFIX_KEY);
                    long postTime = data.getLong(JSONUtils.CREATED_UTC_KEY) * 1000;
                    String title = data.getString(JSONUtils.TITLE_KEY);
                    int score = data.getInt(JSONUtils.SCORE_KEY);
                    int voteType;
                    boolean nsfw = data.getBoolean(JSONUtils.NSFW_KEY);

                    if(data.isNull(JSONUtils.LIKES_KEY)) {
                        voteType = 0;
                    } else {
                        voteType = data.getBoolean(JSONUtils.LIKES_KEY) ? 1 : -1;
                    }
                    Calendar postTimeCalendar = Calendar.getInstance();
                    postTimeCalendar.setTimeInMillis(postTime);
                    String formattedPostTime = new SimpleDateFormat("MMM d, YYYY, HH:mm",
                            mContext.getResources().getConfiguration().locale).format(postTimeCalendar.getTime());
                    String permalink = data.getString(JSONUtils.PERMALINK_KEY);

                    String previewUrl = "";
                    if(data.has(JSONUtils.PREVIEW_KEY)) {
                        previewUrl = data.getJSONObject(JSONUtils.PREVIEW_KEY).getJSONArray(JSONUtils.IMAGES_KEY).getJSONObject(0)
                                .getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY);
                    }

                    if(data.has(JSONUtils.CROSSPOST_PARENT_LIST)) {
                        //Cross post
                        data = data.getJSONArray(JSONUtils.CROSSPOST_PARENT_LIST).getJSONObject(0);
                        parseData(data, permalink, newPostData, id, fullName, subredditName,
                                formattedPostTime, title, previewUrl, score, voteType, nsfw, i);
                    } else {
                        parseData(data, permalink, newPostData, id, fullName, subredditName,
                                formattedPostTime, title, previewUrl, score, voteType, nsfw, i);
                    }
                }
            } catch (JSONException e) {
                Log.e("error", e.getMessage());
                Log.i("Best post", "Error parsing data");
                parseFailed = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!parseFailed) {
                postData.addAll(newPostData);
                mParseBetPostListener.onParsePostSuccess(postData, lastItem);
            } else {
                mParseBetPostListener.onParsePostFail();
            }
        }
    }

    private void parseData(JSONObject data, String permalink, ArrayList<PostData> bestPostData,
                           String id, String fullName, String subredditName, String formattedPostTime, String title,
                           String previewUrl, int score, int voteType, boolean nsfw, int i) throws JSONException {
        boolean isVideo = data.getBoolean(JSONUtils.IS_VIDEO_KEY);
        String url = data.getString(JSONUtils.URL_KEY);

        if(!data.has(JSONUtils.PREVIEW_KEY) && previewUrl.equals("")) {
            if(url.contains(permalink)) {
                //Text post
                Log.i("text", Integer.toString(i));
                int postType = PostData.TEXT_TYPE;
                PostData postData = new PostData(id, fullName, subredditName, formattedPostTime, title, permalink, score, postType, voteType, nsfw);
                if(data.isNull(JSONUtils.SELFTEXT_HTML_KEY)) {
                    postData.setSelfText("");
                } else {
                    postData.setSelfText(data.getString(JSONUtils.SELFTEXT_HTML_KEY).trim());
                }
                postData.setSelfText(data.getString(JSONUtils.SELFTEXT_HTML_KEY).trim());
                bestPostData.add(postData);
            } else {
                //No preview link post
                Log.i("no preview link", Integer.toString(i));
                int postType = PostData.NO_PREVIEW_LINK_TYPE;
                PostData linkPostData = new PostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, url, permalink, score, postType, voteType, nsfw);
                bestPostData.add(linkPostData);
            }
        } else if(isVideo) {
            //Video post
            Log.i("video", Integer.toString(i));
            JSONObject redditVideoObject = data.getJSONObject(JSONUtils.MEDIA_KEY).getJSONObject(JSONUtils.REDDIT_VIDEO_KEY);
            int postType = PostData.VIDEO_TYPE;
            String videoUrl = redditVideoObject.getString(JSONUtils.DASH_URL_KEY);

            PostData videoPostData = new PostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, permalink, score, postType, voteType, nsfw, true);

            videoPostData.setVideoUrl(videoUrl);
            videoPostData.setDownloadableGifOrVideo(false);

            bestPostData.add(videoPostData);
        } else if(data.has(JSONUtils.PREVIEW_KEY)){
            JSONObject variations = data.getJSONObject(JSONUtils.PREVIEW_KEY).getJSONArray(JSONUtils.IMAGES_KEY).getJSONObject(0);
            if (variations.has(JSONUtils.VARIANTS_KEY) && variations.getJSONObject(JSONUtils.VARIANTS_KEY).has(JSONUtils.MP4_KEY)) {
                //Gif video post (MP4)
                Log.i("gif video mp4", Integer.toString(i));
                int postType = PostData.GIF_VIDEO_TYPE;
                String videoUrl = variations.getJSONObject(JSONUtils.VARIANTS_KEY).getJSONObject(JSONUtils.MP4_KEY).getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY);
                String gifDownloadUrl = variations.getJSONObject(JSONUtils.VARIANTS_KEY).getJSONObject(JSONUtils.GIF_KEY).getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY);
                PostData post = new PostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, permalink, score, postType, voteType, nsfw, false);

                post.setVideoUrl(videoUrl);
                post.setDownloadableGifOrVideo(true);
                post.setGifOrVideoDownloadUrl(gifDownloadUrl);

                bestPostData.add(post);
            } else if(data.getJSONObject(JSONUtils.PREVIEW_KEY).has(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY)) {
                //Gif video post (Dash)
                Log.i("gif video dash", Integer.toString(i));
                int postType = PostData.GIF_VIDEO_TYPE;
                String videoUrl = data.getJSONObject(JSONUtils.PREVIEW_KEY)
                        .getJSONObject(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY).getString(JSONUtils.DASH_URL_KEY);

                PostData post = new PostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, permalink, score, postType, voteType, nsfw, true);

                post.setVideoUrl(videoUrl);
                post.setDownloadableGifOrVideo(false);

                bestPostData.add(post);
            } else {
                if (url.endsWith("jpg") || url.endsWith("png")) {
                    //Image post
                    Log.i("image", Integer.toString(i));
                    int postType = PostData.IMAGE_TYPE;
                    bestPostData.add(new PostData(id, fullName, subredditName, formattedPostTime, title, url, url, permalink, score, postType, voteType, nsfw));
                } else {
                    //Link post
                    Log.i("link", Integer.toString(i));
                    int postType = PostData.LINK_TYPE;
                    PostData linkPostData = new PostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, url, permalink, score, postType, voteType, nsfw);
                    bestPostData.add(linkPostData);
                }
            }
        } else {
            if (url.endsWith("jpg") || url.endsWith("png")) {
                //Image post
                Log.i("CP no preview image", Integer.toString(i));
                int postType = PostData.IMAGE_TYPE;
                bestPostData.add(new PostData(id, fullName, subredditName, formattedPostTime, title, url, url, permalink, score, postType, voteType, nsfw));
            } else {
                //Link post
                Log.i("CP no preview link", Integer.toString(i));
                int postType = PostData.LINK_TYPE;
                PostData linkPostData = new PostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, url, permalink, score, postType, voteType, nsfw);
                bestPostData.add(linkPostData);
            }
        }
    }
}

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

class ParseBestPost {

    interface ParseBestPostListener {
        void onParseBestPostSuccess(ArrayList<BestPostData> bestPostData, String lastItem);
        void onParseBestPostFail();
    }

    private Context mContext;
    private ParseBestPostListener mParseBetPostListener;

    ParseBestPost(Context context, ParseBestPostListener parseBestPostListener) {
        mContext = context;
        mParseBetPostListener = parseBestPostListener;
    }

    void parseBestPost(String response, ArrayList<BestPostData> bestPostData) {
        new ParseBestPostDataAsyncTask(response, bestPostData).execute();
    }

    private class ParseBestPostDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONObject jsonResponse;
        private ArrayList<BestPostData> bestPostData;
        private ArrayList<BestPostData> newBestPostData;
        private String lastItem;
        private boolean parseFailed;

        ParseBestPostDataAsyncTask(String response, ArrayList<BestPostData> bestPostData) {
            try {
                jsonResponse = new JSONObject(response);
                this.bestPostData = bestPostData;
                newBestPostData = new ArrayList<>();
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
                if(bestPostData == null) {
                    bestPostData = new ArrayList<>();
                }

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
                        parseData(data, permalink, newBestPostData, id, fullName, subredditName,
                                formattedPostTime, title, previewUrl, score, voteType, nsfw, i);
                    } else {
                        parseData(data, permalink, newBestPostData, id, fullName, subredditName,
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
                bestPostData.addAll(newBestPostData);
                mParseBetPostListener.onParseBestPostSuccess(bestPostData, lastItem);
            } else {
                mParseBetPostListener.onParseBestPostFail();
            }
        }
    }

    private void parseData(JSONObject data, String permalink, ArrayList<BestPostData> bestPostData,
                           String id, String fullName, String subredditName, String formattedPostTime, String title,
                           String previewUrl, int score, int voteType, boolean nsfw, int i) throws JSONException {
        boolean isVideo = data.getBoolean(JSONUtils.IS_VIDEO_KEY);
        String url = data.getString(JSONUtils.URL_KEY);

        if(!data.has(JSONUtils.PREVIEW_KEY) && previewUrl.equals("")) {
            if(url.contains(permalink)) {
                //Text post
                Log.i("text", Integer.toString(i));
                int postType = BestPostData.TEXT_TYPE;
                BestPostData postData = new BestPostData(id, fullName, subredditName, formattedPostTime, title, permalink, score, postType, voteType, nsfw);
                postData.setSelfText(data.getString(JSONUtils.SELF_TEXT_KEY).trim());
                bestPostData.add(postData);
            } else {
                //No preview link post
                Log.i("no preview link", Integer.toString(i));
                int postType = BestPostData.NO_PREVIEW_LINK_TYPE;
                BestPostData linkPostData = new BestPostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, url, permalink, score, postType, voteType, nsfw);
                bestPostData.add(linkPostData);
            }
        } else if(isVideo) {
            //Video post
            Log.i("video", Integer.toString(i));
            JSONObject redditVideoObject = data.getJSONObject(JSONUtils.MEDIA_KEY).getJSONObject(JSONUtils.REDDIT_VIDEO_KEY);
            int postType = BestPostData.VIDEO_TYPE;
            String videoUrl = redditVideoObject.getString(JSONUtils.DASH_URL_KEY);

            BestPostData videoPostData = new BestPostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, permalink, score, postType, voteType, nsfw, true);

            videoPostData.setVideoUrl(videoUrl);
            videoPostData.setDownloadableGifOrVideo(false);

            bestPostData.add(videoPostData);
        } else {
            JSONObject variations = data.getJSONObject(JSONUtils.PREVIEW_KEY).getJSONArray(JSONUtils.IMAGES_KEY).getJSONObject(0);
            if (variations.has(JSONUtils.VARIANTS_KEY) && variations.getJSONObject(JSONUtils.VARIANTS_KEY).has(JSONUtils.MP4_KEY)) {
                //Gif video post (MP4)
                Log.i("gif video mp4", Integer.toString(i));
                int postType = BestPostData.GIF_VIDEO_TYPE;
                String videoUrl = variations.getJSONObject(JSONUtils.VARIANTS_KEY).getJSONObject(JSONUtils.MP4_KEY).getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY);
                String gifDownloadUrl = variations.getJSONObject(JSONUtils.VARIANTS_KEY).getJSONObject(JSONUtils.GIF_KEY).getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY);
                BestPostData post = new BestPostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, permalink, score, postType, voteType, nsfw, false);

                post.setVideoUrl(videoUrl);
                post.setDownloadableGifOrVideo(true);
                post.setGifOrVideoDownloadUrl(gifDownloadUrl);

                bestPostData.add(post);
            } else if(data.getJSONObject(JSONUtils.PREVIEW_KEY).has(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY)) {
                //Gif video post (Dash)
                Log.i("gif video dash", Integer.toString(i));
                int postType = BestPostData.GIF_VIDEO_TYPE;
                String videoUrl = data.getJSONObject(JSONUtils.PREVIEW_KEY)
                        .getJSONObject(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY).getString(JSONUtils.DASH_URL_KEY);

                BestPostData post = new BestPostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, permalink, score, postType, voteType, nsfw, true);

                post.setVideoUrl(videoUrl);
                post.setDownloadableGifOrVideo(false);

                bestPostData.add(post);
            } else {
                if (url.endsWith("jpg") || url.endsWith("png")) {
                    //Image post
                    Log.i("image", Integer.toString(i));
                    int postType = BestPostData.IMAGE_TYPE;
                    bestPostData.add(new BestPostData(id, fullName, subredditName, formattedPostTime, title, url, url, permalink, score, postType, voteType, nsfw));
                } else {
                    //Link post
                    Log.i("link", Integer.toString(i));
                    int postType = BestPostData.LINK_TYPE;
                    BestPostData linkPostData = new BestPostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, url, permalink, score, postType, voteType, nsfw);
                    bestPostData.add(linkPostData);
                }
            }
        }
    }

    /*private void parseData(JSONObject data, String permalink, ArrayList<BestPostData> bestPostData,
                               String id, String fullName, String subredditName, String formattedPostTime, String title,
                               int score, int voteType, boolean nsfw, int i) throws JSONException {
        boolean isVideo = data.getBoolean(JSONUtils.IS_VIDEO_KEY);
        if(!data.has(JSONUtils.PREVIEW_KEY)) {
            String url = data.getString(JSONUtils.URL_KEY);
            if(url.contains(permalink)) {
                //Text post
                Log.i("text", Integer.toString(i));
                int postType = BestPostData.TEXT_TYPE;
                BestPostData postData = new BestPostData(id, fullName, subredditName, formattedPostTime, title, permalink, score, postType, voteType, nsfw);
                postData.setSelfText(data.getString(JSONUtils.SELF_TEXT_KEY).trim());
                bestPostData.add(postData);
            } else {
                //No preview link post
                Log.i("no preview link", Integer.toString(i));
                int postType = BestPostData.NO_PREVIEW_LINK_TYPE;
                BestPostData post = new BestPostData(id, fullName, subredditName, formattedPostTime, title, permalink, score, postType, voteType, nsfw);
                post.setLinkUrl(url);
                bestPostData.add(post);
            }
        } else if (!isVideo) {
            JSONObject variations = data.getJSONObject(JSONUtils.PREVIEW_KEY).getJSONArray(JSONUtils.IMAGES_KEY).getJSONObject(0);
            String previewUrl = variations.getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY);
            if (variations.has(JSONUtils.VARIANTS_KEY)) {
                if (variations.getJSONObject(JSONUtils.VARIANTS_KEY).has(JSONUtils.MP4_KEY)) {
                    //Gif video
                    Log.i("gif video", Integer.toString(i));
                    int postType = BestPostData.GIF_VIDEO_TYPE;
                    String videoUrl = variations.getJSONObject(JSONUtils.VARIANTS_KEY).getJSONObject(JSONUtils.MP4_KEY).getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY);

                    BestPostData post = new BestPostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, permalink, score, postType, voteType, nsfw);
                    post.setVideoUrl(videoUrl);
                    bestPostData.add(post);
                } else if (variations.getJSONObject(JSONUtils.VARIANTS_KEY).has(JSONUtils.GIF_KEY)) {
                    //Gif post
                    Log.i("gif", Integer.toString(i));
                    int postType = BestPostData.GIF_TYPE;
                    String gifUrl = variations.getJSONObject(JSONUtils.VARIANTS_KEY).getJSONObject(JSONUtils.GIF_KEY).getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY);

                    BestPostData post = new BestPostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, permalink, score, postType, voteType, nsfw);
                    post.setGifUrl(gifUrl);
                    bestPostData.add(post);
                } else {
                    if(data.getJSONObject(JSONUtils.PREVIEW_KEY).has(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY)) {
                        //Gif link post
                        Log.i("gif link", Integer.toString(i));
                        int postType = BestPostData.LINK_TYPE;
                        String gifUrl = data.getString(JSONUtils.URL_KEY);
                        BestPostData gifLinkPostData = new BestPostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, permalink, score, postType, voteType, nsfw);
                        gifLinkPostData.setLinkUrl(gifUrl);
                        bestPostData.add(gifLinkPostData);
                    } else {
                        if(!data.isNull(JSONUtils.MEDIA_KEY)) {
                            //Video link post
                            Log.i("video link", Integer.toString(i));
                            int postType = BestPostData.LINK_TYPE;
                            String videoUrl = data.getString(JSONUtils.URL_KEY);
                            BestPostData videoLinkPostData = new BestPostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, permalink, score, postType, voteType, nsfw);
                            videoLinkPostData.setLinkUrl(videoUrl);
                            bestPostData.add(videoLinkPostData);
                        } else {
                            if(data.getBoolean(JSONUtils.IS_REDDIT_MEDIA_DOMAIN)) {
                                //Image post
                                Log.i("image", Integer.toString(i));
                                int postType = BestPostData.IMAGE_TYPE;
                                bestPostData.add(new BestPostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, permalink, score, postType, voteType, nsfw));
                            } else {
                                //Link post
                                Log.i("link", Integer.toString(i));
                                int postType = BestPostData.LINK_TYPE;
                                String linkUrl = data.getString(JSONUtils.URL_KEY);
                                BestPostData linkPostData = new BestPostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, permalink, score, postType, voteType, nsfw);
                                linkPostData.setLinkUrl(linkUrl);
                                bestPostData.add(linkPostData);
                            }
                        }
                    }
                }
            } else {
                //Image post
                Toast.makeText(mContext, "Fixed post" + Integer.toString(i), Toast.LENGTH_SHORT).show();
                Log.i("fixed image", Integer.toString(i));
                int postType = BestPostData.IMAGE_TYPE;
                bestPostData.add(new BestPostData(id, fullName, subredditName, formattedPostTime, title, previewUrl, permalink, score, postType, voteType, nsfw));
            }
        } else {
            //Video post
            Log.i("video", Integer.toString(i));
            JSONObject redditVideoObject = data.getJSONObject(JSONUtils.MEDIA_KEY).getJSONObject(JSONUtils.REDDIT_VIDEO_KEY);
            int postType = BestPostData.VIDEO_TYPE;
            String videoUrl = redditVideoObject.getString(JSONUtils.DASH_URL_KEY);

            String videoPreviewUrl = data.getJSONObject(JSONUtils.PREVIEW_KEY).getJSONArray(JSONUtils.IMAGES_KEY).getJSONObject(0).getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY);

            BestPostData videoPostData = new BestPostData(id, fullName, subredditName, formattedPostTime, title, videoPreviewUrl, permalink, score, postType, voteType, nsfw);
            videoPostData.setVideoUrl(videoUrl);

            bestPostData.add(videoPostData);
        }
    }*/
}

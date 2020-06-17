package ml.docilealligator.infinityforreddit;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import ml.docilealligator.infinityforreddit.API.PushshiftAPI;
import ml.docilealligator.infinityforreddit.Post.Post;
import ml.docilealligator.infinityforreddit.Utils.JSONUtils;
import ml.docilealligator.infinityforreddit.Utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchRemovedPost {

    public static void fetchRemovedPost(Retrofit retrofit, Post post, FetchRemovedPostListener listener) {
        retrofit.create(PushshiftAPI.class).getRemovedPost(post.getId())
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            new ParsePostAsyncTask(response.body(), post, listener).execute();
                        } else {
                            listener.fetchFailed();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        listener.fetchFailed();
                    }
                });
    }

    private static Post parseRemovedPost(JSONObject postJson, Post post) throws JSONException {
        String id = postJson.getString(JSONUtils.ID_KEY);
        if (id.equals(post.getId())) {
            String author = postJson.getString(JSONUtils.AUTHOR_KEY);
            String title = postJson.getString(JSONUtils.TITLE_KEY);
            String postContent = Utils.modifyMarkdown(postJson.getString(JSONUtils.SELFTEXT_KEY).trim());

            post.setAuthor(author);
            post.setTitle(title);
            post.setSelfText(postContent);
            post.setSelfTextPlain("");
            post.setSelfTextPlainTrimmed("");

            String url = postJson.optString(JSONUtils.URL_KEY);

            if (url.endsWith("gif") || url.endsWith("mp4")) {
                post.setVideoUrl(url);
                post.setVideoDownloadUrl(url);
            } else if (post.getPostType() == Post.VIDEO_TYPE || post.getPostType() == Post.GIF_TYPE) {
                JSONObject redditVideoObject = postJson.getJSONObject("secure_media").getJSONObject(JSONUtils.REDDIT_VIDEO_KEY);
                String videoUrl = Html.fromHtml(redditVideoObject.getString(JSONUtils.HLS_URL_KEY)).toString();
                String videoDownloadUrl = redditVideoObject.getString(JSONUtils.FALLBACK_URL_KEY);

                post.setVideoUrl(videoUrl);
                post.setVideoDownloadUrl(videoDownloadUrl);
            } else if (post.getPostType() == Post.LINK_TYPE) {
                post.setUrl(url);
            }

            if (post.getPostType() == Post.VIDEO_TYPE) {
                try {
                    Uri uri = Uri.parse(url);
                    String authority = uri.getAuthority();
                    if (authority != null && (authority.contains("gfycat.com") || authority.contains("redgifs.com"))) {
                        post.setPostType(Post.LINK_TYPE);
                        post.setUrl(url);
                    }
                } catch (IllegalArgumentException ignore) {
                }
            }

            if (!postJson.isNull("thumbnail")) {
                post.setThumbnailPreviewUrl(postJson.getString("thumbnail"));
            }

            return post;
        } else {
            return null;
        }
    }

    public interface FetchRemovedPostListener {
        void fetchSuccess(Post post);

        void fetchFailed();
    }

    private static class ParsePostAsyncTask extends AsyncTask<Void, Void, Void> {

        private String responseBody;
        private FetchRemovedPostListener listener;
        Post post;

        public ParsePostAsyncTask(String responseBody, Post post, FetchRemovedPostListener listener) {
            this.responseBody = responseBody;
            this.post = post;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject postJson = new JSONObject(responseBody).getJSONArray(JSONUtils.DATA_KEY).getJSONObject(0);
                post = parseRemovedPost(postJson, post);
            } catch (JSONException e) {
                e.printStackTrace();
                post = null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (post != null)
                listener.fetchSuccess(post);
            else
                listener.fetchFailed();
        }
    }
}

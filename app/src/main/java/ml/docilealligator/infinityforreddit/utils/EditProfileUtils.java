package ml.docilealligator.infinityforreddit.utils;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.subreddit.SubredditSettingData;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public final class EditProfileUtils {

    public static void updateProfile(Retrofit oauthRetrofit,
                                     String accessToken,
                                     String accountName,
                                     String displayName,
                                     String publicDesc,
                                     EditProfileUtilsListener listener) {
        final Map<String, String> oauthHeader = APIUtils.getOAuthHeader(accessToken);
        final RedditAPI api = oauthRetrofit.create(RedditAPI.class);
        final String name = "u_" + accountName;
        api.getSubredditSetting(oauthHeader, name).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        final String json = response.body();
                        if (json == null) {
                            listener.failed("Something happen.");
                            return;
                        }

                        final JSONObject resBody = new JSONObject(json);
                        final SubredditSettingData data = new Gson().fromJson(resBody.getString("data"), SubredditSettingData.class);

                        if (data.getPublicDescription().equals(publicDesc)
                                && data.getTitle().equals(displayName)) {
                            // no-op
                            listener.success();
                            return;
                        }

                        final Map<String, String> params = new HashMap<>();

                        params.put("api_type", "json");
                        params.put("sr", data.getSubredditId());
                        params.put("name", name);
                        params.put("type", data.getSubredditType());
                        // Only this 2 param
                        params.put("public_description", publicDesc);
                        params.put("title", displayName);
                        // Official Reddit app have this 2 params
                        // 1 = disable; 0 = enable || Active in communities visibility || Show which communities I am active in on my profile.
                        params.put("toxicity_threshold_chat_level", String.valueOf(data.getToxicityThresholdChatLevel()));
                        //  Content visibility || Posts to this profile can appear in r/all and your profile can be discovered in /users
                        params.put("default_set", String.valueOf(data.isDefaultSet()));

                        // Allow people to follow you || Followers will be notified about posts you make to your profile and see them in their home feed.
                        params.put("accept_followers", String.valueOf(data.isAcceptFollowers()));

                        params.put("allow_top", String.valueOf(data.isPublicTraffic())); //
                        params.put("link_type", String.valueOf(data.getContentOptions())); //
                        //
                        params.put("original_content_tag_enabled", String.valueOf(data.isOriginalContentTagEnabled()));
                        params.put("new_pinned_post_pns_enabled", String.valueOf(data.isNewPinnedPostPnsEnabled()));
                        params.put("prediction_leaderboard_entry_type", String.valueOf(data.getPredictionLeaderboardEntryType()));
                        params.put("restrict_commenting", String.valueOf(data.isRestrictCommenting()));
                        params.put("restrict_posting", String.valueOf(data.isRestrictPosting()));
                        params.put("should_archive_posts", String.valueOf(data.isShouldArchivePosts()));
                        params.put("show_media", String.valueOf(data.isShowMedia()));
                        params.put("show_media_preview", String.valueOf(data.isShowMediaPreview()));
                        params.put("spam_comments", data.getSpamComments());
                        params.put("spam_links", data.getSpamLinks());
                        params.put("spam_selfposts", data.getSpamSelfPosts());
                        params.put("spoilers_enabled", String.valueOf(data.isSpoilersEnabled()));
                        params.put("submit_link_label", data.getSubmitLinkLabel());
                        params.put("submit_text", data.getSubmitText());
                        params.put("submit_text_label", data.getSubmitTextLabel());
                        params.put("user_flair_pns_enabled", String.valueOf(data.isUserFlairPnsEnabled()));
                        params.put("all_original_content", String.valueOf(data.isAllOriginalContent()));
                        params.put("allow_chat_post_creation", String.valueOf(data.isAllowChatPostCreation()));
                        params.put("allow_discovery", String.valueOf(data.isAllowDiscovery()));
                        params.put("allow_galleries", String.valueOf(data.isAllowGalleries()));
                        params.put("allow_images", String.valueOf(data.isAllowImages()));
                        params.put("allow_polls", String.valueOf(data.isAllowPolls()));
                        params.put("allow_post_crossposts", String.valueOf(data.isAllowPostCrossPosts()));
                        params.put("allow_prediction_contributors", String.valueOf(data.isAllowPredictionContributors()));
                        params.put("allow_predictions", String.valueOf(data.isAllowPredictions()));
                        params.put("allow_predictions_tournament", String.valueOf(data.isAllowPredictionsTournament()));
                        params.put("allow_videos", String.valueOf(data.isAllowVideos()));
                        params.put("collapse_deleted_comments", String.valueOf(data.isCollapseDeletedComments()));
                        params.put("comment_score_hide_mins", String.valueOf(data.getCommentScoreHideMins()));
                        params.put("crowd_control_chat_level", String.valueOf(data.getCrowdControlChatLevel()));
                        params.put("crowd_control_filter", String.valueOf(data.getCrowdControlChatLevel()));
                        params.put("crowd_control_level", String.valueOf(data.getCrowdControlLevel()));
                        params.put("crowd_control_mode", String.valueOf(data.isCrowdControlMode()));
                        params.put("description", data.getDescription());
                        params.put("disable_contributor_requests", String.valueOf(data.isDisableContributorRequests()));
                        params.put("exclude_banned_modqueue", String.valueOf(data.isExcludeBannedModQueue()));
                        params.put("free_form_reports", String.valueOf(data.isFreeFormReports()));
                        params.put("header-title", data.getHeaderHoverText());
                        params.put("hide_ads", String.valueOf(data.isHideAds()));
                        params.put("key_color", data.getKeyColor());
                        params.put("lang", data.getLanguage());
                        params.put("over_18", String.valueOf(data.isOver18()));
                        params.put("suggested_comment_sort", data.getSuggestedCommentSort());
                        params.put("welcome_message_enabled", String.valueOf(data.isWelcomeMessageEnabled()));
                        params.put("welcome_message_text", String.valueOf(data.getWelcomeMessageText()));
                        params.put("wiki_edit_age", String.valueOf(data.getWikiEditAge()));
                        params.put("wiki_edit_karma", String.valueOf(data.getWikiEditKarma()));
                        params.put("wikimode", data.getWikiMode());

                        api.postSiteAdmin(oauthHeader, params)
                                .enqueue(new Callback<>() {
                                    @Override
                                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                        if (response.isSuccessful()) listener.success();
                                        else listener.failed(response.message());
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                        t.printStackTrace();
                                        listener.failed(t.getLocalizedMessage());
                                    }
                                });
                    } catch (JSONException e) {
                        listener.failed(e.getLocalizedMessage());
                    }
                } else {
                    listener.failed(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                t.printStackTrace();
                listener.failed(t.getLocalizedMessage());
            }
        });

    }

    public static void uploadAvatar(Retrofit oauthRetrofit,
                                    String accessToken,
                                    String accountName,
                                    Bitmap image,
                                    EditProfileUtilsListener listener) {
        oauthRetrofit.create(RedditAPI.class)
                .uploadSrImg(
                        APIUtils.getOAuthHeader(accessToken),
                        "u_" + accountName,
                        requestBodyUploadSr("icon"),
                        fileToUpload(image, accountName + "-icon"))
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call,
                                           @NonNull Response<String> response) {
                        if (response.isSuccessful()) listener.success();
                        else listener.failed(response.message());
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        listener.failed(t.getLocalizedMessage());
                    }
                });
    }

    public static void uploadBanner(Retrofit oauthRetrofit,
                                    String accessToken,
                                    String accountName,
                                    Bitmap image,
                                    EditProfileUtilsListener listener) {
        oauthRetrofit.create(RedditAPI.class)
                .uploadSrImg(
                        APIUtils.getOAuthHeader(accessToken),
                        "u_" + accountName,
                        requestBodyUploadSr("banner"),
                        fileToUpload(image, accountName + "-banner"))
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call,
                                           @NonNull Response<String> response) {
                        if (response.isSuccessful()) listener.success();
                        else listener.failed(response.message());
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        listener.failed(t.getLocalizedMessage());
                    }
                });
    }

    public static void deleteAvatar(Retrofit oauthRetrofit,
                                    String accessToken,
                                    String accountName,
                                    EditProfileUtilsListener listener) {
        oauthRetrofit.create(RedditAPI.class)
                .deleteSrIcon(APIUtils.getOAuthHeader(accessToken), "u_" + accountName)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call,
                                           @NonNull Response<String> response) {
                        if (response.isSuccessful()) listener.success();
                        else listener.failed(response.message());
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        listener.failed(t.getLocalizedMessage());
                    }
                });
    }

    public static void deleteBanner(Retrofit oauthRetrofit,
                                    String accessToken,
                                    String accountName,
                                    EditProfileUtilsListener listener) {
        oauthRetrofit.create(RedditAPI.class)
                .deleteSrBanner(APIUtils.getOAuthHeader(accessToken), "u_" + accountName)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call,
                                           @NonNull Response<String> response) {
                        if (response.isSuccessful()) listener.success();
                        else listener.failed(response.message());
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        listener.failed(t.getLocalizedMessage());
                    }
                });
    }

    private static Map<String, RequestBody> requestBodyUploadSr(String type) {
        Map<String, RequestBody> param = new HashMap<>();
        param.put("upload_type", APIUtils.getRequestBody(type));
        param.put("img_type", APIUtils.getRequestBody("jpg"));
        return param;
    }

    private static MultipartBody.Part fileToUpload(Bitmap image, String fileName) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        RequestBody fileBody = RequestBody.create(byteArray,
                MediaType.parse("image/*"));
        return MultipartBody.Part.createFormData("file", fileName + ".jpg", fileBody);
    }

    public interface EditProfileUtilsListener {
        void success();

        void failed(String message);
    }
}

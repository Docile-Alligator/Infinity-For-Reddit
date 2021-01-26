package ml.docilealligator.infinityforreddit.post;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ml.docilealligator.infinityforreddit.Flair;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SubmitPost {
    public static void submitTextOrLinkPost(Retrofit oauthRetrofit, String accessToken,
                                            Locale locale, String subredditName, String title, String content,
                                            Flair flair, boolean isSpoiler, boolean isNSFW, String kind,
                                            SubmitPostListener submitPostListener) {
        submitPost(oauthRetrofit, accessToken, subredditName, title, content,
                flair, isSpoiler, isNSFW, kind, null, submitPostListener);
    }

    public static void submitImagePost(Retrofit oauthRetrofit, Retrofit uploadMediaRetrofit,
                                       String accessToken, String subredditName, String title, Bitmap image,
                                       Flair flair, boolean isSpoiler, boolean isNSFW, SubmitPostListener submitPostListener) {
        try {
            String imageUrlOrError = uploadImage(oauthRetrofit, uploadMediaRetrofit, accessToken, image);
            if (imageUrlOrError != null && !imageUrlOrError.startsWith("Error: ")) {
                submitPost(oauthRetrofit, accessToken,
                        subredditName, title, imageUrlOrError, flair, isSpoiler, isNSFW,
                        APIUtils.KIND_IMAGE, null, submitPostListener);
            } else {
                submitPostListener.submitFailed(imageUrlOrError);
            }
        } catch (IOException | JSONException | XmlPullParserException e) {
            e.printStackTrace();
            submitPostListener.submitFailed(e.getMessage());
        }
    }

    public static void submitVideoPost(Retrofit oauthRetrofit, Retrofit uploadMediaRetrofit,
                                       Retrofit uploadVideoRetrofit, String accessToken,
                                       String subredditName, String title, File buffer, String mimeType,
                                       Bitmap posterBitmap, Flair flair, boolean isSpoiler, boolean isNSFW,
                                       SubmitPostListener submitPostListener) {
        RedditAPI api = oauthRetrofit.create(RedditAPI.class);

        String fileType = mimeType.substring(mimeType.indexOf("/") + 1);

        Map<String, String> uploadImageParams = new HashMap<>();
        uploadImageParams.put(APIUtils.FILEPATH_KEY, "post_video." + fileType);
        uploadImageParams.put(APIUtils.MIMETYPE_KEY, mimeType);

        Call<String> uploadImageCall = api.uploadImage(APIUtils.getOAuthHeader(accessToken), uploadImageParams);
        try {
            Response<String> uploadImageResponse = uploadImageCall.execute();
            if (uploadImageResponse.isSuccessful()) {
                Map<String, RequestBody> nameValuePairsMap = parseJSONResponseFromAWS(uploadImageResponse.body());

                RequestBody fileBody = RequestBody.create(buffer, MediaType.parse("application/octet-stream"));
                MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", "post_video." + fileType, fileBody);

                RedditAPI uploadVideoToAWSApi;
                if (fileType.equals("gif")) {
                    uploadVideoToAWSApi = uploadMediaRetrofit.create(RedditAPI.class);
                } else {
                    uploadVideoToAWSApi = uploadVideoRetrofit.create(RedditAPI.class);
                }
                Call<String> uploadMediaToAWS = uploadVideoToAWSApi.uploadMediaToAWS(nameValuePairsMap, fileToUpload);
                Response<String> uploadMediaToAWSResponse = uploadMediaToAWS.execute();
                if (uploadMediaToAWSResponse.isSuccessful()) {
                    String url = parseXMLResponseFromAWS(uploadMediaToAWSResponse.body());
                    if (url == null) {
                        submitPostListener.submitFailed(null);
                        return;
                    }
                    String imageUrlOrError = uploadImage(oauthRetrofit, uploadMediaRetrofit, accessToken, posterBitmap);
                    if (imageUrlOrError != null && !imageUrlOrError.startsWith("Error: ")) {
                        if (fileType.equals("gif")) {
                            submitPost(oauthRetrofit, accessToken,
                                    subredditName, title, url, flair, isSpoiler, isNSFW,
                                    APIUtils.KIND_VIDEOGIF, imageUrlOrError, submitPostListener);
                        } else {
                            submitPost(oauthRetrofit, accessToken,
                                    subredditName, title, url, flair, isSpoiler, isNSFW,
                                    APIUtils.KIND_VIDEO, imageUrlOrError, submitPostListener);
                        }
                    } else {
                        submitPostListener.submitFailed(imageUrlOrError);
                    }
                } else {
                    submitPostListener.submitFailed(uploadMediaToAWSResponse.code() + " " + uploadMediaToAWSResponse.message());
                }
            } else {
                submitPostListener.submitFailed(uploadImageResponse.code() + " " + uploadImageResponse.message());
            }
        } catch (IOException | XmlPullParserException | JSONException e) {
            e.printStackTrace();
            submitPostListener.submitFailed(e.getMessage());
        }
    }

    public static void submitCrosspost(Retrofit oauthRetrofit, String accessToken,
                                            String subredditName, String title, String crosspostFullname,
                                            Flair flair, boolean isSpoiler, boolean isNSFW, String kind,
                                            SubmitPostListener submitPostListener) {
        submitPost(oauthRetrofit, accessToken, subredditName, title, crosspostFullname,
                flair, isSpoiler, isNSFW, kind, null, submitPostListener);
    }

    private static void submitPost(Retrofit oauthRetrofit, String accessToken,
                                   String subredditName, String title, String content,
                                   Flair flair, boolean isSpoiler, boolean isNSFW, String kind,
                                   @Nullable String posterUrl, SubmitPostListener submitPostListener) {
        RedditAPI api = oauthRetrofit.create(RedditAPI.class);

        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.API_TYPE_KEY, APIUtils.API_TYPE_JSON);
        params.put(APIUtils.SR_KEY, subredditName);
        params.put(APIUtils.TITLE_KEY, title);
        params.put(APIUtils.KIND_KEY, kind);
        switch (kind) {
            case APIUtils.KIND_SELF:
                params.put(APIUtils.TEXT_KEY, content);
                break;
            case APIUtils.KIND_LINK:
            case APIUtils.KIND_IMAGE:
                params.put(APIUtils.URL_KEY, content);
                break;
            case APIUtils.KIND_VIDEOGIF:
                params.put(APIUtils.KIND_KEY, APIUtils.KIND_IMAGE);
                params.put(APIUtils.URL_KEY, content);
                params.put(APIUtils.VIDEO_POSTER_URL_KEY, posterUrl);
                break;
            case APIUtils.KIND_VIDEO:
                params.put(APIUtils.URL_KEY, content);
                params.put(APIUtils.VIDEO_POSTER_URL_KEY, posterUrl);
                break;
            case APIUtils.KIND_CROSSPOST:
                params.put(APIUtils.CROSSPOST_FULLNAME_KEY, content);
                break;
        }

        if (flair != null) {
            params.put(APIUtils.FLAIR_TEXT_KEY, flair.getText());
            params.put(APIUtils.FLAIR_ID_KEY, flair.getId());
        }
        params.put(APIUtils.SPOILER_KEY, Boolean.toString(isSpoiler));
        params.put(APIUtils.NSFW_KEY, Boolean.toString(isNSFW));

        Call<String> submitPostCall = api.submit(APIUtils.getOAuthHeader(accessToken), params);

        try {
            Response<String> response = submitPostCall.execute();
            if (response.isSuccessful()) {
                getSubmittedPost(response.body(), kind, oauthRetrofit, accessToken, submitPostListener);
            } else {
                submitPostListener.submitFailed(response.message());
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            submitPostListener.submitFailed(e.getMessage());
        }
    }

    @Nullable
    private static String uploadImage(Retrofit oauthRetrofit, Retrofit uploadMediaRetrofit,
                                    String accessToken, Bitmap image) throws IOException, JSONException, XmlPullParserException {
        RedditAPI api = oauthRetrofit.create(RedditAPI.class);

        Map<String, String> uploadImageParams = new HashMap<>();
        uploadImageParams.put(APIUtils.FILEPATH_KEY, "post_image.jpg");
        uploadImageParams.put(APIUtils.MIMETYPE_KEY, "image/jpeg");

        Call<String> uploadImageCall = api.uploadImage(APIUtils.getOAuthHeader(accessToken), uploadImageParams);
        Response<String> uploadImageResponse = uploadImageCall.execute();
        if (uploadImageResponse.isSuccessful()) {
            Map<String, RequestBody> nameValuePairsMap = parseJSONResponseFromAWS(uploadImageResponse.body());

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), byteArray);
            MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", "post_image.jpg", fileBody);

            RedditAPI uploadMediaToAWSApi = uploadMediaRetrofit.create(RedditAPI.class);
            Call<String> uploadMediaToAWS = uploadMediaToAWSApi.uploadMediaToAWS(nameValuePairsMap, fileToUpload);
            Response<String> uploadMediaToAWSResponse = uploadMediaToAWS.execute();
            if (uploadMediaToAWSResponse.isSuccessful()) {
                return parseXMLResponseFromAWS(uploadMediaToAWSResponse.body());
            } else {
                return "Error: " + uploadMediaToAWSResponse.code();
            }
        } else {
            return "Error: " + uploadImageResponse.message();
        }
    }

    private static void getSubmittedPost(String response, String kind, Retrofit oauthRetrofit,
                                         String accessToken, SubmitPostListener submitPostListener) throws JSONException, IOException {
        JSONObject responseObject = new JSONObject(response).getJSONObject(JSONUtils.JSON_KEY);
        if (responseObject.getJSONArray(JSONUtils.ERRORS_KEY).length() != 0) {
            JSONArray error = responseObject.getJSONArray(JSONUtils.ERRORS_KEY)
                    .getJSONArray(responseObject.getJSONArray(JSONUtils.ERRORS_KEY).length() - 1);
            if (error.length() != 0) {
                String errorString;
                if (error.length() >= 2) {
                    errorString = error.getString(1);
                } else {
                    errorString = error.getString(0);
                }
                submitPostListener.submitFailed(errorString);
            } else {
                submitPostListener.submitFailed(null);
            }

            return;
        }

        if (!kind.equals(APIUtils.KIND_IMAGE) && !kind.equals(APIUtils.KIND_VIDEO) && !kind.equals(APIUtils.KIND_VIDEOGIF)) {
            String postId = responseObject.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.ID_KEY);

            RedditAPI api = oauthRetrofit.create(RedditAPI.class);

            Call<String> getPostCall = api.getPostOauth(postId, APIUtils.getOAuthHeader(accessToken));
            Response<String> getPostCallResponse = getPostCall.execute();
            if (getPostCallResponse.isSuccessful()) {
                ParsePost.parsePost(getPostCallResponse.body(), new ParsePost.ParsePostListener() {
                    @Override
                    public void onParsePostSuccess(Post post) {
                        submitPostListener.submitSuccessful(post);
                    }

                    @Override
                    public void onParsePostFail() {
                        submitPostListener.submitFailed(null);
                    }
                });
            } else {
                submitPostListener.submitFailed(getPostCallResponse.message());
            }
        } else {
            submitPostListener.submitSuccessful(null);
        }
    }

    public interface SubmitPostListener {
        void submitSuccessful(Post post);

        void submitFailed(@Nullable String errorMessage);
    }

    private static Map<String, RequestBody> parseJSONResponseFromAWS(String response) throws JSONException {
        JSONObject responseObject = new JSONObject(response);
        JSONArray nameValuePairs = responseObject.getJSONObject(JSONUtils.ARGS_KEY).getJSONArray(JSONUtils.FIELDS_KEY);

        Map<String, RequestBody> nameValuePairsMap = new HashMap<>();
        for (int i = 0; i < nameValuePairs.length(); i++) {
            nameValuePairsMap.put(nameValuePairs.getJSONObject(i).getString(JSONUtils.NAME_KEY),
                    APIUtils.getRequestBody(nameValuePairs.getJSONObject(i).getString(JSONUtils.VALUE_KEY)));
        }

        return nameValuePairsMap;
    }

    @Nullable
    private static String parseXMLResponseFromAWS(String response) throws XmlPullParserException, IOException {
        XmlPullParser xmlPullParser = XmlPullParserFactory.newInstance().newPullParser();
        xmlPullParser.setInput(new StringReader(response));

        boolean isLocationTag = false;
        int eventType = xmlPullParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (xmlPullParser.getName().equals("Location")) {
                    isLocationTag = true;
                }
            } else if (eventType == XmlPullParser.TEXT) {
                if (isLocationTag) {
                    return xmlPullParser.getText();
                }
            }
            eventType = xmlPullParser.next();
        }

        return null;
    }
}

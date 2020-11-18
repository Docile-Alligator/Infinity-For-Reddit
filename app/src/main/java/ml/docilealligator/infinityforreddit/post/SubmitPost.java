package ml.docilealligator.infinityforreddit.post;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
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

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.Flair;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SubmitPost {
    public static void submitTextOrLinkPost(Retrofit oauthRetrofit, String accessToken,
                                            Locale locale, String subredditName, String title, String content,
                                            Flair flair, boolean isSpoiler, boolean isNSFW, String kind,
                                            SubmitPostListener submitPostListener) {
        submitPost(oauthRetrofit, accessToken, locale, subredditName, title, content,
                flair, isSpoiler, isNSFW, kind, null, submitPostListener);
    }

    public static void submitImagePost(Retrofit oauthRetrofit, Retrofit uploadMediaRetrofit,
                                       String accessToken, Locale locale,
                                       String subredditName, String title, Bitmap image, Flair flair,
                                       boolean isSpoiler, boolean isNSFW, SubmitPostListener submitPostListener) {
        uploadImage(oauthRetrofit, uploadMediaRetrofit, accessToken, image,
                new UploadImageListener() {
                    @Override
                    public void uploaded(String imageUrl) {
                        submitPost(oauthRetrofit, accessToken, locale,
                                subredditName, title, imageUrl, flair, isSpoiler, isNSFW,
                                APIUtils.KIND_IMAGE, null, submitPostListener);
                    }

                    @Override
                    public void uploadFailed(@Nullable String errorMessage) {
                        submitPostListener.submitFailed(errorMessage);
                    }
                });
    }

    public static void submitVideoPost(Retrofit oauthRetrofit, Retrofit uploadMediaRetrofit,
                                       Retrofit uploadVideoRetrofit, String accessToken,
                                       Locale locale, String subredditName, String title, File buffer, String mimeType,
                                       Bitmap posterBitmap, Flair flair, boolean isSpoiler, boolean isNSFW,
                                       SubmitPostListener submitPostListener) {
        RedditAPI api = oauthRetrofit.create(RedditAPI.class);

        String fileType = mimeType.substring(mimeType.indexOf("/") + 1);

        Map<String, String> uploadImageParams = new HashMap<>();
        uploadImageParams.put(APIUtils.FILEPATH_KEY, "post_video." + fileType);
        uploadImageParams.put(APIUtils.MIMETYPE_KEY, mimeType);

        Call<String> uploadImageCall = api.uploadImage(APIUtils.getOAuthHeader(accessToken), uploadImageParams);
        uploadImageCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    new ParseJSONResponseFromAWSAsyncTask(response.body(), new ParseJSONResponseFromAWSAsyncTask.ParseJSONResponseFromAWSListener() {
                        @Override
                        public void parseSuccessful(Map<String, RequestBody> nameValuePairsMap) {
                            RequestBody fileBody = RequestBody.create(buffer, MediaType.parse("application/octet-stream"));
                            MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", "post_video." + fileType, fileBody);

                            RedditAPI uploadVideoToAWSApi;
                            if (fileType.equals("gif")) {
                                uploadVideoToAWSApi = uploadMediaRetrofit.create(RedditAPI.class);
                            } else {
                                uploadVideoToAWSApi = uploadVideoRetrofit.create(RedditAPI.class);
                            }
                            Call<String> uploadMediaToAWS = uploadVideoToAWSApi.uploadMediaToAWS(nameValuePairsMap, fileToUpload);

                            uploadMediaToAWS.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                    if (response.isSuccessful()) {
                                        new ParseXMLReponseFromAWSAsyncTask(response.body(), new ParseXMLReponseFromAWSAsyncTask.ParseXMLResponseFromAWSListener() {
                                            @Override
                                            public void parseSuccessful(String url) {
                                                uploadImage(oauthRetrofit, uploadMediaRetrofit, accessToken,
                                                        posterBitmap, new UploadImageListener() {
                                                            @Override
                                                            public void uploaded(String imageUrl) {
                                                                if (fileType.equals("gif")) {
                                                                    submitPost(oauthRetrofit, accessToken, locale,
                                                                            subredditName, title, url, flair, isSpoiler, isNSFW,
                                                                            APIUtils.KIND_VIDEOGIF, imageUrl, submitPostListener);
                                                                } else {
                                                                    submitPost(oauthRetrofit, accessToken, locale,
                                                                            subredditName, title, url, flair, isSpoiler, isNSFW,
                                                                            APIUtils.KIND_VIDEO, imageUrl, submitPostListener);
                                                                }
                                                            }

                                                            @Override
                                                            public void uploadFailed(@Nullable String errorMessage) {
                                                                submitPostListener.submitFailed(errorMessage);
                                                            }
                                                        });
                                            }

                                            @Override
                                            public void parseFailed() {
                                                submitPostListener.submitFailed(null);
                                            }
                                        }).execute();
                                    } else {
                                        submitPostListener.submitFailed(response.message());
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                    submitPostListener.submitFailed(t.getMessage());
                                }
                            });
                        }

                        @Override
                        public void parseFailed() {
                            submitPostListener.submitFailed(null);
                        }
                    }).execute();
                } else {
                    submitPostListener.submitFailed(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                submitPostListener.submitFailed(t.getMessage());
            }
        });
    }

    public static void submitCrosspost(Retrofit oauthRetrofit, String accessToken,
                                            Locale locale, String subredditName, String title, String crosspostFullname,
                                            Flair flair, boolean isSpoiler, boolean isNSFW, String kind,
                                            SubmitPostListener submitPostListener) {
        submitPost(oauthRetrofit, accessToken, locale, subredditName, title, crosspostFullname,
                flair, isSpoiler, isNSFW, kind, null, submitPostListener);
    }

    private static void submitPost(Retrofit oauthRetrofit, String accessToken,
                                   Locale locale, String subredditName, String title, String content,
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
        submitPostCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        getSubmittedPost(response.body(), kind, oauthRetrofit, accessToken,
                                locale, submitPostListener);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        submitPostListener.submitFailed(null);
                    }
                } else {
                    submitPostListener.submitFailed(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                submitPostListener.submitFailed(t.getMessage());
            }
        });
    }

    private static void uploadImage(Retrofit oauthRetrofit, Retrofit uploadMediaRetrofit,
                                    String accessToken, Bitmap image,
                                    UploadImageListener uploadImageListener) {
        RedditAPI api = oauthRetrofit.create(RedditAPI.class);

        Map<String, String> uploadImageParams = new HashMap<>();
        uploadImageParams.put(APIUtils.FILEPATH_KEY, "post_image.jpg");
        uploadImageParams.put(APIUtils.MIMETYPE_KEY, "image/jpeg");

        Call<String> uploadImageCall = api.uploadImage(APIUtils.getOAuthHeader(accessToken), uploadImageParams);
        uploadImageCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    new ParseJSONResponseFromAWSAsyncTask(response.body(), new ParseJSONResponseFromAWSAsyncTask.ParseJSONResponseFromAWSListener() {
                        @Override
                        public void parseSuccessful(Map<String, RequestBody> nameValuePairsMap) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byte[] byteArray = stream.toByteArray();

                            RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), byteArray);
                            MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", "post_image.jpg", fileBody);

                            RedditAPI uploadMediaToAWSApi = uploadMediaRetrofit.create(RedditAPI.class);
                            Call<String> uploadMediaToAWS = uploadMediaToAWSApi.uploadMediaToAWS(nameValuePairsMap, fileToUpload);

                            uploadMediaToAWS.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                    if (response.isSuccessful()) {
                                        new ParseXMLReponseFromAWSAsyncTask(response.body(), new ParseXMLReponseFromAWSAsyncTask.ParseXMLResponseFromAWSListener() {
                                            @Override
                                            public void parseSuccessful(String url) {
                                                uploadImageListener.uploaded(url);
                                            }

                                            @Override
                                            public void parseFailed() {
                                                uploadImageListener.uploadFailed(null);
                                            }
                                        }).execute();
                                    } else {
                                        uploadImageListener.uploadFailed("Error: " + response.code());
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                    uploadImageListener.uploadFailed(t.getMessage());
                                }
                            });
                        }

                        @Override
                        public void parseFailed() {
                            uploadImageListener.uploadFailed(null);
                        }
                    }).execute();
                } else {
                    uploadImageListener.uploadFailed(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                uploadImageListener.uploadFailed(t.getMessage());
            }
        });
    }

    private static void getSubmittedPost(String response, String kind, Retrofit oauthRetrofit,
                                         String accessToken, Locale locale,
                                         SubmitPostListener submitPostListener) throws JSONException {
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
            getPostCall.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                    if (response.isSuccessful()) {
                        ParsePost.parsePost(response.body(), new ParsePost.ParsePostListener() {
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
                        submitPostListener.submitFailed(response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    submitPostListener.submitFailed(t.getMessage());
                }
            });
        } else {
            submitPostListener.submitSuccessful(null);
        }
    }

    public interface SubmitPostListener {
        void submitSuccessful(Post post);

        void submitFailed(@Nullable String errorMessage);
    }

    private interface UploadImageListener {
        void uploaded(String imageUrl);

        void uploadFailed(@Nullable String errorMessage);
    }

    private static class ParseJSONResponseFromAWSAsyncTask extends AsyncTask<Void, Void, Void> {
        private String response;
        private ParseJSONResponseFromAWSListener parseJSONResponseFromAWSListener;
        private Map<String, RequestBody> nameValuePairsMap;
        private boolean successful;
        ParseJSONResponseFromAWSAsyncTask(String response, ParseJSONResponseFromAWSListener parseJSONResponseFromAWSListener) {
            this.response = response;
            this.parseJSONResponseFromAWSListener = parseJSONResponseFromAWSListener;
            nameValuePairsMap = new HashMap<>();
            successful = false;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject responseObject = new JSONObject(response);
                JSONArray nameValuePairs = responseObject.getJSONObject(JSONUtils.ARGS_KEY).getJSONArray(JSONUtils.FIELDS_KEY);

                nameValuePairsMap = new HashMap<>();
                for (int i = 0; i < nameValuePairs.length(); i++) {
                    nameValuePairsMap.put(nameValuePairs.getJSONObject(i).getString(JSONUtils.NAME_KEY),
                            APIUtils.getRequestBody(nameValuePairs.getJSONObject(i).getString(JSONUtils.VALUE_KEY)));
                }

                successful = true;
            } catch (JSONException e) {
                e.printStackTrace();
                successful = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (successful) {
                parseJSONResponseFromAWSListener.parseSuccessful(nameValuePairsMap);
            } else {
                parseJSONResponseFromAWSListener.parseFailed();
            }
        }

        interface ParseJSONResponseFromAWSListener {
            void parseSuccessful(Map<String, RequestBody> nameValuePairsMap);

            void parseFailed();
        }
    }

    private static class ParseXMLReponseFromAWSAsyncTask extends AsyncTask<Void, Void, Void> {
        private String response;
        private ParseXMLResponseFromAWSListener parseXMLResponseFromAWSListener;
        private String imageUrl;
        private boolean successful;
        ParseXMLReponseFromAWSAsyncTask(String response, ParseXMLResponseFromAWSListener parseXMLResponseFromAWSListener) {
            this.response = response;
            this.parseXMLResponseFromAWSListener = parseXMLResponseFromAWSListener;
            successful = false;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
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
                            imageUrl = xmlPullParser.getText();
                            successful = true;
                            return null;
                        }
                    }
                    eventType = xmlPullParser.next();
                }
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
                successful = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (successful) {
                parseXMLResponseFromAWSListener.parseSuccessful(imageUrl);
            } else {
                parseXMLResponseFromAWSListener.parseFailed();
            }
        }

        interface ParseXMLResponseFromAWSListener {
            void parseSuccessful(String url);

            void parseFailed();
        }
    }
}

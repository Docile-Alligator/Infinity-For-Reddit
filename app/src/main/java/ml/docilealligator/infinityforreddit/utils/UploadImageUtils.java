package ml.docilealligator.infinityforreddit.utils;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UploadImageUtils {
    @Nullable
    public static String uploadVideoPosterImage(Retrofit oauthRetrofit, Retrofit uploadMediaRetrofit,
                                                String accessToken, Bitmap image) throws IOException, JSONException, XmlPullParserException {
        RedditAPI api = oauthRetrofit.create(RedditAPI.class);

        Map<String, String> uploadImageParams = new HashMap<>();
        uploadImageParams.put(APIUtils.MIMETYPE_KEY, "image/jpeg");

        Call<String> uploadImageCall = api.uploadImage(APIUtils.getOAuthHeader(accessToken), uploadImageParams);
        Response<String> uploadImageResponse = uploadImageCall.execute();
        if (uploadImageResponse.isSuccessful()) {
            Map<String, RequestBody> nameValuePairsMap = parseJSONResponseFromAWS(uploadImageResponse.body());

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            RequestBody fileBody = RequestBody.create(byteArray, MediaType.parse("application/octet-stream"));
            MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", "post_image.jpg", fileBody);

            RedditAPI uploadMediaToAWSApi = uploadMediaRetrofit.create(RedditAPI.class);
            Call<String> uploadMediaToAWS = uploadMediaToAWSApi.uploadMediaToAWS(nameValuePairsMap, fileToUpload);
            Response<String> uploadMediaToAWSResponse = uploadMediaToAWS.execute();
            if (uploadMediaToAWSResponse.isSuccessful()) {
                return parseImageFromXMLResponseFromAWS(uploadMediaToAWSResponse.body(), false);
            } else {
                return "Error: " + uploadMediaToAWSResponse.code();
            }
        } else {
            return "Error: " + uploadImageResponse.message();
        }
    }

    @Nullable
    public static String uploadImage(Retrofit oauthRetrofit, Retrofit uploadMediaRetrofit, ContentResolver contentResolver,
                                     String accessToken, Uri imageUri) throws IOException, JSONException, XmlPullParserException {
        return uploadImage(oauthRetrofit, uploadMediaRetrofit, contentResolver, accessToken, imageUri, false);
    }

    @Nullable
    public static String uploadImage(Retrofit oauthRetrofit, Retrofit uploadMediaRetrofit, ContentResolver contentResolver,
                                     String accessToken, Uri imageUri,
                                     boolean getImageKey) throws IOException, JSONException, XmlPullParserException {
        byte[] buf;
        try (InputStream inputStream = contentResolver.openInputStream(imageUri)) {
            if (inputStream == null) {
                return "Error: Cannot access the image.";
            }
            buf = IOUtils.toByteArray(inputStream);
        }

        // The lease's mimetype, the uploaded part's name and the bytes all describe the same format.
        String mimeType = resolveImageMimeType(contentResolver.getType(imageUri), buf);
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        String fileName = "post_image." + (extension == null ? "jpg" : extension);

        RedditAPI api = oauthRetrofit.create(RedditAPI.class);

        Map<String, String> uploadImageParams = new HashMap<>();
        uploadImageParams.put(APIUtils.MIMETYPE_KEY, mimeType);

        Call<String> uploadImageCall = api.uploadImage(APIUtils.getOAuthHeader(accessToken), uploadImageParams);
        Response<String> uploadImageResponse = uploadImageCall.execute();
        if (!uploadImageResponse.isSuccessful()) {
            return "Error: " + uploadImageResponse.message();
        }

        Map<String, RequestBody> nameValuePairsMap = parseJSONResponseFromAWS(uploadImageResponse.body());

        RequestBody fileBody = RequestBody.create(buf, MediaType.parse("application/octet-stream"));
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", fileName, fileBody);

        RedditAPI uploadMediaToAWSApi = uploadMediaRetrofit.create(RedditAPI.class);
        Call<String> uploadMediaToAWS = uploadMediaToAWSApi.uploadMediaToAWS(nameValuePairsMap, fileToUpload);
        Response<String> uploadMediaToAWSResponse = uploadMediaToAWS.execute();
        if (uploadMediaToAWSResponse.isSuccessful()) {
            return parseImageFromXMLResponseFromAWS(uploadMediaToAWSResponse.body(), getImageKey);
        } else {
            return "Error: " + uploadMediaToAWSResponse.code();
        }
    }

    /**
     * The image's real format, read from its header, so a provider that mislabels the file (or, like
     * a cropping app's cache provider, reports no type at all) cannot lease it under the wrong one.
     * Falls back to the provider's type for anything BitmapFactory cannot identify, and to JPEG when
     * there is neither -- a null mimetype would make Retrofit reject the lease request outright.
     */
    static String resolveImageMimeType(@Nullable String providerMimeType, byte[] image) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(image, 0, image.length, options);
        if (options.outMimeType != null) {
            return options.outMimeType;
        }
        return providerMimeType == null ? "image/jpeg" : providerMimeType;
    }

    @Nullable
    public static String parseImageFromXMLResponseFromAWS(String response) throws XmlPullParserException, IOException {
        //Get Image URL
        return parseImageFromXMLResponseFromAWS(response, false);
    }

    @Nullable
    public static String parseImageFromXMLResponseFromAWS(@Nullable String response, boolean getImageKey) throws XmlPullParserException, IOException {
        if (response == null) {
            return null;
        }
        XmlPullParser xmlPullParser = XmlPullParserFactory.newInstance().newPullParser();
        xmlPullParser.setInput(new StringReader(response));

        boolean isKeyTag = false;
        int eventType = xmlPullParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if ((xmlPullParser.getName().equals("Key") && getImageKey) || (xmlPullParser.getName().equals("Location") && !getImageKey)) {
                    isKeyTag = true;
                }
            } else if (eventType == XmlPullParser.TEXT) {
                if (isKeyTag) {
                    return xmlPullParser.getText();
                }
            }
            eventType = xmlPullParser.next();
        }

        return null;
    }

    public static Map<String, RequestBody> parseJSONResponseFromAWS(String response) throws JSONException {
        JSONObject responseObject = new JSONObject(response);
        // The upload form arrives wrapped in `args` (a lease that names a filepath, as a video
        // upload's does) or bare (an image lease; see imageLeaseParams).
        JSONObject uploadForm = responseObject.has(JSONUtils.ARGS_KEY)
                ? responseObject.getJSONObject(JSONUtils.ARGS_KEY) : responseObject;
        JSONArray nameValuePairs = uploadForm.getJSONArray(JSONUtils.FIELDS_KEY);

        Map<String, RequestBody> nameValuePairsMap = new HashMap<>();
        for (int i = 0; i < nameValuePairs.length(); i++) {
            nameValuePairsMap.put(nameValuePairs.getJSONObject(i).getString(JSONUtils.NAME_KEY),
                    APIUtils.getRequestBody(nameValuePairs.getJSONObject(i).getString(JSONUtils.VALUE_KEY)));
        }

        return nameValuePairsMap;
    }
}

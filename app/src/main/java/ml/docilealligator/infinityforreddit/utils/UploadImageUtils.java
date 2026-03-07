package ml.docilealligator.infinityforreddit.utils;

import android.content.ContentResolver;
import android.graphics.Bitmap;
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
        uploadImageParams.put(APIUtils.FILEPATH_KEY, "post_image.jpg");
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
                                     String accessToken, Uri imageUri, boolean getImageKey) throws IOException, JSONException, XmlPullParserException {
        return uploadImage(oauthRetrofit, uploadMediaRetrofit, contentResolver, accessToken, imageUri, false, getImageKey);
    }

    @Nullable
    public static String uploadImage(Retrofit oauthRetrofit, Retrofit uploadMediaRetrofit, ContentResolver contentResolver,
                                     String accessToken, Uri imageUri,
                                     boolean returnResponseForGallerySubmission,
                                     boolean getImageKey) throws IOException, JSONException, XmlPullParserException {
        String mimeType = contentResolver.getType(imageUri);
        String extension = "jpg";
        if (mimeType != null) {
            String extensionFromMimeType = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            extension = extensionFromMimeType == null ? extension : extensionFromMimeType;
        }

        RedditAPI api = oauthRetrofit.create(RedditAPI.class);

        Map<String, String> uploadImageParams = new HashMap<>();
        uploadImageParams.put(APIUtils.FILEPATH_KEY, "post_image.jpg");
        uploadImageParams.put(APIUtils.MIMETYPE_KEY, mimeType);

        Call<String> uploadImageCall = api.uploadImage(APIUtils.getOAuthHeader(accessToken), uploadImageParams);
        Response<String> uploadImageResponse = uploadImageCall.execute();
        if (uploadImageResponse.isSuccessful()) {
            Map<String, RequestBody> nameValuePairsMap = parseJSONResponseFromAWS(uploadImageResponse.body());
            try (InputStream inputStream = contentResolver.openInputStream(imageUri)) {
                byte[] buf = IOUtils.toByteArray(inputStream);
                RequestBody fileBody = RequestBody.create(buf, MediaType.parse("application/octet-stream"));
                MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", "post_image." + extension, fileBody);

                RedditAPI uploadMediaToAWSApi = uploadMediaRetrofit.create(RedditAPI.class);
                Call<String> uploadMediaToAWS = uploadMediaToAWSApi.uploadMediaToAWS(nameValuePairsMap, fileToUpload);
                Response<String> uploadMediaToAWSResponse = uploadMediaToAWS.execute();
                if (uploadMediaToAWSResponse.isSuccessful()) {
                    if (returnResponseForGallerySubmission) {
                        return uploadImageResponse.body();
                    }
                    return parseImageFromXMLResponseFromAWS(uploadMediaToAWSResponse.body(), getImageKey);
                } else {
                    return "Error: " + uploadMediaToAWSResponse.code();
                }
            }

        } else {
            return "Error: " + uploadImageResponse.message();
        }
    }

    @Nullable
    public static String parseImageFromXMLResponseFromAWS(String response) throws XmlPullParserException, IOException {
        //Get Image URL
        return parseImageFromXMLResponseFromAWS(response, false);
    }

    @Nullable
    public static String parseImageFromXMLResponseFromAWS(String response, boolean getImageKey) throws XmlPullParserException, IOException {
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
        JSONArray nameValuePairs = responseObject.getJSONObject(JSONUtils.ARGS_KEY).getJSONArray(JSONUtils.FIELDS_KEY);

        Map<String, RequestBody> nameValuePairsMap = new HashMap<>();
        for (int i = 0; i < nameValuePairs.length(); i++) {
            nameValuePairsMap.put(nameValuePairs.getJSONObject(i).getString(JSONUtils.NAME_KEY),
                    APIUtils.getRequestBody(nameValuePairs.getJSONObject(i).getString(JSONUtils.VALUE_KEY)));
        }

        return nameValuePairsMap;
    }
}

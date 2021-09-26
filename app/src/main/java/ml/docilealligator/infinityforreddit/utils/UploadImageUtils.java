package ml.docilealligator.infinityforreddit.utils;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    public static String uploadImage(Retrofit oauthRetrofit, Retrofit uploadMediaRetrofit,
                                     String accessToken, Bitmap image) throws IOException, JSONException, XmlPullParserException {
        return uploadImage(oauthRetrofit, uploadMediaRetrofit, accessToken, image, false);
    }

    @Nullable
    public static String uploadImage(Retrofit oauthRetrofit, Retrofit uploadMediaRetrofit,
                                      String accessToken, Bitmap image, boolean returnResponseForGallerySubmission) throws IOException, JSONException, XmlPullParserException {
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
                if (returnResponseForGallerySubmission) {
                    return uploadImageResponse.body();
                }
                return parseXMLResponseFromAWS(uploadMediaToAWSResponse.body());
            } else {
                return "Error: " + uploadMediaToAWSResponse.code();
            }
        } else {
            return "Error: " + uploadImageResponse.message();
        }
    }

    @Nullable
    public static String parseXMLResponseFromAWS(String response) throws XmlPullParserException, IOException {
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

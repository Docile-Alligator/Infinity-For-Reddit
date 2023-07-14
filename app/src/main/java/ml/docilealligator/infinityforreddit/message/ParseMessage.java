package ml.docilealligator.infinityforreddit.message;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class ParseMessage {
    public static void parseMessage(String response, Locale locale, int messageType,
                                    ParseMessageAsyncTaskListener parseMessageAsnycTaskListener) {
        new ParseMessageAsnycTask(response, locale, messageType, parseMessageAsnycTaskListener).execute();
    }

    public static ArrayList<Message> parseMessages(JSONArray messageArray, Locale locale, int messageType) {
        ArrayList<Message> messages = new ArrayList<>();
        for (int i = 0; i < messageArray.length(); i++) {
            try {
                Message message = parseSingleMessage(messageArray.getJSONObject(i), locale, messageType);
                if (message != null) {
                    messages.add(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    public static void parseRepliedMessage(String response, Locale locale, ParseSentMessageAsyncTaskListener parseSentMessageAsyncTaskListener) {
        new ParseSentMessageAsnycTask(response, locale, parseSentMessageAsyncTaskListener).execute();
    }

    public static void parseComposedMessageError(String response, ParseComposedMessageErrorListener parseComposedMessageErrorListener) {
        new ParseComposedMessageErrorAsncTask(response, parseComposedMessageErrorListener).execute();
    }

    @Nullable
    private static Message parseSingleMessage(JSONObject messageJSON, Locale locale, int messageType) throws JSONException {
        String kind = messageJSON.getString(JSONUtils.KIND_KEY);
        if ((messageType == FetchMessage.MESSAGE_TYPE_INBOX && kind.equals("t4")) ||
                (messageType == FetchMessage.MESSAGE_TYPE_PRIVATE_MESSAGE && !kind.equals("t4"))) {
            return null;
        }

        JSONObject rawMessageJSON = messageJSON.getJSONObject(JSONUtils.DATA_KEY);
        String subredditName = rawMessageJSON.getString(JSONUtils.SUBREDDIT_KEY);
        String subredditNamePrefixed = rawMessageJSON.getString(JSONUtils.SUBREDDIT_NAME_PREFIX_KEY);
        String id = rawMessageJSON.getString(JSONUtils.ID_KEY);
        String fullname = rawMessageJSON.getString(JSONUtils.NAME_KEY);
        String subject = rawMessageJSON.getString(JSONUtils.SUBJECT_KEY);
        String author = rawMessageJSON.getString(JSONUtils.AUTHOR_KEY);
        String destination = rawMessageJSON.getString(JSONUtils.DEST_KEY);
        String parentFullname = rawMessageJSON.getString(JSONUtils.PARENT_ID_KEY);
        String title = rawMessageJSON.has(JSONUtils.LINK_TITLE_KEY) ? rawMessageJSON.getString(JSONUtils.LINK_TITLE_KEY) : null;
        String body = Utils.modifyMarkdown(rawMessageJSON.getString(JSONUtils.BODY_KEY));
        String context = rawMessageJSON.getString(JSONUtils.CONTEXT_KEY);
        String distinguished = rawMessageJSON.getString(JSONUtils.DISTINGUISHED_KEY);
        boolean wasComment = rawMessageJSON.getBoolean(JSONUtils.WAS_COMMENT_KEY);
        boolean isNew = rawMessageJSON.getBoolean(JSONUtils.NEW_KEY);
        int score = rawMessageJSON.getInt(JSONUtils.SCORE_KEY);
        int nComments = rawMessageJSON.isNull(JSONUtils.NUM_COMMENTS_KEY) ? -1 : rawMessageJSON.getInt(JSONUtils.NUM_COMMENTS_KEY);
        long timeUTC = rawMessageJSON.getLong(JSONUtils.CREATED_UTC_KEY) * 1000;

        Calendar submitTimeCalendar = Calendar.getInstance();
        submitTimeCalendar.setTimeInMillis(timeUTC);
        String formattedTime = new SimpleDateFormat("MMM d, yyyy, HH:mm",
                locale).format(submitTimeCalendar.getTime());

        ArrayList<Message> replies = null;
        if (!rawMessageJSON.isNull(JSONUtils.REPLIES_KEY) && rawMessageJSON.get(JSONUtils.REPLIES_KEY) instanceof JSONObject) {
            JSONArray repliesArray = rawMessageJSON.getJSONObject(JSONUtils.REPLIES_KEY).getJSONObject(JSONUtils.DATA_KEY)
                    .getJSONArray(JSONUtils.CHILDREN_KEY);
            replies = parseMessages(repliesArray, locale, messageType);
        }

        Message message = new Message(kind, subredditName, subredditNamePrefixed, id, fullname, subject,
                author, destination, parentFullname, title, body, context, distinguished, formattedTime,
                wasComment, isNew, score, nComments, timeUTC);
        if (replies != null) {
            message.setReplies(replies);
        }

        return message;
    }

    private static class ParseMessageAsnycTask extends AsyncTask<Void, Void, Void> {

        private String response;
        private Locale locale;
        private ArrayList<Message> messages;
        private String after;
        private int messageType;
        private ParseMessageAsyncTaskListener parseMessageAsyncTaskListener;
        ParseMessageAsnycTask(String response, Locale locale, int messageType,
                              ParseMessageAsyncTaskListener parseMessageAsnycTaskListener) {
            this.response = response;
            this.locale = locale;
            this.messageType = messageType;
            messages = new ArrayList<>();
            this.parseMessageAsyncTaskListener = parseMessageAsnycTaskListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONArray messageArray = new JSONObject(response).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                messages = parseMessages(messageArray, locale, messageType);
                after = new JSONObject(response).getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            parseMessageAsyncTaskListener.parseSuccess(messages, after);
        }
    }

    private static class ParseSentMessageAsnycTask extends AsyncTask<Void, Void, Void> {

        private String response;
        private Locale locale;
        private Message message;
        private String errorMessage;
        private boolean parseFailed = false;
        private ParseSentMessageAsyncTaskListener parseSentMessageAsyncTaskListener;

        ParseSentMessageAsnycTask(String response, Locale locale, ParseSentMessageAsyncTaskListener parseSentMessageAsyncTaskListener) {
            this.response = response;
            this.locale = locale;
            this.parseSentMessageAsyncTaskListener = parseSentMessageAsyncTaskListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject messageJSON = new JSONObject(response).getJSONObject(JSONUtils.JSON_KEY)
                        .getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.THINGS_KEY).getJSONObject(0);
                message = parseSingleMessage(messageJSON, locale, FetchMessage.MESSAGE_TYPE_PRIVATE_MESSAGE);
            } catch (JSONException e) {
                e.printStackTrace();
                errorMessage = parseRepliedMessageErrorMessage(response);
                parseFailed = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (parseFailed) {
                parseSentMessageAsyncTaskListener.parseFailed(errorMessage);
            } else {
                parseSentMessageAsyncTaskListener.parseSuccess(message);
            }
        }
    }

    private static class ParseComposedMessageErrorAsncTask extends AsyncTask<Void, Void, Void> {
        private String response;
        private ParseComposedMessageErrorListener parseComposedMessageErrorListener;
        private String errorMessage;

        ParseComposedMessageErrorAsncTask(String response, ParseComposedMessageErrorListener parseComposedMessageErrorListener) {
            this.response = response;
            this.parseComposedMessageErrorListener = parseComposedMessageErrorListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            errorMessage = parseRepliedMessageErrorMessage(response);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (errorMessage == null) {
                parseComposedMessageErrorListener.noError();
            } else {
                parseComposedMessageErrorListener.error(errorMessage);
            }
        }
    }

    @Nullable
    private static String parseRepliedMessageErrorMessage(String response) {
        try {
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
                    return errorString.substring(0, 1).toUpperCase() + errorString.substring(1);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public interface ParseMessageAsyncTaskListener {
        void parseSuccess(ArrayList<Message> messages, @Nullable String after);
    }

    public interface ParseSentMessageAsyncTaskListener {
        void parseSuccess(Message message);
        void parseFailed(String errorMessage);
    }

    public interface ParseComposedMessageErrorListener {
        void noError();
        void error(String errorMessage);
    }
}

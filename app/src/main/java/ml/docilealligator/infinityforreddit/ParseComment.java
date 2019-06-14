package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

class ParseComment {
    interface ParseCommentListener {
        void onParseCommentSuccess(List<?> commentData, String parentId, ArrayList<String> children);
        void onParseCommentFailed();
    }

    interface ParseMoreCommentBasicInfoListener {
        void onParseMoreCommentBasicInfoSuccess(String commaSeparatedChildrenId);
        void onParseMoreCommentBasicInfoFailed();
    }

    interface ParseSentCommentListener {
        void onParseSentCommentSuccess(CommentData commentData);
        void onParseSentCommentFailed();
    }

    static void parseComment(String response, ArrayList<CommentData> commentData, Locale locale,
                             boolean isPost, int parentDepth, ParseCommentListener parseCommentListener) {
        try {
            JSONArray childrenArray = new JSONArray(response);

            if(isPost) {
                childrenArray = childrenArray.getJSONObject(1).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
            } else {
                childrenArray = childrenArray.getJSONObject(1).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY)
                        .getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY).getJSONObject(JSONUtils.REPLIES_KEY)
                        .getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
            }
            new ParseCommentAsyncTask(childrenArray, commentData, locale, parentDepth, parseCommentListener).execute();
        } catch (JSONException e) {
            e.printStackTrace();
            if(e.getMessage() != null) {
                Log.i("comment json error", e.getMessage());
            }
            parseCommentListener.onParseCommentFailed();
        }
    }

    static void parseMoreCommentBasicInfo(String response, ParseMoreCommentBasicInfoListener parseMoreCommentBasicInfoListener) {
        new ParseMoreCommentBasicInfoAsyncTask(response, parseMoreCommentBasicInfoListener).execute();
    }

    static void parseMoreComment(String response, ArrayList<CommentData> commentData, Locale locale,
                                 int parentDepth, ParseCommentListener parseCommentListener) {
        try {
            JSONArray childrenArray = new JSONObject(response).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
            new ParseCommentAsyncTask(childrenArray, commentData, locale, parentDepth, parseCommentListener).execute();
        } catch (JSONException e) {
            e.printStackTrace();
            if(e.getMessage() != null) {
                Log.i("comment json error", e.getMessage());
            }
            parseCommentListener.onParseCommentFailed();
        }
    }

    static void parseSentComment(String response, int parentDepth, Locale locale,
                                 ParseSentCommentListener parseSentCommentListener) {
        new ParseSentCommentAsyncTask(response, parentDepth, locale, parseSentCommentListener).execute();
    }

    private static class ParseCommentAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONArray comments;
        private ArrayList<CommentData> commentData;
        private ArrayList<CommentData> newcommentData;
        private ArrayList<String> children;
        private Locale locale;
        private int parentDepth;
        private ParseCommentListener parseCommentListener;
        private boolean parseFailed;
        private String parentId;

        ParseCommentAsyncTask(JSONArray comments, ArrayList<CommentData> commentData, Locale locale,
                              int parentDepth, ParseCommentListener parseCommentListener){
            this.comments = comments;
            this.commentData = commentData;
            newcommentData = new ArrayList<>();
            children = new ArrayList<>();
            this.locale = locale;
            this.parentDepth = parentDepth;
            parseFailed = false;
            this.parseCommentListener = parseCommentListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                int actualCommentLength;

                if(comments.length() == 0) {
                    return null;
                }

                JSONObject more = comments.getJSONObject(comments.length() - 1).getJSONObject(JSONUtils.DATA_KEY);

                //Maybe children contain only comments and no more info
                if(more.has(JSONUtils.COUNT_KEY)) {
                    JSONArray childrenArray = more.getJSONArray(JSONUtils.CHILDREN_KEY);

                    parentId = more.getString(JSONUtils.PARENT_ID_KEY);
                    for(int i = 0; i < childrenArray.length(); i++) {
                        children.add(childrenArray.getString(i));
                    }

                    actualCommentLength = comments.length() - 1;
                } else {
                    actualCommentLength = comments.length();
                }

                for (int i = 0; i < actualCommentLength; i++) {
                    JSONObject data = comments.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
                    newcommentData.add(parseSingleComment(data, parentDepth, locale));
                }
            } catch (JSONException e) {
                parseFailed = true;
                if(e.getMessage() != null) {
                    Log.i("parse comment error", e.getMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!parseFailed) {
                commentData.addAll(newcommentData);
                parseCommentListener.onParseCommentSuccess(commentData, parentId, children);
            } else {
                parseCommentListener.onParseCommentFailed();
            }
        }
    }

    private static class ParseMoreCommentBasicInfoAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONArray children;
        private StringBuilder commaSeparatedChildren;
        private ParseMoreCommentBasicInfoListener parseMoreCommentBasicInfoListener;
        private boolean parseFailed;

        ParseMoreCommentBasicInfoAsyncTask(String response, ParseMoreCommentBasicInfoListener parseMoreCommentBasicInfoListener) {
            this.parseMoreCommentBasicInfoListener = parseMoreCommentBasicInfoListener;
            try {
                children = new JSONObject(response).getJSONObject(JSONUtils.JSON_KEY)
                        .getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.THINGS_KEY);
                commaSeparatedChildren = new StringBuilder();
            } catch (JSONException e) {
                parseMoreCommentBasicInfoListener.onParseMoreCommentBasicInfoFailed();
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                for(int i = 0; i < children.length(); i++) {
                    commaSeparatedChildren.append(children.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.ID_KEY));
                    commaSeparatedChildren.append(",");
                }
                commaSeparatedChildren.deleteCharAt(commaSeparatedChildren.length() - 1);
                parseFailed = false;
            } catch (JSONException e) {
                parseFailed = true;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(!parseFailed) {
                parseMoreCommentBasicInfoListener.onParseMoreCommentBasicInfoSuccess(commaSeparatedChildren.toString());
            } else {
                parseMoreCommentBasicInfoListener.onParseMoreCommentBasicInfoFailed();
            }
        }
    }

    private static class ParseSentCommentAsyncTask extends AsyncTask<Void, Void, Void> {
        private String response;
        private int parentDepth;
        private Locale locale;
        private ParseSentCommentListener parseSentCommentListener;
        private boolean parseFailed;
        private CommentData commentData;

        ParseSentCommentAsyncTask(String response, int parentDepth, Locale locale, ParseSentCommentListener parseSentCommentListener) {
            this.response = response;
            this.parentDepth = parentDepth;
            this.locale = locale;
            this.parseSentCommentListener = parseSentCommentListener;
            parseFailed = false;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject sentCommentData = new JSONObject(response);
                commentData = parseSingleComment(sentCommentData, parentDepth, locale);
            } catch (JSONException e) {
                e.printStackTrace();
                parseFailed = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(parseFailed) {
                parseSentCommentListener.onParseSentCommentFailed();
            } else {
                parseSentCommentListener.onParseSentCommentSuccess(commentData);
            }
        }
    }

    private static CommentData parseSingleComment(JSONObject singleCommentData, int parentDepth, Locale locale) throws JSONException {
        String id = singleCommentData.getString(JSONUtils.ID_KEY);
        String fullName = singleCommentData.getString(JSONUtils.NAME_KEY);
        String author = singleCommentData.getString(JSONUtils.AUTHOR_KEY);
        boolean isSubmitter = singleCommentData.getBoolean(JSONUtils.IS_SUBMITTER_KEY);
        String commentContent = "";
        if(!singleCommentData.isNull(JSONUtils.BODY_HTML_KEY)) {
            commentContent = singleCommentData.getString(JSONUtils.BODY_HTML_KEY).trim();
        }
        String permalink = singleCommentData.getString(JSONUtils.PERMALINK_KEY);
        int score = singleCommentData.getInt(JSONUtils.SCORE_KEY);
        long submitTime = singleCommentData.getLong(JSONUtils.CREATED_UTC_KEY) * 1000;
        boolean scoreHidden = singleCommentData.getBoolean(JSONUtils.SCORE_HIDDEN_KEY);

        Calendar submitTimeCalendar = Calendar.getInstance();
        submitTimeCalendar.setTimeInMillis(submitTime);
        String formattedSubmitTime = new SimpleDateFormat("MMM d, YYYY, HH:mm",
                locale).format(submitTimeCalendar.getTime());

        int depth;
        if(singleCommentData.has(JSONUtils.DEPTH_KEY)) {
            depth = singleCommentData.getInt(JSONUtils.DEPTH_KEY) + parentDepth;
        } else {
            depth = parentDepth;
        }
        boolean collapsed = singleCommentData.getBoolean(JSONUtils.COLLAPSED_KEY);
        boolean hasReply = !(singleCommentData.get(JSONUtils.REPLIES_KEY) instanceof String);

        return new CommentData(id, fullName, author, formattedSubmitTime, commentContent, score,
                isSubmitter, permalink, depth, collapsed, hasReply, scoreHidden);
    }
}

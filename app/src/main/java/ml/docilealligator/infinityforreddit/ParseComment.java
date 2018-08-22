package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

class ParseComment {
    interface ParseCommentListener {
        void onParseCommentSuccess(ArrayList<CommentData> commentData, int moreCommentCount);
        void onParseCommentFail();
    }

    private Context mContext;
    private ParseCommentListener mParseCommentListener;

    void parseComment(Context context, String response, ArrayList<CommentData> commentData, ParseCommentListener parseCommentListener) {
        mParseCommentListener = parseCommentListener;
        mContext = context;
        new ParseCommentAsyncTask(response, commentData).execute();
    }

    private class ParseCommentAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONArray jsonResponse;
        private ArrayList<CommentData> commentData;
        private ArrayList<CommentData> newcommentData;
        private boolean parseFailed;
        int moreCommentCount;

        ParseCommentAsyncTask(String response, ArrayList<CommentData> commentData){
            try {
                jsonResponse = new JSONArray(response);
                this.commentData = commentData;
                newcommentData = new ArrayList<>();
                parseFailed = false;
            } catch (JSONException e) {
                Log.i("comment json error", e.getMessage());
                mParseCommentListener.onParseCommentFail();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                moreCommentCount = 0;
                int actualCommentLength;

                JSONArray allComments = jsonResponse.getJSONObject(1).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                if(allComments.length() == 0) {
                    return null;
                }

                JSONObject more = allComments.getJSONObject(allComments.length() - 1).getJSONObject(JSONUtils.DATA_KEY);

                //Maybe children contain only comments and no more info
                if(more.has(JSONUtils.COUNT_KEY)) {
                    moreCommentCount = more.getInt(JSONUtils.COUNT_KEY);
                    actualCommentLength = allComments.length() - 1;
                } else {
                    actualCommentLength = allComments.length();
                }

                for (int i = 0; i < actualCommentLength; i++) {
                    JSONObject data = allComments.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
                    String fullName = data.getString(JSONUtils.LINK_ID);
                    String author = data.getString(JSONUtils.AUTHOR_KEY);
                    boolean isSubmitter = data.getBoolean(JSONUtils.IS_SUBMITTER_KEY);
                    String commentContent = "";
                    if(!data.isNull(JSONUtils.BODY_HTML_KEY)) {
                        commentContent = data.getString(JSONUtils.BODY_HTML_KEY);
                    }
                    String permalink = data.getString(JSONUtils.PERMALINK_KEY);
                    int score = data.getInt(JSONUtils.SCORE_KEY);
                    long submitTime = data.getLong(JSONUtils.CREATED_UTC_KEY) * 1000;
                    boolean scoreHidden = data.getBoolean(JSONUtils.SCORE_HIDDEN_KEY);

                    Calendar submitTimeCalendar = Calendar.getInstance();
                    submitTimeCalendar.setTimeInMillis(submitTime);
                    String formattedSubmitTime = new SimpleDateFormat("MMM d, YYYY, HH:mm",
                            mContext.getResources().getConfiguration().locale).format(submitTimeCalendar.getTime());

                    int depth = data.getInt(JSONUtils.DEPTH_KEY);
                    boolean collapsed = data.getBoolean(JSONUtils.COLLAPSED_KEY);
                    boolean hasReply = !(data.get(JSONUtils.REPLIES_KEY) instanceof String);

                    newcommentData.add(new CommentData(fullName, author, formattedSubmitTime, commentContent, score, isSubmitter, permalink, depth, collapsed, hasReply, scoreHidden));
                }
            } catch (JSONException e) {
                parseFailed = true;
                Log.i("parse comment error", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!parseFailed) {
                commentData.addAll(newcommentData);
                mParseCommentListener.onParseCommentSuccess(commentData, moreCommentCount);
            } else {
                mParseCommentListener.onParseCommentFail();
            }
        }
    }
}

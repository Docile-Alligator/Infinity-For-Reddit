package ml.docilealligator.infinityforreddit;

import static ml.docilealligator.infinityforreddit.CommentData.VOTE_TYPE_DOWNVOTE;
import static ml.docilealligator.infinityforreddit.CommentData.VOTE_TYPE_NO_VOTE;
import static ml.docilealligator.infinityforreddit.CommentData.VOTE_TYPE_UPVOTE;

import android.os.AsyncTask;
import android.text.Html;
import androidx.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class ParseComment {

  static void parseComment(String response, ArrayList<CommentData> commentData, Locale locale,
      ParseCommentListener parseCommentListener) {
    try {
      JSONArray childrenArray = new JSONArray(response);
      String parentId = childrenArray.getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY)
          .getJSONArray(JSONUtils.CHILDREN_KEY)
          .getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.NAME_KEY);
      childrenArray = childrenArray.getJSONObject(1).getJSONObject(JSONUtils.DATA_KEY)
          .getJSONArray(JSONUtils.CHILDREN_KEY);

      new ParseCommentAsyncTask(childrenArray, commentData, locale, parentId, 0,
          parseCommentListener).execute();
    } catch (JSONException e) {
      e.printStackTrace();
      parseCommentListener.onParseCommentFailed();
    }
  }

  static void parseMoreComment(String response, ArrayList<CommentData> commentData, Locale locale,
      int depth, ParseCommentListener parseCommentListener) {
    try {
      JSONArray childrenArray = new JSONObject(response).getJSONObject(JSONUtils.DATA_KEY)
          .getJSONArray(JSONUtils.CHILDREN_KEY);
      new ParseCommentAsyncTask(childrenArray, commentData, locale, null, depth,
          parseCommentListener).execute();
    } catch (JSONException e) {
      e.printStackTrace();
      parseCommentListener.onParseCommentFailed();
    }
  }

  static void parseSentComment(String response, int depth, Locale locale,
      ParseSentCommentListener parseSentCommentListener) {
    new ParseSentCommentAsyncTask(response, depth, locale, parseSentCommentListener).execute();
  }

  private static void parseCommentRecursion(JSONArray comments,
      ArrayList<CommentData> newCommentData,
      ArrayList<String> moreChildrenFullnames, int depth, Locale locale) throws JSONException {
    int actualCommentLength;

    if (comments.length() == 0) {
      return;
    }

    JSONObject more = comments.getJSONObject(comments.length() - 1)
        .getJSONObject(JSONUtils.DATA_KEY);

    //Maybe moreChildrenFullnames contain only commentsJSONArray and no more info
    if (more.has(JSONUtils.COUNT_KEY)) {
      JSONArray childrenArray = more.getJSONArray(JSONUtils.CHILDREN_KEY);

      for (int i = 0; i < childrenArray.length(); i++) {
        moreChildrenFullnames.add("t1_" + childrenArray.getString(i));
      }

      actualCommentLength = comments.length() - 1;
    } else {
      actualCommentLength = comments.length();
    }

    for (int i = 0; i < actualCommentLength; i++) {
      JSONObject data = comments.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
      CommentData singleComment = parseSingleComment(data, depth, locale);

      if (data.get(JSONUtils.REPLIES_KEY) instanceof JSONObject) {
        JSONArray childrenArray = data.getJSONObject(JSONUtils.REPLIES_KEY)
            .getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
        ArrayList<CommentData> children = new ArrayList<>();
        ArrayList<String> nextMoreChildrenFullnames = new ArrayList<>();
        parseCommentRecursion(childrenArray, children, nextMoreChildrenFullnames,
            singleComment.getDepth(),
            locale);
        singleComment.addChildren(children);
        singleComment.setMoreChildrenFullnames(nextMoreChildrenFullnames);
      }

      newCommentData.add(singleComment);
    }
  }

  private static void expandChildren(ArrayList<CommentData> comments,
      ArrayList<CommentData> visibleComments) {
    for (CommentData c : comments) {
      visibleComments.add(c);
      if (c.hasReply()) {
        c.setExpanded(true);
        expandChildren(c.getChildren(), visibleComments);
      }
      if (c.hasMoreChildrenFullnames() && c.getMoreChildrenFullnames().size() > c
          .getMoreChildrenStartingIndex()) {
        //Add a load more placeholder
        CommentData placeholder = new CommentData(c.getFullName(), c.getDepth() + 1);
        visibleComments.add(placeholder);
        c.addChild(placeholder, c.getChildren().size());
      }
    }
  }

  static CommentData parseSingleComment(JSONObject singleCommentData, int depth, Locale locale)
      throws JSONException {
    String id = singleCommentData.getString(JSONUtils.ID_KEY);
    String fullName = singleCommentData.getString(JSONUtils.NAME_KEY);
    String author = singleCommentData.getString(JSONUtils.AUTHOR_KEY);
    String linkAuthor = singleCommentData.has(JSONUtils.LINK_AUTHOR_KEY) ? singleCommentData
        .getString(JSONUtils.LINK_AUTHOR_KEY) : null;
    String linkId = singleCommentData.getString(JSONUtils.LINK_ID_KEY).substring(3);
    String subredditName = singleCommentData.getString(JSONUtils.SUBREDDIT_KEY);
    String parentId = singleCommentData.getString(JSONUtils.PARENT_ID_KEY);
    boolean isSubmitter = singleCommentData.getBoolean(JSONUtils.IS_SUBMITTER_KEY);
    String commentContent = "";
    if (!singleCommentData.isNull(JSONUtils.BODY_KEY)) {
      commentContent = Utils
          .addSubredditAndUserLink(singleCommentData.getString(JSONUtils.BODY_KEY).trim());
    }
    String permalink = Html.fromHtml(singleCommentData.getString(JSONUtils.PERMALINK_KEY))
        .toString();
    int score = singleCommentData.getInt(JSONUtils.SCORE_KEY);
    int voteType;
    if (singleCommentData.isNull(JSONUtils.LIKES_KEY)) {
      voteType = VOTE_TYPE_NO_VOTE;
    } else {
      voteType =
          singleCommentData.getBoolean(JSONUtils.LIKES_KEY) ? VOTE_TYPE_UPVOTE : VOTE_TYPE_DOWNVOTE;
      score -= voteType;
    }
    long submitTime = singleCommentData.getLong(JSONUtils.CREATED_UTC_KEY) * 1000;
    boolean scoreHidden = singleCommentData.getBoolean(JSONUtils.SCORE_HIDDEN_KEY);

    Calendar submitTimeCalendar = Calendar.getInstance();
    submitTimeCalendar.setTimeInMillis(submitTime);
    String formattedSubmitTime = new SimpleDateFormat("MMM d, yyyy, HH:mm",
        locale).format(submitTimeCalendar.getTime());

    if (singleCommentData.has(JSONUtils.DEPTH_KEY)) {
      depth = singleCommentData.getInt(JSONUtils.DEPTH_KEY);
    }

    boolean collapsed = singleCommentData.getBoolean(JSONUtils.COLLAPSED_KEY);
    boolean hasReply = !(singleCommentData.get(JSONUtils.REPLIES_KEY) instanceof String);

    return new CommentData(id, fullName, author, linkAuthor, formattedSubmitTime, commentContent,
        linkId, subredditName, parentId, score, voteType, isSubmitter, permalink, depth, collapsed,
        hasReply, scoreHidden);
  }

  @Nullable
  private static String parseSentCommentErrorMessage(String response) {
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

  interface ParseCommentListener {

    void onParseCommentSuccess(ArrayList<CommentData> expandedComments, String parentId,
        ArrayList<String> moreChildrenFullnames);

    void onParseCommentFailed();
  }

  interface ParseSentCommentListener {

    void onParseSentCommentSuccess(CommentData commentData);

    void onParseSentCommentFailed(@Nullable String errorMessage);
  }

  private static class ParseCommentAsyncTask extends AsyncTask<Void, Void, Void> {

    private final JSONArray commentsJSONArray;
    private final ArrayList<CommentData> comments;
    private final ArrayList<CommentData> newComments;
    private final ArrayList<CommentData> expandedNewComments;
    private final ArrayList<String> moreChildrenFullnames;
    private final Locale locale;
    private final String parentId;
    private final int depth;
    private final ParseCommentListener parseCommentListener;
    private boolean parseFailed;

    ParseCommentAsyncTask(JSONArray commentsJSONArray, ArrayList<CommentData> comments,
        Locale locale,
        @Nullable String parentId, int depth, ParseCommentListener parseCommentListener) {
      this.commentsJSONArray = commentsJSONArray;
      this.comments = comments;
      newComments = new ArrayList<>();
      expandedNewComments = new ArrayList<>();
      moreChildrenFullnames = new ArrayList<>();
      this.locale = locale;
      this.parentId = parentId;
      this.depth = depth;
      parseFailed = false;
      this.parseCommentListener = parseCommentListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
      try {
        parseCommentRecursion(commentsJSONArray, newComments, moreChildrenFullnames, depth, locale);
        expandChildren(newComments, expandedNewComments);
      } catch (JSONException e) {
        parseFailed = true;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      if (!parseFailed) {
        comments.addAll(expandedNewComments);
        parseCommentListener.onParseCommentSuccess(comments, parentId, moreChildrenFullnames);
      } else {
        parseCommentListener.onParseCommentFailed();
      }
    }
  }

  private static class ParseSentCommentAsyncTask extends AsyncTask<Void, Void, Void> {

    private final String response;
    private final int depth;
    private final Locale locale;
    private final ParseSentCommentListener parseSentCommentListener;
    private boolean parseFailed;
    private String errorMessage;
    private CommentData commentData;

    ParseSentCommentAsyncTask(String response, int depth, Locale locale,
        ParseSentCommentListener parseSentCommentListener) {
      this.response = response;
      this.depth = depth;
      this.locale = locale;
      this.parseSentCommentListener = parseSentCommentListener;
      parseFailed = false;
    }

    @Override
    protected Void doInBackground(Void... voids) {
      try {
        JSONObject sentCommentData = new JSONObject(response);
        commentData = parseSingleComment(sentCommentData, depth, locale);
      } catch (JSONException e) {
        e.printStackTrace();
        errorMessage = parseSentCommentErrorMessage(response);
        parseFailed = true;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      if (parseFailed) {
        parseSentCommentListener.onParseSentCommentFailed(errorMessage);
      } else {
        parseSentCommentListener.onParseSentCommentSuccess(commentData);
      }
    }
  }
}

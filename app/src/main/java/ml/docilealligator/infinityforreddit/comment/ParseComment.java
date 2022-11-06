package ml.docilealligator.infinityforreddit.comment;

import static ml.docilealligator.infinityforreddit.comment.Comment.VOTE_TYPE_DOWNVOTE;
import static ml.docilealligator.infinityforreddit.comment.Comment.VOTE_TYPE_NO_VOTE;
import static ml.docilealligator.infinityforreddit.comment.Comment.VOTE_TYPE_UPVOTE;

import android.os.Handler;
import android.text.Html;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class ParseComment {
    public static void parseComment(Executor executor, Handler handler, String response,
                                    ArrayList<Comment> commentData, boolean expandChildren,
                                    ParseCommentListener parseCommentListener) {
        executor.execute(() -> {
            try {
                JSONArray childrenArray = new JSONArray(response);
                String parentId = childrenArray.getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY)
                        .getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.NAME_KEY);
                childrenArray = childrenArray.getJSONObject(1).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);

                ArrayList<Comment> expandedNewComments = new ArrayList<>();
                ArrayList<String> moreChildrenFullnames = new ArrayList<>();
                ArrayList<Comment> newComments = new ArrayList<>();

                parseCommentRecursion(childrenArray, newComments, moreChildrenFullnames, 0);
                expandChildren(newComments, expandedNewComments, expandChildren);

                if (expandChildren) {
                    commentData.addAll(expandedNewComments);
                } else {
                    commentData.addAll(newComments);
                }

                handler.post(() -> parseCommentListener.onParseCommentSuccess(commentData, parentId, moreChildrenFullnames));
            } catch (JSONException e) {
                e.printStackTrace();
                handler.post(parseCommentListener::onParseCommentFailed);
            }
        });
    }

    static void parseMoreComment(Executor executor, Handler handler, String response,
                                 ArrayList<Comment> commentData, int depth, boolean expandChildren,
                                 ParseCommentListener parseCommentListener) {
        executor.execute(() -> {
            try {
                JSONArray childrenArray = new JSONObject(response).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);

                ArrayList<Comment> newComments = new ArrayList<>();
                ArrayList<Comment> expandedNewComments = new ArrayList<>();
                ArrayList<String> moreChildrenFullnames = new ArrayList<>();

                parseCommentRecursion(childrenArray, newComments, moreChildrenFullnames, depth);
                expandChildren(newComments, expandedNewComments, expandChildren);

                if (expandChildren) {
                    commentData.addAll(expandedNewComments);
                } else {
                    commentData.addAll(newComments);
                }

                handler.post(() -> parseCommentListener.onParseCommentSuccess(commentData, null, moreChildrenFullnames));
            } catch (JSONException e) {
                e.printStackTrace();
                handler.post(parseCommentListener::onParseCommentFailed);
            }
        });
    }

    static void parseSentComment(Executor executor, Handler handler, String response, int depth,
                                 ParseSentCommentListener parseSentCommentListener) {
        executor.execute(() -> {
            try {
                JSONObject sentCommentData = new JSONObject(response);
                Comment comment = parseSingleComment(sentCommentData, depth);

                handler.post(() -> parseSentCommentListener.onParseSentCommentSuccess(comment));
            } catch (JSONException e) {
                e.printStackTrace();
                String errorMessage = parseSentCommentErrorMessage(response);
                handler.post(() -> parseSentCommentListener.onParseSentCommentFailed(errorMessage));
            }
        });
    }

    private static void parseCommentRecursion(JSONArray comments, ArrayList<Comment> newCommentData,
                                              ArrayList<String> moreChildrenFullnames, int depth) throws JSONException {
        int actualCommentLength;

        if (comments.length() == 0) {
            return;
        }

        JSONObject more = comments.getJSONObject(comments.length() - 1).getJSONObject(JSONUtils.DATA_KEY);

        //Maybe moreChildrenFullnames contain only commentsJSONArray and no more info
        if (more.has(JSONUtils.COUNT_KEY)) {
            JSONArray childrenArray = more.getJSONArray(JSONUtils.CHILDREN_KEY);

            for (int i = 0; i < childrenArray.length(); i++) {
                moreChildrenFullnames.add("t1_" + childrenArray.getString(i));
            }

            actualCommentLength = comments.length() - 1;

            if (moreChildrenFullnames.isEmpty() && comments.getJSONObject(comments.length() - 1).getString(JSONUtils.KIND_KEY).equals(JSONUtils.KIND_VALUE_MORE)) {
                newCommentData.add(new Comment(more.getString(JSONUtils.PARENT_ID_KEY), more.getInt(JSONUtils.DEPTH_KEY), Comment.PLACEHOLDER_CONTINUE_THREAD));
                return;
            }
        } else {
            actualCommentLength = comments.length();
        }

        for (int i = 0; i < actualCommentLength; i++) {
            JSONObject data = comments.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
            Comment singleComment = parseSingleComment(data, depth);

            if (data.get(JSONUtils.REPLIES_KEY) instanceof JSONObject) {
                JSONArray childrenArray = data.getJSONObject(JSONUtils.REPLIES_KEY)
                        .getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                ArrayList<Comment> children = new ArrayList<>();
                ArrayList<String> nextMoreChildrenFullnames = new ArrayList<>();
                parseCommentRecursion(childrenArray, children, nextMoreChildrenFullnames, singleComment.getDepth());
                singleComment.addChildren(children);
                singleComment.setMoreChildrenFullnames(nextMoreChildrenFullnames);
                singleComment.setChildCount(getChildCount(singleComment));
            }

            newCommentData.add(singleComment);
        }
    }

    private static int getChildCount(Comment comment) {
        if (comment.getChildren() == null) {
            return 0;
        }
        int count = 0;
        for (Comment c : comment.getChildren()) {
            count += getChildCount(c);
        }
        return comment.getChildren().size() + count;
    }

    private static void expandChildren(ArrayList<Comment> comments, ArrayList<Comment> visibleComments,
                                       boolean setExpanded) {
        for (Comment c : comments) {
            visibleComments.add(c);
            if (c.hasReply()) {
                if (setExpanded) {
                    c.setExpanded(true);
                }
                expandChildren(c.getChildren(), visibleComments, setExpanded);
            } else {
                c.setExpanded(true);
            }
            if (c.hasMoreChildrenFullnames() && c.getMoreChildrenFullnames().size() > c.getMoreChildrenStartingIndex()) {
                //Add a load more placeholder
                Comment placeholder = new Comment(c.getFullName(), c.getDepth() + 1, Comment.PLACEHOLDER_LOAD_MORE_COMMENTS);
                visibleComments.add(placeholder);
                c.addChild(placeholder, c.getChildren().size());
            }
        }
    }

    static Comment parseSingleComment(JSONObject singleCommentData, int depth) throws JSONException {
        String id = singleCommentData.getString(JSONUtils.ID_KEY);
        String fullName = singleCommentData.getString(JSONUtils.NAME_KEY);
        String author = singleCommentData.getString(JSONUtils.AUTHOR_KEY);
        StringBuilder authorFlairHTMLBuilder = new StringBuilder();
        if (singleCommentData.has(JSONUtils.AUTHOR_FLAIR_RICHTEXT_KEY)) {
            JSONArray flairArray = singleCommentData.getJSONArray(JSONUtils.AUTHOR_FLAIR_RICHTEXT_KEY);
            for (int i = 0; i < flairArray.length(); i++) {
                JSONObject flairObject = flairArray.getJSONObject(i);
                String e = flairObject.getString(JSONUtils.E_KEY);
                if (e.equals("text")) {
                    authorFlairHTMLBuilder.append(Html.escapeHtml(flairObject.getString(JSONUtils.T_KEY)));
                } else if (e.equals("emoji")) {
                    authorFlairHTMLBuilder.append("<img src=\"").append(Html.escapeHtml(flairObject.getString(JSONUtils.U_KEY))).append("\">");
                }
            }
        }
        String authorFlair = singleCommentData.isNull(JSONUtils.AUTHOR_FLAIR_TEXT_KEY) ? "" : singleCommentData.getString(JSONUtils.AUTHOR_FLAIR_TEXT_KEY);
        String linkAuthor = singleCommentData.has(JSONUtils.LINK_AUTHOR_KEY) ? singleCommentData.getString(JSONUtils.LINK_AUTHOR_KEY) : null;
        String linkId = singleCommentData.getString(JSONUtils.LINK_ID_KEY).substring(3);
        String subredditName = singleCommentData.getString(JSONUtils.SUBREDDIT_KEY);
        String parentId = singleCommentData.getString(JSONUtils.PARENT_ID_KEY);
        boolean isSubmitter = singleCommentData.getBoolean(JSONUtils.IS_SUBMITTER_KEY);
        String distinguished = singleCommentData.getString(JSONUtils.DISTINGUISHED_KEY);
        String commentMarkdown = "";
        if (!singleCommentData.isNull(JSONUtils.BODY_KEY)) {
            commentMarkdown = Utils.parseInlineGifInComments(Utils.modifyMarkdown(singleCommentData.getString(JSONUtils.BODY_KEY).trim()));
            if (!singleCommentData.isNull(JSONUtils.MEDIA_METADATA_KEY)) {
                JSONObject mediaMetadataObject = singleCommentData.getJSONObject(JSONUtils.MEDIA_METADATA_KEY);
                commentMarkdown = Utils.parseInlineEmotes(commentMarkdown, mediaMetadataObject);
            }
        }
        String commentRawText = Utils.trimTrailingWhitespace(
                Html.fromHtml(singleCommentData.getString(JSONUtils.BODY_HTML_KEY))).toString();
        String permalink = Html.fromHtml(singleCommentData.getString(JSONUtils.PERMALINK_KEY)).toString();
        StringBuilder awardingsBuilder = new StringBuilder();
        JSONArray awardingsArray = singleCommentData.getJSONArray(JSONUtils.ALL_AWARDINGS_KEY);
        for (int i = 0; i < awardingsArray.length(); i++) {
            JSONObject award = awardingsArray.getJSONObject(i);
            int count = award.getInt(JSONUtils.COUNT_KEY);
            JSONArray icons = award.getJSONArray(JSONUtils.RESIZED_ICONS_KEY);
            if (icons.length() > 4) {
                String iconUrl = icons.getJSONObject(3).getString(JSONUtils.URL_KEY);
                awardingsBuilder.append("<img src=\"").append(Html.escapeHtml(iconUrl)).append("\"> ").append("x").append(count).append(" ");
            } else if (icons.length() > 0) {
                String iconUrl = icons.getJSONObject(icons.length() - 1).getString(JSONUtils.URL_KEY);
                awardingsBuilder.append("<img src=\"").append(Html.escapeHtml(iconUrl)).append("\"> ").append("x").append(count).append(" ");
            }
        }
        int score = singleCommentData.getInt(JSONUtils.SCORE_KEY);
        int voteType;
        if (singleCommentData.isNull(JSONUtils.LIKES_KEY)) {
            voteType = VOTE_TYPE_NO_VOTE;
        } else {
            voteType = singleCommentData.getBoolean(JSONUtils.LIKES_KEY) ? VOTE_TYPE_UPVOTE : VOTE_TYPE_DOWNVOTE;
            score -= voteType;
        }
        long submitTime = singleCommentData.getLong(JSONUtils.CREATED_UTC_KEY) * 1000;
        boolean scoreHidden = singleCommentData.getBoolean(JSONUtils.SCORE_HIDDEN_KEY);
        boolean saved = singleCommentData.getBoolean(JSONUtils.SAVED_KEY);

        if (singleCommentData.has(JSONUtils.DEPTH_KEY)) {
            depth = singleCommentData.getInt(JSONUtils.DEPTH_KEY);
        }

        boolean collapsed = singleCommentData.getBoolean(JSONUtils.COLLAPSED_KEY);
        boolean hasReply = !(singleCommentData.get(JSONUtils.REPLIES_KEY) instanceof String);

        return new Comment(id, fullName, author, authorFlair, authorFlairHTMLBuilder.toString(),
                linkAuthor, submitTime, commentMarkdown, commentRawText,
                linkId, subredditName, parentId, score, voteType, isSubmitter, distinguished,
                permalink, awardingsBuilder.toString(), depth, collapsed, hasReply, scoreHidden, saved);
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

    public interface ParseCommentListener {
        void onParseCommentSuccess(ArrayList<Comment> expandedComments, String parentId,
                                   ArrayList<String> moreChildrenFullnames);

        void onParseCommentFailed();
    }

    interface ParseSentCommentListener {
        void onParseSentCommentSuccess(Comment comment);

        void onParseSentCommentFailed(@Nullable String errorMessage);
    }
}

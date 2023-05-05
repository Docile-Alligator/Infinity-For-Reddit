package ml.docilealligator.infinityforreddit.comment;

import static ml.docilealligator.infinityforreddit.comment.Comment.VOTE_TYPE_DOWNVOTE;
import static ml.docilealligator.infinityforreddit.comment.Comment.VOTE_TYPE_NO_VOTE;
import static ml.docilealligator.infinityforreddit.comment.Comment.VOTE_TYPE_UPVOTE;

import android.os.Handler;
import android.text.Html;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.checkerframework.checker.units.qual.A;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class ParseComment {
    public static void parseComment(Executor executor, Handler handler, String response,
                                    boolean expandChildren,
                                    ParseCommentListener parseCommentListener) {
        executor.execute(() -> {
            try {
                JSONArray childrenArray = new JSONArray(response);
                String parentId = childrenArray.getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY)
                        .getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.NAME_KEY);
                childrenArray = childrenArray.getJSONObject(1).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);

                ArrayList<Comment> expandedNewComments = new ArrayList<>();
                ArrayList<String> moreChildrenIds = new ArrayList<>();
                ArrayList<Comment> newComments = new ArrayList<>();

                parseCommentRecursion(childrenArray, newComments, moreChildrenIds, 0);
                expandChildren(newComments, expandedNewComments, expandChildren);

                ArrayList<Comment> commentData;
                if (expandChildren) {
                    commentData = expandedNewComments;
                } else {
                    commentData = newComments;
                }

                handler.post(() -> parseCommentListener.onParseCommentSuccess(newComments, commentData, parentId, moreChildrenIds));
            } catch (JSONException e) {
                e.printStackTrace();
                handler.post(parseCommentListener::onParseCommentFailed);
            }
        });
    }

    static void parseMoreComment(Executor executor, Handler handler, String response, boolean expandChildren,
                                 ParseCommentListener parseCommentListener) {
        executor.execute(() -> {
            try {
                JSONArray childrenArray = new JSONObject(response).getJSONObject(JSONUtils.JSON_KEY)
                        .getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.THINGS_KEY);

                ArrayList<Comment> newComments = new ArrayList<>();
                ArrayList<Comment> expandedNewComments = new ArrayList<>();
                ArrayList<String> moreChildrenIds = new ArrayList<>();

                // api response is a flat list of comments tree
                // process it in order and rebuild the tree
                for (int i = 0; i < childrenArray.length(); i++) {
                    JSONObject child = childrenArray.getJSONObject(i);
                    JSONObject childData = child.getJSONObject(JSONUtils.DATA_KEY);
                    if (child.getString(JSONUtils.KIND_KEY).equals(JSONUtils.KIND_VALUE_MORE)) {
                        String parentFullName = childData.getString(JSONUtils.PARENT_ID_KEY);
                        JSONArray childrenIds = childData.getJSONArray(JSONUtils.CHILDREN_KEY);

                        if (childrenIds.length() != 0) {
                            ArrayList<String> localMoreChildrenIds = new ArrayList<>(childrenIds.length());
                            for (int j = 0; j < childrenIds.length(); j++) {
                                localMoreChildrenIds.add(childrenIds.getString(j));
                            }

                            Comment parentComment = findCommentByFullName(newComments, parentFullName);
                            if (parentComment != null) {
                                parentComment.setHasReply(true);
                                parentComment.setMoreChildrenIds(localMoreChildrenIds);
                                parentComment.addChildren(new ArrayList<>()); // ensure children list is not null
                            } else {
                                // assume that it is parent of this call
                                moreChildrenIds.addAll(localMoreChildrenIds);
                            }
                        } else {
                            Comment continueThreadPlaceholder = new Comment(
                                    parentFullName,
                                    childData.getInt(JSONUtils.DEPTH_KEY),
                                    Comment.PLACEHOLDER_CONTINUE_THREAD
                            );

                            Comment parentComment = findCommentByFullName(newComments, parentFullName);
                            if (parentComment != null) {
                                parentComment.setHasReply(true);
                                parentComment.addChild(continueThreadPlaceholder, parentComment.getChildCount());
                                parentComment.setChildCount(parentComment.getChildCount() + 1);
                            } else {
                                // assume that it is parent of this call
                                newComments.add(continueThreadPlaceholder);
                            }
                        }
                    } else {
                        Comment comment = parseSingleComment(childData, 0);
                        String parentFullName = comment.getParentId();

                        Comment parentComment = findCommentByFullName(newComments, parentFullName);
                        if (parentComment != null) {
                            parentComment.setHasReply(true);
                            parentComment.addChild(comment, parentComment.getChildCount());
                            parentComment.setChildCount(parentComment.getChildCount() + 1);
                        } else {
                            // assume that it is parent of this call
                            newComments.add(comment);
                        }
                    }
                }

                updateChildrenCount(newComments);
                expandChildren(newComments, expandedNewComments, expandChildren);

                ArrayList<Comment> commentData;
                if (expandChildren) {
                    commentData = expandedNewComments;
                } else {
                    commentData = newComments;
                }

                handler.post(() -> parseCommentListener.onParseCommentSuccess(newComments, commentData, null, moreChildrenIds));
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
                                              ArrayList<String> moreChildrenIds, int depth) throws JSONException {
        int actualCommentLength;

        if (comments.length() == 0) {
            return;
        }

        JSONObject more = comments.getJSONObject(comments.length() - 1).getJSONObject(JSONUtils.DATA_KEY);

        //Maybe moreChildrenIds contain only commentsJSONArray and no more info
        if (more.has(JSONUtils.COUNT_KEY)) {
            JSONArray childrenArray = more.getJSONArray(JSONUtils.CHILDREN_KEY);

            for (int i = 0; i < childrenArray.length(); i++) {
                moreChildrenIds.add(childrenArray.getString(i));
            }

            actualCommentLength = comments.length() - 1;

            if (moreChildrenIds.isEmpty() && comments.getJSONObject(comments.length() - 1).getString(JSONUtils.KIND_KEY).equals(JSONUtils.KIND_VALUE_MORE)) {
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
                ArrayList<String> nextMoreChildrenIds = new ArrayList<>();
                parseCommentRecursion(childrenArray, children, nextMoreChildrenIds, singleComment.getDepth());
                singleComment.addChildren(children);
                singleComment.setMoreChildrenIds(nextMoreChildrenIds);
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
            if (c.hasMoreChildrenIds() && !c.getMoreChildrenIds().isEmpty()) {
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
            commentMarkdown = Utils.parseInlineGifInComments(Utils.modifyMarkdown(Utils.trimTrailingWhitespace(singleCommentData.getString(JSONUtils.BODY_KEY))));
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

        // this key can either be a bool (false) or a long (edited timestamp)
        long edited = singleCommentData.optLong(JSONUtils.EDITED_KEY) * 1000;

        return new Comment(id, fullName, author, authorFlair, authorFlairHTMLBuilder.toString(),
                linkAuthor, submitTime, commentMarkdown, commentRawText,
                linkId, subredditName, parentId, score, voteType, isSubmitter, distinguished,
                permalink, awardingsBuilder.toString(), depth, collapsed, hasReply, scoreHidden, saved, edited);
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

    @Nullable
    private static Comment findCommentByFullName(@NonNull List<Comment> comments, @NonNull String fullName) {
        for (Comment comment: comments) {
            if (comment.getFullName().equals(fullName) &&
                    comment.getPlaceholderType() == Comment.NOT_PLACEHOLDER) {
                return comment;
            }
            if (comment.getChildren() != null) {
                Comment result = findCommentByFullName(comment.getChildren(), fullName);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static void updateChildrenCount(@NonNull List<Comment> comments) {
        for (Comment comment: comments) {
            comment.setChildCount(getChildCount(comment));
            if (comment.getChildren() != null) {
                updateChildrenCount(comment.getChildren());
            }
        }
    }

    public interface ParseCommentListener {
        void onParseCommentSuccess(ArrayList<Comment> topLevelComments, ArrayList<Comment> expandedComments, String parentId,
                                   ArrayList<String> moreChildrenIds);

        void onParseCommentFailed();
    }

    interface ParseSentCommentListener {
        void onParseSentCommentSuccess(Comment comment);

        void onParseSentCommentFailed(@Nullable String errorMessage);
    }
}

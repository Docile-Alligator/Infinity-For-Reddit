package ml.docilealligator.infinityforreddit.user;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.viewmodels.ViewPostDetailActivityViewModel;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UserProfileImagesBatchLoader {
    public static final int BATCH_SIZE = 100;

    private final Executor mExecutor;
    private final Handler mHandler;
    private final RedditDataRoomDatabase mRedditDataRoomDatabase;
    private final Retrofit mRetrofit;
    private final Map<String, String> mAuthorFullNameToImageMap;
    private final Queue<Comment> mCommentQueue;
    private final Map<String, ViewPostDetailActivityViewModel.LoadIconListener> mAuthorFullNameToListenerMap;
    private final List<Comment> mCallingComments;
    private final Set<String> mLoadingAuthorFullNames;
    private final Object mImageMapLock = new Object();
    private final Object mCommentQueueLock = new Object();
    private final Object mListenerMapLock = new Object();
    private final Object mCallingCommentsLock = new Object();
    private final Object mLoadingSetLock = new Object();
    private boolean mIsLoadingBatch = false;

    public UserProfileImagesBatchLoader(Executor executor, Handler handler, RedditDataRoomDatabase redditDataRoomDatabase,
                                           Retrofit retrofit) {
        mExecutor = executor;
        mHandler = handler;
        mRedditDataRoomDatabase = redditDataRoomDatabase;
        mRetrofit = retrofit;
        mAuthorFullNameToImageMap = new HashMap<>();
        mCommentQueue = new LinkedList<>();
        mAuthorFullNameToListenerMap = new HashMap<>();
        mCallingComments = new LinkedList<>();
        mLoadingAuthorFullNames = new HashSet<>();
    }

    public void loadAuthorImages(List<Comment> comments, @NonNull ViewPostDetailActivityViewModel.LoadIconListener loadIconListener) {
        String authorFullName = comments.get(0).getAuthorFullName();
        synchronized (mImageMapLock) {
            if (mAuthorFullNameToImageMap.containsKey(authorFullName)) {
                loadIconListener.loadIconSuccess(authorFullName, mAuthorFullNameToImageMap.get(authorFullName));
                return;
            }
        }

        synchronized (mListenerMapLock) {
            mAuthorFullNameToListenerMap.put(authorFullName, loadIconListener);
        }

        synchronized (mCommentQueueLock) {
            mCommentQueue.addAll(comments);
        }

        synchronized (mCallingCommentsLock) {
            mCallingComments.add(comments.get(0));
        }

        if (!mIsLoadingBatch) {
            loadNextBatch();
        }
    }

    private void loadNextBatch() {
        synchronized (mCommentQueueLock) {
            if (mCommentQueue.isEmpty()) {
                return;
            }
        }

        mIsLoadingBatch = true;

        mExecutor.execute(() -> {
            synchronized (mCallingCommentsLock) {
                Iterator<Comment> iterator = mCallingComments.iterator();
                while (iterator.hasNext()) {
                    Comment c = iterator.next();
                    String authorFullName = c.getAuthorFullName();
                    ViewPostDetailActivityViewModel.LoadIconListener loadIconListener;
                    synchronized (mListenerMapLock) {
                        loadIconListener = mAuthorFullNameToListenerMap.get(authorFullName);
                    }

                    if (loadIconListener != null) {
                        synchronized (mImageMapLock) {
                            if (mAuthorFullNameToImageMap.containsKey(authorFullName)) {
                                String url = mAuthorFullNameToImageMap.get(authorFullName);
                                mHandler.post(() -> loadIconListener.loadIconSuccess(authorFullName, url));
                                iterator.remove();
                                continue;
                            }
                        }

                        UserData userData = mRedditDataRoomDatabase.userDao().getUserData(c.getAuthor());
                        if (userData != null) {
                            String iconImageUrl = userData.getIconUrl();
                            synchronized (mImageMapLock) {
                                mAuthorFullNameToImageMap.put(authorFullName, iconImageUrl);
                            }
                            mHandler.post(() -> loadIconListener.loadIconSuccess(authorFullName, iconImageUrl));
                            synchronized (mListenerMapLock) {
                                mAuthorFullNameToListenerMap.remove(authorFullName);
                            }
                            iterator.remove();
                        }
                    } else {
                        iterator.remove();
                    }
                }
            }

            StringBuilder stringBuilder = new StringBuilder();

            synchronized (mCommentQueueLock) {
                for (int i = 0; i < BATCH_SIZE && !mCommentQueue.isEmpty(); i++) {
                    Comment comment = mCommentQueue.poll();
                    if (comment == null) {
                        continue;
                    }

                    String authorFullName = comment.getAuthorFullName();
                    boolean alreadyCached;
                    synchronized (mImageMapLock) {
                        alreadyCached = mAuthorFullNameToImageMap.containsKey(authorFullName);
                    }
                    if (!alreadyCached) {
                        stringBuilder.append(authorFullName).append(",");
                        synchronized (mLoadingSetLock) {
                            mLoadingAuthorFullNames.add(authorFullName);
                        }
                    } else if (i == 0) {
                        ViewPostDetailActivityViewModel.LoadIconListener loadIconListener;
                        synchronized (mListenerMapLock) {
                            loadIconListener = mAuthorFullNameToListenerMap.get(authorFullName);
                        }
                        if (loadIconListener != null) {
                            String url;
                            synchronized (mImageMapLock) {
                                url = mAuthorFullNameToImageMap.get(authorFullName);
                            }
                            mHandler.post(() -> {
                                loadIconListener.loadIconSuccess(authorFullName, url);
                            });
                            synchronized (mListenerMapLock) {
                                mAuthorFullNameToListenerMap.remove(authorFullName);
                            }
                        }
                        for (int j = 0; j < BATCH_SIZE - 1 && !mCommentQueue.isEmpty(); j++) {
                            mCommentQueue.poll();
                        }
                        break;
                    }
                }
            }

            if (stringBuilder.length() > 0) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                try {
                    Response<String> response = mRetrofit.create(RedditAPI.class).loadPartialUserData(stringBuilder.toString()).execute();
                    if (response.isSuccessful()) {
                        parseUserProfileImages(response.body());
                        callListenerAndLoadNextBatch(true);
                    } else {
                        callListenerAndLoadNextBatch(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    callListenerAndLoadNextBatch(false);
                }
            } else {
                mIsLoadingBatch = false;
                loadNextBatch();
            }
        });
    }

    @WorkerThread
    private void parseUserProfileImages(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            synchronized (mLoadingSetLock) {
                for (String s : mLoadingAuthorFullNames) {
                    try {
                        String imageUrl = jsonResponse.getJSONObject(s).getString(JSONUtils.PROFILE_IMG_KEY).replaceAll("&amp;","&");
                        synchronized (mImageMapLock) {
                            mAuthorFullNameToImageMap.put(s, imageUrl);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void callListenerAndLoadNextBatch(boolean loadSuccessful) {
        synchronized (mLoadingSetLock) {
            for (String s : mLoadingAuthorFullNames) {
                ViewPostDetailActivityViewModel.LoadIconListener loadIconListener;
                synchronized (mListenerMapLock) {
                    loadIconListener = mAuthorFullNameToListenerMap.get(s);
                }
                if (loadIconListener != null) {
                    String imageUrl;
                    synchronized (mImageMapLock) {
                        imageUrl = mAuthorFullNameToImageMap.get(s);
                    }
                    mHandler.post(() -> {
                        loadIconListener.loadIconSuccess(s, imageUrl);
                    });
                    synchronized (mListenerMapLock) {
                        mAuthorFullNameToListenerMap.remove(s);
                    }
                }
                if (!loadSuccessful) {
                    synchronized (mImageMapLock) {
                        if (!mAuthorFullNameToImageMap.containsKey(s)) {
                            mAuthorFullNameToImageMap.put(s, null);
                        }
                    }
                }
            }

            mLoadingAuthorFullNames.clear();
        }

        mIsLoadingBatch = false;
        loadNextBatch();
    }
}

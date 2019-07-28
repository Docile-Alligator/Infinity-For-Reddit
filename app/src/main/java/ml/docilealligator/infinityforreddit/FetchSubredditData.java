package ml.docilealligator.infinityforreddit;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import SubredditDatabase.SubredditData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

class FetchSubredditData {
    interface FetchSubredditDataListener {
        void onFetchSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers);
        void onFetchSubredditDataFail();
    }

    interface FetchSubredditListingDataListener {
        void onFetchSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after);
        void onFetchSubredditListingDataFail();
    }

    static void fetchSubredditData(Retrofit retrofit, String subredditName, final FetchSubredditDataListener fetchSubredditDataListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> subredditData = api.getSubredditData(subredditName);
        subredditData.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()) {
                    ParseSubredditData.parseSubredditData(response.body(), new ParseSubredditData.ParseSubredditDataListener() {
                        @Override
                        public void onParseSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers) {
                            fetchSubredditDataListener.onFetchSubredditDataSuccess(subredditData, nCurrentOnlineSubscribers);
                        }

                        @Override
                        public void onParseSubredditDataFail() {
                            Log.i("parse failed", "true");
                            fetchSubredditDataListener.onFetchSubredditDataFail();
                        }
                    });
                } else {
                    Log.i("call failed", response.message());
                    fetchSubredditDataListener.onFetchSubredditDataFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("call failed", t.getMessage());
                fetchSubredditDataListener.onFetchSubredditDataFail();
            }
        });
    }

    static void fetchSubredditListingData(Retrofit retrofit, String query, String after, String sortType, final FetchSubredditListingDataListener fetchSubredditListingDataListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> subredditDataCall = api.searchSubreddits(query, after, sortType);
        subredditDataCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()) {
                    ParseSubredditData.parseSubredditListingData(response.body(), new ParseSubredditData.ParseSubredditListingDataListener() {
                        @Override
                        public void onParseSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after) {
                            fetchSubredditListingDataListener.onFetchSubredditListingDataSuccess(subredditData, after);
                        }

                        @Override
                        public void onParseSubredditListingDataFail() {
                            Log.i("parse failed", "true");
                            fetchSubredditListingDataListener.onFetchSubredditListingDataFail();
                        }
                    });
                } else {
                    Log.i("call failed", response.message());
                    fetchSubredditListingDataListener.onFetchSubredditListingDataFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("call failed", t.getMessage());
                fetchSubredditListingDataListener.onFetchSubredditListingDataFail();
            }
        });
    }
}

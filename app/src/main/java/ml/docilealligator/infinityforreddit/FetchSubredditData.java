package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.API.RedditAPI;
import ml.docilealligator.infinityforreddit.SubredditDatabase.SubredditData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchSubredditData {
    public static void fetchSubredditData(Retrofit retrofit, String subredditName, final FetchSubredditDataListener fetchSubredditDataListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> subredditData = api.getSubredditData(subredditName);
        subredditData.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    ParseSubredditData.parseSubredditData(response.body(), new ParseSubredditData.ParseSubredditDataListener() {
                        @Override
                        public void onParseSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers) {
                            fetchSubredditDataListener.onFetchSubredditDataSuccess(subredditData, nCurrentOnlineSubscribers);
                        }

                        @Override
                        public void onParseSubredditDataFail() {
                            fetchSubredditDataListener.onFetchSubredditDataFail();
                        }
                    });
                } else {
                    fetchSubredditDataListener.onFetchSubredditDataFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchSubredditDataListener.onFetchSubredditDataFail();
            }
        });
    }

    static void fetchSubredditListingData(Retrofit retrofit, String query, String after, String sortType,
                                          final FetchSubredditListingDataListener fetchSubredditListingDataListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> subredditDataCall = api.searchSubreddits(query, after, sortType);
        subredditDataCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    ParseSubredditData.parseSubredditListingData(response.body(), new ParseSubredditData.ParseSubredditListingDataListener() {
                        @Override
                        public void onParseSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after) {
                            fetchSubredditListingDataListener.onFetchSubredditListingDataSuccess(subredditData, after);
                        }

                        @Override
                        public void onParseSubredditListingDataFail() {
                            fetchSubredditListingDataListener.onFetchSubredditListingDataFail();
                        }
                    });
                } else {
                    fetchSubredditListingDataListener.onFetchSubredditListingDataFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchSubredditListingDataListener.onFetchSubredditListingDataFail();
            }
        });
    }

    public interface FetchSubredditDataListener {
        void onFetchSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers);

        void onFetchSubredditDataFail();
    }

    interface FetchSubredditListingDataListener {
        void onFetchSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after);

        void onFetchSubredditListingDataFail();
    }
}

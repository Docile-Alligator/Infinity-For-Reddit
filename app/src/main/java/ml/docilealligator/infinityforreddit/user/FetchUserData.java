package ml.docilealligator.infinityforreddit.user;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class FetchUserData {
    public static void fetchUserData(Retrofit retrofit, String userName, FetchUserDataListener fetchUserDataListener) {
        fetchUserData(null, retrofit, null, userName, fetchUserDataListener);
    }

    public static void fetchUserData(RedditDataRoomDatabase redditDataRoomDatabase, Retrofit retrofit,
                                     String accessToken, String userName, FetchUserDataListener fetchUserDataListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> userInfo;
        if (redditDataRoomDatabase == null) {
            userInfo = api.getUserData(userName);
        } else {
            userInfo = api.getUserDataOauth(APIUtils.getOAuthHeader(accessToken), userName);
        }
        userInfo.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParseUserData.parseUserData(redditDataRoomDatabase, response.body(), new ParseUserData.ParseUserDataListener() {
                        @Override
                        public void onParseUserDataSuccess(UserData userData, int inboxCount) {
                            fetchUserDataListener.onFetchUserDataSuccess(userData, inboxCount);
                        }

                        @Override
                        public void onParseUserDataFailed() {
                            fetchUserDataListener.onFetchUserDataFailed();
                        }
                    });
                } else {
                    fetchUserDataListener.onFetchUserDataFailed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchUserDataListener.onFetchUserDataFailed();
            }
        });
    }

    public static void fetchUserListingData(Retrofit retrofit, String query, String after, String sortType, boolean nsfw,
                                            FetchUserListingDataListener fetchUserListingDataListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> userInfo = api.searchUsers(query, after, sortType, nsfw ? 1 : 0);
        userInfo.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParseUserData.parseUserListingData(response.body(), new ParseUserData.ParseUserListingDataListener() {
                        @Override
                        public void onParseUserListingDataSuccess(ArrayList<UserData> userData, String after) {
                            fetchUserListingDataListener.onFetchUserListingDataSuccess(userData, after);
                        }

                        @Override
                        public void onParseUserListingDataFailed() {
                            fetchUserListingDataListener.onFetchUserListingDataFailed();
                        }
                    });
                } else {
                    fetchUserListingDataListener.onFetchUserListingDataFailed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchUserListingDataListener.onFetchUserListingDataFailed();
            }
        });
    }

    public interface FetchUserDataListener {
        void onFetchUserDataSuccess(UserData userData, int inboxCount);

        void onFetchUserDataFailed();
    }

    public interface FetchUserListingDataListener {
        void onFetchUserListingDataSuccess(ArrayList<UserData> userData, String after);

        void onFetchUserListingDataFailed();
    }
}

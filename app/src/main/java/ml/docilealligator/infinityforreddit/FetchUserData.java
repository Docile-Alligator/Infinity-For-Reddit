package ml.docilealligator.infinityforreddit;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import User.UserData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class FetchUserData {
    public interface FetchUserDataListener {
        void onFetchUserDataSuccess(UserData userData);
        void onFetchUserDataFailed();
    }

    public interface FetchUserListingDataListener {
        void onFetchUserListingDataSuccess(ArrayList<UserData> userData, String after);
        void onFetchUserListingDataFailed();
    }

    public static void fetchUserData(Retrofit retrofit, String userName, FetchUserDataListener fetchUserDataListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> userInfo = api.getUserData(userName);
        userInfo.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParseUserData.parseUserData(response.body(), new ParseUserData.ParseUserDataListener() {
                        @Override
                        public void onParseUserDataSuccess(UserData userData) {
                            fetchUserDataListener.onFetchUserDataSuccess(userData);
                        }

                        @Override
                        public void onParseUserDataFailed() {
                            fetchUserDataListener.onFetchUserDataFailed();
                        }
                    });
                } else {
                    Log.i("call failed", response.message());
                    fetchUserDataListener.onFetchUserDataFailed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("call failed", "message " + t.getMessage());
                fetchUserDataListener.onFetchUserDataFailed();
            }
        });
    }

    public static void fetchUserListingData(Retrofit retrofit, String query, String after, String sortType,
                                            FetchUserListingDataListener fetchUserListingDataListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> userInfo = api.searchUsers(query, after, sortType);
        userInfo.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
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
                    Log.i("call failed", response.message());
                    fetchUserListingDataListener.onFetchUserListingDataFailed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("call failed", "message " + t.getMessage());
                fetchUserListingDataListener.onFetchUserListingDataFailed();
            }
        });
    }
}

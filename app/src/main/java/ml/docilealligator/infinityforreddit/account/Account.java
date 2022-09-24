package ml.docilealligator.infinityforreddit.account;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "accounts")
public class Account implements Parcelable {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "username")
    private String accountName;
    @ColumnInfo(name = "profile_image_url")
    private String profileImageUrl;
    @ColumnInfo(name = "banner_image_url")
    private String bannerImageUrl;
    @ColumnInfo(name = "karma")
    private int karma;
    @ColumnInfo(name = "access_token")
    private String accessToken;
    @ColumnInfo(name = "refresh_token")
    private String refreshToken;
    @ColumnInfo(name = "code")
    private String code;
    @ColumnInfo(name = "is_current_user")
    private boolean isCurrentUser;

    @Ignore
    protected Account(Parcel in) {
        accountName = in.readString();
        profileImageUrl = in.readString();
        bannerImageUrl = in.readString();
        karma = in.readInt();
        accessToken = in.readString();
        refreshToken = in.readString();
        code = in.readString();
        isCurrentUser = in.readByte() != 0;
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

    @Ignore
    public static Account getAnonymousAccount() {
        return new Account("-", null, null, null, null, null, 0, false);
    }

    public Account(@NonNull String accountName, String accessToken, String refreshToken, String code,
                   String profileImageUrl, String bannerImageUrl, int karma, boolean isCurrentUser) {
        this.accountName = accountName;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.code = code;
        this.profileImageUrl = profileImageUrl;
        this.bannerImageUrl = bannerImageUrl;
        this.karma = karma;
        this.isCurrentUser = isCurrentUser;
    }

    @NonNull
    public String getAccountName() {
        return accountName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public int getKarma() {
        return karma;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getCode() {
        return code;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accountName);
        dest.writeString(profileImageUrl);
        dest.writeString(bannerImageUrl);
        dest.writeInt(karma);
        dest.writeString(accessToken);
        dest.writeString(refreshToken);
        dest.writeString(code);
        dest.writeByte((byte) (isCurrentUser ? 1 : 0));
    }
}

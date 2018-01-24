package tv.camment.cammentsdk.aws.messages;


import android.os.Parcel;
import android.os.Parcelable;

public final class UserUnblockedMessage extends BaseMessage {

    public Body body;

    public UserUnblockedMessage() {
        super();
    }

    private UserUnblockedMessage(Parcel in) {
        super(in);
        body = in.readParcelable(Body.class.getClassLoader());
    }

    public static final Creator<UserUnblockedMessage> CREATOR = new Creator<UserUnblockedMessage>() {
        @Override
        public UserUnblockedMessage createFromParcel(Parcel in) {
            return new UserUnblockedMessage(in);
        }

        @Override
        public UserUnblockedMessage[] newArray(int size) {
            return new UserUnblockedMessage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        super.writeToParcel(dest, i);
        dest.writeParcelable(body, i);
    }

    public static class Body implements Parcelable {
        public String groupUuid;
        public UnblockedUser unblockedUser;

        protected Body(Parcel in) {
            groupUuid = in.readString();
            unblockedUser = in.readParcelable(UnblockedUser.class.getClassLoader());
        }

        public static final Creator<Body> CREATOR = new Creator<Body>() {
            @Override
            public Body createFromParcel(Parcel in) {
                return new Body(in);
            }

            @Override
            public Body[] newArray(int size) {
                return new Body[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(groupUuid);
            dest.writeParcelable(unblockedUser, flags);
        }
    }

    public static class UnblockedUser implements Parcelable {
        public String name;
        public String userCognitoIdentityId;
        public String picture;

        UnblockedUser(Parcel in) {
            name = in.readString();
            userCognitoIdentityId = in.readString();
            picture = in.readString();
        }

        public static final Creator<UnblockedUser> CREATOR = new Creator<UnblockedUser>() {
            @Override
            public UnblockedUser createFromParcel(Parcel in) {
                return new UnblockedUser(in);
            }

            @Override
            public UnblockedUser[] newArray(int size) {
                return new UnblockedUser[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(userCognitoIdentityId);
            dest.writeString(picture);
        }
    }

}

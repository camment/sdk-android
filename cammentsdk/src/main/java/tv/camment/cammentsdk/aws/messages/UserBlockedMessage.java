package tv.camment.cammentsdk.aws.messages;


import android.os.Parcel;
import android.os.Parcelable;

public final class UserBlockedMessage extends BaseMessage {

    public Body body;

    public UserBlockedMessage() {
        super();
    }

    private UserBlockedMessage(Parcel in) {
        super(in);
        body = in.readParcelable(Body.class.getClassLoader());
    }

    public static final Creator<UserBlockedMessage> CREATOR = new Creator<UserBlockedMessage>() {
        @Override
        public UserBlockedMessage createFromParcel(Parcel in) {
            return new UserBlockedMessage(in);
        }

        @Override
        public UserBlockedMessage[] newArray(int size) {
            return new UserBlockedMessage[size];
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
        public BlockedUser blockedUser;

        protected Body(Parcel in) {
            groupUuid = in.readString();
            blockedUser = in.readParcelable(BlockedUser.class.getClassLoader());
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
            dest.writeParcelable(blockedUser, flags);
        }
    }

    public static class BlockedUser implements Parcelable {
        public String name;
        public String userCognitoIdentityId;
        public String picture;

        BlockedUser(Parcel in) {
            name = in.readString();
            userCognitoIdentityId = in.readString();
            picture = in.readString();
        }

        public static final Creator<BlockedUser> CREATOR = new Creator<BlockedUser>() {
            @Override
            public BlockedUser createFromParcel(Parcel in) {
                return new BlockedUser(in);
            }

            @Override
            public BlockedUser[] newArray(int size) {
                return new BlockedUser[size];
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

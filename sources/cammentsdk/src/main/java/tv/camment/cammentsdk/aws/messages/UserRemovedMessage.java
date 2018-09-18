package tv.camment.cammentsdk.aws.messages;


import android.os.Parcel;
import android.os.Parcelable;

public final class UserRemovedMessage extends BaseMessage {

    public Body body;

    public UserRemovedMessage() {
        super();
    }

    private UserRemovedMessage(Parcel in) {
        super(in);
        body = in.readParcelable(Body.class.getClassLoader());
    }

    public static final Creator<UserRemovedMessage> CREATOR = new Creator<UserRemovedMessage>() {
        @Override
        public UserRemovedMessage createFromParcel(Parcel in) {
            return new UserRemovedMessage(in);
        }

        @Override
        public UserRemovedMessage[] newArray(int size) {
            return new UserRemovedMessage[size];
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
        public RemovedUser removedUser;

        protected Body(Parcel in) {
            groupUuid = in.readString();
            removedUser = in.readParcelable(RemovedUser.class.getClassLoader());
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
            dest.writeParcelable(removedUser, flags);
        }
    }

    public static class RemovedUser implements Parcelable {
        public String userCognitoIdentityId;
        public String name;

        protected RemovedUser(Parcel in) {
            userCognitoIdentityId = in.readString();
            name = in.readString();
        }

        public static final Creator<RemovedUser> CREATOR = new Creator<RemovedUser>() {
            @Override
            public RemovedUser createFromParcel(Parcel in) {
                return new RemovedUser(in);
            }

            @Override
            public RemovedUser[] newArray(int size) {
                return new RemovedUser[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(userCognitoIdentityId);
            dest.writeString(name);
        }
    }

}

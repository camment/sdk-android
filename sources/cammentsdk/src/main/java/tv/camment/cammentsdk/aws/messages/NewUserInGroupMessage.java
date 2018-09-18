package tv.camment.cammentsdk.aws.messages;


import android.os.Parcel;
import android.os.Parcelable;

public final class NewUserInGroupMessage extends BaseMessage {

    public Body body;

    public NewUserInGroupMessage() {
        super();
    }

    private NewUserInGroupMessage(Parcel in) {
        super(in);
        body = in.readParcelable(Body.class.getClassLoader());
    }

    public static final Creator<NewUserInGroupMessage> CREATOR = new Creator<NewUserInGroupMessage>() {
        @Override
        public NewUserInGroupMessage createFromParcel(Parcel in) {
            return new NewUserInGroupMessage(in);
        }

        @Override
        public NewUserInGroupMessage[] newArray(int size) {
            return new NewUserInGroupMessage[size];
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
        public String showUuid;
        public String groupOwnerCognitoIdentityId;
        public User joiningUser;


        protected Body(Parcel in) {
            groupUuid = in.readString();
            showUuid = in.readString();
            groupOwnerCognitoIdentityId = in.readString();
            joiningUser = in.readParcelable(User.class.getClassLoader());
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
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(groupUuid);
            parcel.writeString(showUuid);
            parcel.writeString(groupOwnerCognitoIdentityId);
            parcel.writeParcelable(joiningUser, i);
        }
    }

    public static class User implements Parcelable {
        public String name;
        public String userCognitoIdentityId;
        public String picture;

        User(Parcel in) {
            name = in.readString();
            userCognitoIdentityId = in.readString();
            picture = in.readString();
        }

        public static final Creator<User> CREATOR = new Creator<User>() {
            @Override
            public User createFromParcel(Parcel in) {
                return new User(in);
            }

            @Override
            public User[] newArray(int size) {
                return new User[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(name);
            parcel.writeString(userCognitoIdentityId);
            parcel.writeString(picture);
        }
    }

}

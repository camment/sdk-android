package tv.camment.cammentsdk.aws.messages;


import android.os.Parcel;
import android.os.Parcelable;

public class MembershipRequestMessage extends BaseMessage {

    public Body body;

    public MembershipRequestMessage() {
        super();
    }

    public static class Body implements Parcelable {
        public String groupUuid;
        public String showUuid;
        public JoiningUser joiningUser;

        Body(Parcel in) {
            groupUuid = in.readString();
            showUuid = in.readString();
            joiningUser = in.readParcelable(JoiningUser.class.getClassLoader());
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
            parcel.writeParcelable(joiningUser, i);
        }
    }

    public static class JoiningUser implements Parcelable {
        public String name;
        public String userCognitoIdentityId;

        JoiningUser(Parcel in) {
            name = in.readString();
            userCognitoIdentityId = in.readString();
        }

        public static final Creator<JoiningUser> CREATOR = new Creator<JoiningUser>() {
            @Override
            public JoiningUser createFromParcel(Parcel in) {
                return new JoiningUser(in);
            }

            @Override
            public JoiningUser[] newArray(int size) {
                return new JoiningUser[size];
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
        }
    }

    private MembershipRequestMessage(Parcel in) {
        super(in);
        body = in.readParcelable(Body.class.getClassLoader());
    }

    public static final Creator<MembershipRequestMessage> CREATOR = new Creator<MembershipRequestMessage>() {
        @Override
        public MembershipRequestMessage createFromParcel(Parcel in) {
            return new MembershipRequestMessage(in);
        }

        @Override
        public MembershipRequestMessage[] newArray(int size) {
            return new MembershipRequestMessage[size];
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

}

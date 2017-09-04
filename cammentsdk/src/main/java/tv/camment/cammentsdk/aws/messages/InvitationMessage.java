package tv.camment.cammentsdk.aws.messages;


import android.os.Parcel;
import android.os.Parcelable;

public final class InvitationMessage extends BaseMessage {

    public Body body;

    public InvitationMessage() {
        super();
    }

    public static class Body implements Parcelable {
        public String timestamp;
        public String groupUuid;
        public String userFacebookId;
        public String key;
        public boolean confirmed;
        public String showUuid;
        public InvitingUser invitingUser;

        public Body() {

        }

        Body(Parcel in) {
            timestamp = in.readString();
            groupUuid = in.readString();
            userFacebookId = in.readString();
            key = in.readString();
            confirmed = in.readByte() != 0;
            showUuid = in.readString();
            invitingUser = in.readParcelable(InvitingUser.class.getClassLoader());
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
            parcel.writeString(timestamp);
            parcel.writeString(groupUuid);
            parcel.writeString(userFacebookId);
            parcel.writeString(key);
            parcel.writeByte((byte) (confirmed ? 1 : 0));
            parcel.writeString(showUuid);
            parcel.writeParcelable(invitingUser, i);
        }
    }

    public static class InvitingUser implements Parcelable {
        public String facebookId;
        public String picture;
        public String name;
        public String userCognitoIdentityId;

        InvitingUser(Parcel in) {
            facebookId = in.readString();
            picture = in.readString();
            name = in.readString();
            userCognitoIdentityId = in.readString();
        }

        public static final Creator<InvitingUser> CREATOR = new Creator<InvitingUser>() {
            @Override
            public InvitingUser createFromParcel(Parcel in) {
                return new InvitingUser(in);
            }

            @Override
            public InvitingUser[] newArray(int size) {
                return new InvitingUser[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(facebookId);
            parcel.writeString(picture);
            parcel.writeString(name);
            parcel.writeString(userCognitoIdentityId);
        }
    }

    private InvitationMessage(Parcel in) {
        super(in);
        body = in.readParcelable(Body.class.getClassLoader());
    }

    public static final Creator<InvitationMessage> CREATOR = new Creator<InvitationMessage>() {
        @Override
        public InvitationMessage createFromParcel(Parcel in) {
            return new InvitationMessage(in);
        }

        @Override
        public InvitationMessage[] newArray(int size) {
            return new InvitationMessage[size];
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

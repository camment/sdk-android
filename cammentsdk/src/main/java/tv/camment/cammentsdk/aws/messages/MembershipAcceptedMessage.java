package tv.camment.cammentsdk.aws.messages;


import android.os.Parcel;
import android.os.Parcelable;

public class MembershipAcceptedMessage extends BaseMessage {

    public Body body;

    public MembershipAcceptedMessage() {
        super();
    }

    public static class Body implements Parcelable {
        public String groupUuid;

        Body(Parcel in) {
            groupUuid = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(groupUuid);
        }

        @Override
        public int describeContents() {
            return 0;
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
    }

    private MembershipAcceptedMessage(Parcel in) {
        super(in);
        body = in.readParcelable(Body.class.getClassLoader());
    }

    public static final Creator<MembershipAcceptedMessage> CREATOR = new Creator<MembershipAcceptedMessage>() {
        @Override
        public MembershipAcceptedMessage createFromParcel(Parcel in) {
            return new MembershipAcceptedMessage(in);
        }

        @Override
        public MembershipAcceptedMessage[] newArray(int size) {
            return new MembershipAcceptedMessage[size];
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

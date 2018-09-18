package tv.camment.cammentsdk.aws.messages;

import android.os.Parcel;
import android.os.Parcelable;

public final class NewGroupHostMessage extends BaseMessage {

    public Body body;

    public NewGroupHostMessage() {
        super();
    }

    private NewGroupHostMessage(Parcel in) {
        super(in);
        body = in.readParcelable(Body.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        super.writeToParcel(dest, i);
        dest.writeParcelable(body, i);
    }

    public static final Creator<NewGroupHostMessage> CREATOR = new Creator<NewGroupHostMessage>() {
        @Override
        public NewGroupHostMessage createFromParcel(Parcel in) {
            return new NewGroupHostMessage(in);
        }

        @Override
        public NewGroupHostMessage[] newArray(int size) {
            return new NewGroupHostMessage[size];
        }
    };

    public static class Body implements Parcelable {
        public String groupUuid;
        public String hostId;

        protected Body(Parcel in) {
            groupUuid = in.readString();
            hostId = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(groupUuid);
            dest.writeString(hostId);
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

}

package tv.camment.cammentsdk.aws.messages;

import android.os.Parcel;
import android.os.Parcelable;

public final class NeedPlayerStateMessage extends BaseMessage {

    public Body body;

    public NeedPlayerStateMessage() {
        super();
    }

    private NeedPlayerStateMessage(Parcel in) {
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

    public static final Creator<NeedPlayerStateMessage> CREATOR = new Creator<NeedPlayerStateMessage>() {
        @Override
        public NeedPlayerStateMessage createFromParcel(Parcel in) {
            return new NeedPlayerStateMessage(in);
        }

        @Override
        public NeedPlayerStateMessage[] newArray(int size) {
            return new NeedPlayerStateMessage[size];
        }
    };

    public static class Body implements Parcelable {
        public String groupUuid;

        public Body(String groupUuid) {
            this.groupUuid = groupUuid;
        }

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
}

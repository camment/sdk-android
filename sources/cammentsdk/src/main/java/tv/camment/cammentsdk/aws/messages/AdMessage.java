package tv.camment.cammentsdk.aws.messages;

import android.os.Parcel;
import android.os.Parcelable;

public final class AdMessage extends BaseMessage {

    public Body body;

    public AdMessage() {
        super();
    }

    private AdMessage(Parcel in) {
        super(in);
        body = in.readParcelable(Body.class.getClassLoader());
    }

    public static final Creator<AdMessage> CREATOR = new Creator<AdMessage>() {
        @Override
        public AdMessage createFromParcel(Parcel in) {
            return new AdMessage(in);
        }

        @Override
        public AdMessage[] newArray(int size) {
            return new AdMessage[size];
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
        public String title;
        public String file;
        public String url;
        public String thumbnail;

        public Body() {

        }

        protected Body(Parcel in) {
            title = in.readString();
            file = in.readString();
            url = in.readString();
            thumbnail = in.readString();
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
            parcel.writeString(title);
            parcel.writeString(file);
            parcel.writeString(url);
            parcel.writeString(thumbnail);
        }
    }

}
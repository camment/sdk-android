package tv.camment.cammentsdk.aws.messages;


public final class NewUserInGroupMessage extends BaseMessage {

    public Body body;

    public class Body {
        public String groupUuid;
        public User user;
    }

    public class User {
        public String facebookId;
        public String picture;
        public String name;
        public String userCognitoIdentityId;
    }

}

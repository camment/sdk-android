package tv.camment.cammentsdk.aws.messages;


public class InvitationMessage extends BaseMessage {

    public Body body;

    public class Body {
        public String timestamp;
        public String groupUuid;
        public String userFacebookId;
        public String key;
        public boolean confirmed;
        public String showUuid;
        public InvitingUser invitingUser;
    }

    public class InvitingUser {
        public String facebookId;
        public String picture;
        public String name;
        public String userCognitoIdentityId;
    }

}

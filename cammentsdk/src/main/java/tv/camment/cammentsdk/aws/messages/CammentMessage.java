package tv.camment.cammentsdk.aws.messages;


public class CammentMessage extends BaseMessage {

    public String groupUuid;

    public Body body;

    public class Body {
        public String uuid;
        public String thumbnail;
        public String userGroupUuid;
        public String url;
        public String userCognitoIdentityId;
        public String timestamp;
    }
}

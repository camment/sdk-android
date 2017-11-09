package tv.camment.cammentsdk;


public final class ShowMetadata {

    private final String uuid;
    private final String invitationText;

    public ShowMetadata(String uuid, String invitationText) {
        this.uuid = uuid;
        this.invitationText = invitationText;
    }

    public String getUuid() {
        return uuid;
    }

    public String getInvitationText() {
        return invitationText;
    }

}

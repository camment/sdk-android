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

    public static class ShowMetadataBuilder {
        private String uuid;
        private String invitationText;

        public ShowMetadataBuilder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public ShowMetadataBuilder invitationText(String invitationText) {
            this.invitationText = invitationText;
            return this;
        }

        public ShowMetadata build() {
            return new ShowMetadata(uuid, invitationText);
        }
    }

}

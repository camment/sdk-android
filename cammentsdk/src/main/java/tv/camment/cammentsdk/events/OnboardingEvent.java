package tv.camment.cammentsdk.events;


public final class OnboardingEvent {

    private final boolean start;

    public OnboardingEvent(boolean start) {
        this.start = start;
    }

    public boolean shouldStart() {
        return start;
    }

}

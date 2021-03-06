package tv.camment.cammentsdk.helpers;

import java.util.ArrayList;
import java.util.List;


public final class OnboardingPreferences extends BasePreferences {

    private static final String PREFS_NAME = "camment_onboarding_prefs";

    private static final String PREFS_RECORD = "onboarding_record";
    private static final String PREFS_PLAY = "onboarding_play";
    private static final String PREFS_HIDE = "onboarding_hide";
    private static final String PREFS_SHOW = "onboarding_show";
    private static final String PREFS_DELETE = "onboarding_delete";
    private static final String PREFS_INVITE = "onboarding_invite";

    private static final String PREFS_FIRST_ONBOARDING = "prefs_onboarding_first";

    private static final String PREFS_TUTORIAL_SKIPPED = "prefs_tutorial_skipped";

    private static final String PREFS_LAST_COMPLETED = "prefs_last_completed";

    private static final String PREFS_LATER_DISPLAYED = "prefs_later_displayed";

    private List<Step> steps;

    private static OnboardingPreferences INSTANCE;

    public static OnboardingPreferences getInstance() {
        if (INSTANCE == null) {
            synchronized (OnboardingPreferences.class) {
                if (INSTANCE == null) {
                    INSTANCE = new OnboardingPreferences();
                }
            }
        }
        return INSTANCE;
    }

    private OnboardingPreferences() {

    }

    @Override
    protected String getPreferencesName() {
        return PREFS_NAME;
    }

    public void initSteps() {
        steps = new ArrayList<>();

        for (Step step : Step.values()) {
            if (!wasOnboardingStepShown(step)
                    && step != Step.LATER
                    && step != Step.TUTORIAL) {
                steps.add(step);
            }
        }
    }

    public void putOnboardingStepDisplayed(Step step, boolean display) {
        Step currentStep = steps != null && !steps.isEmpty() ? steps.get(0) : null;

        switch (step) {
            case RECORD:
                if (display && currentStep == step) {
                    MixpanelHelper.getInstance().trackEvent(MixpanelHelper.ONBOARDING_START);
                }

                putBoolean(PREFS_RECORD, display);
                break;
            case PLAY:
                putBoolean(PREFS_PLAY, display);
                break;
            case HIDE:
                putBoolean(PREFS_HIDE, display);
                break;
            case SHOW:
                putBoolean(PREFS_SHOW, display);
                break;
            case DELETE:
                putBoolean(PREFS_DELETE, display);
                break;
            case INVITE:
                if (display && currentStep == step) {
                    MixpanelHelper.getInstance().trackEvent(MixpanelHelper.ONBOARDING_END);
                }

                putBoolean(PREFS_INVITE, display);
                break;
            default:
                break;
        }

        if (steps != null && display && currentStep == step) {
            steps.remove(step);
        }
    }

    public boolean wasOnboardingStepShown(Step step) {
        switch (step) {
            case RECORD:
                return getBoolean(PREFS_RECORD, false);
            case PLAY:
                return getBoolean(PREFS_PLAY, false);
            case HIDE:
                return getBoolean(PREFS_HIDE, false);
            case SHOW:
                return getBoolean(PREFS_SHOW, false);
            case DELETE:
                return getBoolean(PREFS_DELETE, false);
            case INVITE:
                return getBoolean(PREFS_INVITE, false);
            default:
                return true;
        }
    }

    public Step getNextOnboardingStep() {
        return steps != null && steps.size() > 0 ? steps.get(0) : null;
    }

    public boolean isOnboardingStepLastRemaining(Step step) {
        return steps != null && steps.size() == 1 && steps.get(0) == step;
    }

    public boolean wasOnboardingFirstTimeShown() {
        return getBoolean(PREFS_FIRST_ONBOARDING, false);
    }

    public void setOnboardingFirstTimeShown() {
        putBoolean(PREFS_FIRST_ONBOARDING, true);
    }

    public boolean wasTutorialSkipped() {
        return getBoolean(PREFS_TUTORIAL_SKIPPED, false);
    }

    public void setTutorialSkipped(boolean skipped) {
        putBoolean(PREFS_TUTORIAL_SKIPPED, skipped);
    }

    public Step getLastCompletedStep() {
        return Step.fromInt(getInt(PREFS_LAST_COMPLETED, -1));
    }

    public void setLastCompletedStep(Step step) {
        putInt(PREFS_LAST_COMPLETED, step.getIntValue());
    }

    public boolean wasLaterDisplayed() {
        return getBoolean(PREFS_LATER_DISPLAYED, false);
    }

    public void setLaterDisplayed() {
        putBoolean(PREFS_LATER_DISPLAYED, true);
    }

    @SuppressWarnings("unused")
    public void resetOnboarding() {
        putBoolean(PREFS_FIRST_ONBOARDING, false);
        putBoolean(PREFS_TUTORIAL_SKIPPED, false);
        putBoolean(PREFS_LATER_DISPLAYED, false);
        putInt(PREFS_LAST_COMPLETED, -1);

        putBoolean(PREFS_RECORD, false);
        putBoolean(PREFS_PLAY, false);
        putBoolean(PREFS_HIDE, false);
        putBoolean(PREFS_SHOW, false);
        putBoolean(PREFS_DELETE, false);
        putBoolean(PREFS_INVITE, false);
    }

}

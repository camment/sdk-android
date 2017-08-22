package tv.camment.cammentsdk.helpers;

import java.util.ArrayList;
import java.util.List;


public class OnboardingPreferences extends BasePreferences {

    private static final String PREFS_NAME = "camment_onboarding_prefs";

    private static final String PREFS_RECORD = "onboarding_record";
    private static final String PREFS_PLAY = "onboarding_play";
    private static final String PREFS_HIDE = "onboarding_hide";
    private static final String PREFS_SHOW = "onboarding_show";
    private static final String PREFS_DELETE = "onboarding_delete";
    private static final String PREFS_INVITE = "onboarding_invite";

    private List<Step> steps;

    private static OnboardingPreferences INSTANCE;

    public static OnboardingPreferences getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OnboardingPreferences();
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
            if (!wasOnboardingStepShown(step)) {
                steps.add(step);
            }
        }
    }

    public void putOnboardingStepDisplayed(Step step, boolean display) {
        switch (step) {
            case RECORD:
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
                putBoolean(PREFS_INVITE, display);
                break;
            default:
                break;
        }

        if (steps != null && display) {
            steps.remove(step);
        }
    }

    public boolean wasAtLeastOneStepShown() {
        return steps != null
                && steps.size() != Step.values().length;
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
                return false;
        }
    }

    public Step getNextOnboardingStep() {
        return steps != null && steps.size() > 0 ? steps.get(0) : null;
    }

    public boolean isOnboardingStepLastRemaining(Step step) {
        return steps != null && steps.size() == 1 && steps.get(0) == step;
    }
}

package tv.camment.cammentsdk.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by petrushka on 21/08/2017.
 */

public class OnboardingPreferences extends BasePreferences {

    private static final String PREFS_NAME = "camment_onboarding_prefs";

    private static final String PREFS_FINISHED = "onboarding_finished";

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
            if (!getOnboardingStepFinished(step)) {
                steps.add(step);
            }
        }
    }

    public void putOnboardingFinished(boolean finished) {
        putBoolean(PREFS_FINISHED, finished);
    }

    public boolean getOnboardingFinished() {
        return getBoolean(PREFS_FINISHED, false);
    }

    public void putOnboardingStepFinished(Step step, boolean finished) {
        switch (step) {
            case RECORD:
                putBoolean(PREFS_RECORD, finished);
                break;
            case PLAY:
                putBoolean(PREFS_PLAY, finished);
                break;
            case HIDE:
                putBoolean(PREFS_HIDE, finished);
                break;
            case SHOW:
                putBoolean(PREFS_SHOW, finished);
                break;
            case DELETE:
                putBoolean(PREFS_DELETE, finished);
                break;
            case INVITE:
                putBoolean(PREFS_INVITE, finished);
                break;
            default:
                break;
        }
    }

    public boolean isAtLeastOneStepFinished() {
        return steps != null
                && steps.size() != Step.values().length;
    }

    public boolean getOnboardingStepFinished(Step step) {
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

}

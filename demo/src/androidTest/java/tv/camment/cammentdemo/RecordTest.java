package tv.camment.cammentdemo;


import android.Manifest;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.MotionEvents;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import tv.camment.cammentsdk.helpers.OnboardingPreferences;
import tv.camment.cammentsdk.helpers.Step;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
public class RecordTest {

    @Rule
    public ActivityTestRule<CammentShowsActivity> mActivityRule =
            new ActivityTestRule<>(CammentShowsActivity.class);

    @Rule public GrantPermissionRule cameraPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);
    @Rule public GrantPermissionRule micPermissionRule = GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO);

    @Before
    public void beforeTest() {
        OnboardingPreferences.getInstance().putOnboardingStepDisplayed(Step.RECORD, true);
        OnboardingPreferences.getInstance().putOnboardingStepDisplayed(Step.PLAY, true);
        OnboardingPreferences.getInstance().putOnboardingStepDisplayed(Step.DELETE, true);
        OnboardingPreferences.getInstance().putOnboardingStepDisplayed(Step.SHOW, true);
        OnboardingPreferences.getInstance().putOnboardingStepDisplayed(Step.HIDE, true);
        OnboardingPreferences.getInstance().putOnboardingStepDisplayed(Step.INVITE, true);
        OnboardingPreferences.getInstance().putOnboardingStepDisplayed(Step.LATER, true);
        OnboardingPreferences.getInstance().setOnboardingFirstTimeShown();
    }

    @Test
    public void test_0_record() {
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.rv_shows)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.constraint_layout)));
        onView(isRoot()).perform(waitFor(10000));
        for (int i = 0; i < 10; i++) {
            recordVideo();
        }
        onView(isRoot()).perform(waitFor(5000));
    }

    private void recordVideo() {
        onView(isRoot()).perform(waitFor(1000));
        onView(withId(R.id.camment_overlay)).perform(touchDownAndUp(20,20, 3000));
    }

    private static ViewAction touchDownAndUp(final float x, final float y, final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Send touch events.";
            }

            @Override
            public void perform(UiController uiController, final View view) {
                // Get view absolute position
                View v = view.findViewById(R.id.cmmsdk_ib_record);

                int[] location = new int[2];
                v.getLocationOnScreen(location);

                // Offset coordinates by view position
                float[] coordinates = new float[] { x + location[0], y + location[1] };
                float[] precision = new float[] { 1f, 1f };

                // Send down event, pause, and send up
                MotionEvent down = MotionEvents.sendDown(uiController, coordinates, precision).down;
                uiController.loopMainThreadForAtLeast(millis);
                MotionEvents.sendUp(uiController, down, coordinates);
            }
        };
    }

    private static ViewAction waitFor(final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, final View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }

    private static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                v.performClick();
            }
        };
    }
}


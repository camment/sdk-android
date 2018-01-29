package tv.camment.cammentsdk.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.events.ShowTutorialTooltipEvent;
import tv.camment.cammentsdk.events.TutorialContinueEvent;
import tv.camment.cammentsdk.events.TutorialSkippedEvent;
import tv.camment.cammentsdk.helpers.OnboardingPreferences;
import tv.camment.cammentsdk.helpers.PermissionHelper;
import tv.camment.cammentsdk.helpers.Step;
import tv.camment.cammentsdk.views.dialogs.CammentDialog;
import tv.camment.cammentsdk.views.dialogs.OnboardingCammentDialog;


abstract class BaseOnboardingOverlay extends RelativeLayout
        implements CammentDialog.ActionListener {

    private Map<Step, TooltipView> tooltipViewMap;
    private View recordingButton;
    private View recyclerView;
    private SkipTutorialView skipTutorialView;

    BaseOnboardingOverlay(Context context) {
        super(context);
        init();
    }

    BaseOnboardingOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    BaseOnboardingOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    BaseOnboardingOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        EventBus.getDefault().unregister(this);

        super.onDetachedFromWindow();
    }

    private void init() {
        tooltipViewMap = new HashMap<>();
        OnboardingPreferences.getInstance().initSteps();

        if (!OnboardingPreferences.getInstance().wasOnboardingStepShown(Step.RECORD)
                && !OnboardingPreferences.getInstance().wasOnboardingFirstTimeShown()) {
            OnboardingPreferences.getInstance().setOnboardingFirstTimeShown();

            BaseMessage message = new BaseMessage();
            message.type = MessageType.ONBOARDING;

            OnboardingCammentDialog.createInstance(message).show();
        }
    }

    void setAnchorViews(View recordingButton, View recyclerView) {
        this.recordingButton = recordingButton;
        this.recyclerView = recyclerView;
    }

    void displayTooltip(Step step) {
        if (tooltipViewMap != null
                && tooltipViewMap.size() > 0) {
            return;
        }

        if (OnboardingPreferences.getInstance().wasTutorialSkipped()
                && step != Step.TUTORIAL) {
            return;
        }

        OnboardingPreferences.getInstance().putOnboardingStepDisplayed(step, true);

        final TooltipView tooltipView = new TooltipView(getContext());
        addView(tooltipView);

        if (step == Step.RECORD
                && !PermissionHelper.getInstance().hasPermissions()) {
            PermissionHelper.getInstance().cameraAndMicTask();
        }

        TooltipView.Orientation orientation;
        switch (step) {
            case RECORD:
            case LATER:
                orientation = TooltipView.Orientation.RIGHT;
                break;
            case PLAY:
            case HIDE:
            case SHOW:
            case DELETE:
            case INVITE:
            case TUTORIAL:
            default:
                orientation = TooltipView.Orientation.LEFT;
                break;
        }

        View attachToView;
        switch (step) {
            case RECORD:
            case LATER:
                attachToView = recordingButton;
                break;
            case HIDE:
            case SHOW:
            case INVITE:
            case TUTORIAL:
                attachToView = recyclerView;
                break;
            case PLAY:
            case DELETE:
            default:
                View childAt = ((RecyclerView) recyclerView).getChildAt(0);
                if (childAt != null) {
                    attachToView = childAt;
                } else {
                    attachToView = recyclerView;
                }
                break;
        }

        tooltipView.init(attachToView, orientation, step);
        tooltipView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view instanceof TooltipView) {
                    Step step = ((TooltipView) view).getStep();
                    if (step != null) {
                        hideTooltipIfNeeded(step);
                    }
                }
            }
        });

        tooltipViewMap.put(step, tooltipView);

        if (step == Step.RECORD) {
            displaySkipTutorial();
        }
    }

    void hideTooltipIfNeeded(Step step) {
        hideTooltipIfNeeded(step, true);
    }

    private void hideTooltipIfNeeded(Step step, boolean markAsCompleted) {
        if (markAsCompleted) {
            OnboardingPreferences.getInstance().putOnboardingStepDisplayed(step, true);
        }

        if (tooltipViewMap != null
                && tooltipViewMap.containsKey(step)) {
            TooltipView tooltipView = tooltipViewMap.get(step);
            if (tooltipView != null) {
                removeView(tooltipView);

                if (step != null
                        && step.getIntValue() <= Step.LAST_STEP_ID
                        && markAsCompleted) {
                    OnboardingPreferences.getInstance().setLastCompletedStep(step);
                }
            }
            tooltipViewMap.remove(step);

            if (!markAsCompleted) {
                return;
            }
        } else {
            return;
        }

        final Step nextStep = OnboardingPreferences.getInstance().getNextOnboardingStep();

        if (nextStep != null) {
            switch (nextStep) {
                case HIDE:
                case SHOW:
                    if (CammentProvider.getCammentsSize() > 0) {
                        displayTooltip(nextStep);
                    }
                    break;
                case INVITE:
                    displayTooltip(nextStep);
                    break;
                case DELETE:
                    if (CammentProvider.getCammentsSize() > 0) {
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                displayTooltip(nextStep);
                            }
                        }, 500);
                    }
                    break;
            }
        }
    }

    private void displaySkipTutorial() {
        skipTutorialView = new SkipTutorialView(getContext());
        addView(skipTutorialView);
        skipTutorialView.init();
        skipTutorialView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                OnboardingPreferences.getInstance().setTutorialSkipped(true);
                hideSkipTutorial();
                EventBus.getDefault().post(new TutorialSkippedEvent());
            }
        });
    }

    private void hideSkipTutorial() {
        if (skipTutorialView != null) {
            removeView(skipTutorialView);
            skipTutorialView = null;
        }
    }

    @Override
    public void onPositiveButtonClick(BaseMessage baseMessage) {
        if (baseMessage.type == MessageType.ONBOARDING) {
            displayTooltip(Step.RECORD);
        }
    }

    @Override
    public void onNegativeButtonClick(BaseMessage baseMessage) {
        if (baseMessage.type == MessageType.ONBOARDING) {
            displayTooltip(Step.LATER);
        }
    }

    @Override
    public void addView(View child) {
        if (child instanceof TooltipView) {
            child.setAlpha(0.0f);
            child.setTranslationY(16);
            super.addView(child);
            child.animate().alpha(1.0f).translationY(0).setDuration(500).start();
        } else {
            child.setTranslationX(getWidth() * 2);
            super.addView(child);
            child.animate().translationX(0).setDuration(1000).setStartDelay(3000).start();
        }
    }

    @Override
    public void removeView(View child) {
        child.animate().alpha(1.0f).setDuration(500).start();
        super.removeView(child);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TutorialContinueEvent event) {
        Step lastCompletedStep = OnboardingPreferences.getInstance().getLastCompletedStep();

        if (lastCompletedStep != null && lastCompletedStep.getIntValue() >= Step.LAST_STEP_ID) {
            return;
        }

        Step nextStep = Step.RECORD;

        if (lastCompletedStep != null) {
            switch (lastCompletedStep) {
                case RECORD:
                case PLAY:
                case HIDE:
                case SHOW:
                    int count = CammentProvider.getCammentsSize();

                    if (count > 0) {
                        nextStep = Step.fromInt(lastCompletedStep.getIntValue() + 1);
                    }
                    break;
                case DELETE:
                    nextStep = Step.fromInt(lastCompletedStep.getIntValue() + 1);
                    break;
            }
        }

        displayTooltip(nextStep);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TutorialSkippedEvent event) {
        for (int i = 0; i <= 5; i++) {
            hideTooltipIfNeeded(Step.fromInt(i), false);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ShowTutorialTooltipEvent event) {
        displayTooltip(Step.TUTORIAL);
    }

}

package tv.camment.cammentsdk.views;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.events.ShowTutorialTooltipEvent;
import tv.camment.cammentsdk.events.TutorialContinueEvent;
import tv.camment.cammentsdk.events.TutorialSkippedEvent;


abstract class BaseCammentDrawerLayout extends DrawerLayout {

    private boolean peak = false;

    BaseCammentDrawerLayout(Context context) {
        super(context);
        init();
    }

    BaseCammentDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    BaseCammentDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        addDrawerListener(new SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (peak && slideOffset >= 0.2f) {
                    peak = false;
                    EventBus.getDefault().post(new ShowTutorialTooltipEvent());
                    closeDrawer(drawerView, true);
                }
            }
        });
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        View mDrawerListView = findViewById(R.id.drawer);

        return !(event.getX() > 30 && event.getAction() == MotionEvent.ACTION_DOWN)
                || isDrawerOpen(mDrawerListView)
                || isDrawerVisible(mDrawerListView);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TutorialSkippedEvent event) {
        View mDrawerListView = findViewById(R.id.drawer);

        if (mDrawerListView != null) {
            peak = true;
            openDrawer(mDrawerListView, true);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TutorialContinueEvent event) {
        View mDrawerListView = findViewById(R.id.drawer);

        if (mDrawerListView != null) {
            closeDrawer(mDrawerListView, true);
        }
    }

}

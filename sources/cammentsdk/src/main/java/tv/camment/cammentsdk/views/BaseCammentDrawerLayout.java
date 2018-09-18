package tv.camment.cammentsdk.views;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.events.ShowTutorialTooltipEvent;
import tv.camment.cammentsdk.events.TutorialContinueEvent;
import tv.camment.cammentsdk.events.TutorialSkippedEvent;
import tv.camment.cammentsdk.utils.LogUtils;


abstract class BaseCammentDrawerLayout extends DrawerLayout {

    private WindowManager windowManager;

    private Field mLeftDraggerField;
    private Field mRightDraggerField;
    private ViewDragHelper mRightDragger;
    private ViewDragHelper mLeftDragger;

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
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        try {
            mLeftDraggerField = DrawerLayout.class.getDeclaredField("mLeftDragger");
            mLeftDraggerField.setAccessible(true);
            mRightDraggerField = DrawerLayout.class.getDeclaredField("mRightDragger");
            mRightDraggerField.setAccessible(true);
        } catch (Exception e) {
            LogUtils.debug("drawer", "init error", e);
        }

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
        final View mDrawerListView = findViewById(R.id.drawer);

        openDrawerToOffset(100f);

        EventBus.getDefault().post(new ShowTutorialTooltipEvent());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                closeDrawer(mDrawerListView);
            }
        }, 500);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TutorialContinueEvent event) {
        View mDrawerListView = findViewById(R.id.drawer);

        if (mDrawerListView != null) {
            closeDrawer(mDrawerListView, true);
        }
    }

    private boolean isDrawerView(View child) {
        final int gravity = ((LayoutParams) child.getLayoutParams()).gravity;
        final int absGravity = GravityCompat.getAbsoluteGravity(gravity,
                ViewCompat.getLayoutDirection(child));
        if ((absGravity & Gravity.LEFT) != 0) {
            // This child is a left-edge drawer
            return true;
        }
        if ((absGravity & Gravity.RIGHT) != 0) {
            // This child is a right-edge drawer
            return true;
        }
        return false;
    }

    private void openDrawerToOffset(float slideOffset) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child == null || !isDrawerView(child)) {
                continue;
            }
            final int gravity = ((LayoutParams) child.getLayoutParams()).gravity;
            final int absGravity = GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(this));

            final ViewDragHelper helper;
            int finalLeft;
            if (absGravity == Gravity.LEFT) {
                helper = getLeftDragger();
                finalLeft = (int) (slideOffset - child.getWidth());
            } else {
                helper = getRightDragger();
                finalLeft = (int) (getDisplayWidth() - slideOffset);
            }

            helper.cancel();
            helper.smoothSlideViewTo(child, finalLeft, child.getTop());
            invalidate();
        }
    }

    private int getDisplayWidth() {
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    private ViewDragHelper getLeftDragger() {
        if (mLeftDragger == null) {
            try {
                mLeftDragger = (ViewDragHelper) this.mLeftDraggerField.get(this);
            } catch (Exception e) {
                LogUtils.debug("drawer", "error", e);
            }
        }
        return mLeftDragger;
    }

    private ViewDragHelper getRightDragger() {
        if (mRightDragger == null) {
            try {
                mRightDragger = (ViewDragHelper) this.mRightDraggerField.get(this);
            } catch (Exception e) {
                LogUtils.debug("drawer", "error", e);
            }
        }
        return mRightDragger;
    }

}

package tv.camment.cammentsdk.views;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import tv.camment.cammentsdk.R;


public final class CammentDrawerLayout extends DrawerLayout {

    public CammentDrawerLayout(Context context) {
        super(context);
    }

    public CammentDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CammentDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        View mDrawerListView = findViewById(R.id.drawer);

        return !(event.getX() > 30 && event.getAction() == MotionEvent.ACTION_DOWN)
                || isDrawerOpen(mDrawerListView)
                || isDrawerVisible(mDrawerListView);

    }
}

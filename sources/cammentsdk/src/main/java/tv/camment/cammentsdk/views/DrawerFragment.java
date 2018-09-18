package tv.camment.cammentsdk.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.events.ShowMetadataSetEvent;


public final class DrawerFragment extends Fragment
        implements OnDrawerViewListener {

    public static DrawerFragment newInstance() {
        return new DrawerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cmmsdk_fragment_drawer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.cmmsdk_group_container, GroupListFragment.newInstance()).commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void displayGroupInfo() {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.cmmsdk_slide_in_right, R.anim.cmmsdk_slide_out_left, R.anim.cmmsdk_slide_in_left, R.anim.cmmsdk_slide_out_right);
        ft.addToBackStack(null);
        ft.replace(R.id.cmmsdk_group_container, GroupInfoFragment.newInstance()).commit();
    }

    private void displayGroupList() {
        FragmentManager fm = getChildFragmentManager();

        if (fm.getBackStackEntryCount() != 0) {
            fm.popBackStack();
        } else {
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(R.anim.cmmsdk_slide_in_left, R.anim.cmmsdk_slide_out_right, R.anim.cmmsdk_slide_in_right, R.anim.cmmsdk_slide_out_left);
            ft.replace(R.id.cmmsdk_group_container, GroupListFragment.newInstance())
                    .commit();
        }
    }

    @Override
    public void switchGroupContainer() {
        FragmentManager fm = getChildFragmentManager();
        Fragment fragmentById = fm.findFragmentById(R.id.cmmsdk_group_container);
        if (fragmentById instanceof GroupInfoFragment) {
            displayGroupList();
        } else {
            displayGroupInfo();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ShowMetadataSetEvent event) {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.cmmsdk_group_container, GroupListFragment.newInstance()).commit();
    }

}

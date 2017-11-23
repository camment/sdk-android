package tv.camment.cammentsdk.views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.events.LoginStatusChanged;
import tv.camment.cammentsdk.helpers.AuthHelper;


public final class DrawerFragment extends Fragment
        implements OnSwitchViewListener {

    public static DrawerFragment newInstance() {
        return new DrawerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cmmsdk_fragment_drawer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (AuthHelper.getInstance().isLoggedIn()) {
            displayFbSignedInUser();
            ApiManager.getInstance().getUserApi().getMyUserGroups();
        } else {
            hideFbUserView();
        }

        displayGroupInfo();
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

    private void displayFbSignedInUser() {
        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction().replace(R.id.cmmsdk_user_container, FbUserFragment.newInstance()).commit();
    }

    private void displayFbLogoutUser() {
        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction().replace(R.id.cmmsdk_user_container, FbLogoutFragment.newInstance()).commit();
    }

    private void hideFbUserView() {
        FragmentManager fm = getChildFragmentManager();
        Fragment fragmentById = fm.findFragmentById(R.id.cmmsdk_user_container);
        if (fragmentById != null) {
            fm.beginTransaction().remove(fragmentById).commit();
        }

        displayGroupInfo();
    }

    private boolean isFbUserViewDisplayed() {
        FragmentManager fm = getChildFragmentManager();
        Fragment fragmentById = fm.findFragmentById(R.id.cmmsdk_user_container);
        return fragmentById != null;
    }

    private void displayGroupInfo() {
        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction().replace(R.id.cmmsdk_group_container, GroupInfoFragment.newInstance()).commit();
    }

    private void displayGroupList() {
        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction().replace(R.id.cmmsdk_group_container, GroupListFragment.newInstance()).commit();
    }

    @Override
    public void switchToFbUserInfo() {
        if (AuthHelper.getInstance().isLoggedIn()) {
            displayFbSignedInUser();
        } else {
            hideFbUserView();
        }
    }

    @Override
    public void switchToFbUserLogout() {
        displayFbLogoutUser();
    }

    @Override
    public void hideUserInfoContainer() {
        hideFbUserView();
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
    public void onMessageEvent(LoginStatusChanged event) {
        if (AuthHelper.getInstance().isLoggedIn()
                && !isFbUserViewDisplayed()) {
            displayFbSignedInUser();
            ApiManager.getInstance().getUserApi().getMyUserGroups();
        }
    }

}

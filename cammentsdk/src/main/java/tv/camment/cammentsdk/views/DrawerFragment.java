package tv.camment.cammentsdk.views;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.camment.clientsdk.model.Usergroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.events.FbLoginChangedEvent;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;
import tv.camment.cammentsdk.helpers.FacebookHelper;


public final class DrawerFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        OnSwitchViewListener {

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

        if (FacebookHelper.getInstance().isLoggedIn()) {
            displayFbSignedInUser();
        } else {
            hideFbUserView();
        }

        displayGroupInfo();

        //getLoaderManager().initLoader(1, null, this);

        //ApiManager.getInstance().getUserApi().getMyUserGroups();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //return UserGroupProvider.getUserGroupLoader();
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //List<CUserGroup> userGroups = UserGroupProvider.listFromCursor(data);
        //adapter.setData(userGroups);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
        if (FacebookHelper.getInstance().isLoggedIn()) {
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

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FbLoginChangedEvent event) {
        if (FacebookHelper.getInstance().isLoggedIn()
                && !isFbUserViewDisplayed()) {
            displayFbSignedInUser();
        }
    }

}

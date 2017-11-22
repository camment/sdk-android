package tv.camment.cammentsdk.views;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.camment.clientsdk.model.Usergroup;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.model.CUserGroup;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;


public final class GroupListFragment extends Fragment
        implements UserGroupAdapter.ActionListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView rvGroups;
    private UserGroupAdapter adapter;

    public static GroupListFragment newInstance() {
        return new GroupListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.cmmsdk_fragment_group_list, container, false);

        rvGroups = (RecyclerView) layout.findViewById(R.id.cmmsdk_rv_groups);

        setupRecyclerView();

        return layout;
    }

    private void setupRecyclerView() {
        adapter = new UserGroupAdapter(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvGroups.setLayoutManager(layoutManager);
        rvGroups.setAdapter(adapter);
        rvGroups.setItemAnimator(null);
        rvGroups.setHasFixedSize(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getLoaderManager().initLoader(1, null, this);

        ApiManager.getInstance().getUserApi().getMyUserGroups();
    }

    @Override
    public void onUserGroupClick(CUserGroup userGroup) {
        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        if (activeUserGroup != null) {
            UserGroupProvider.setActive(activeUserGroup.getUuid(), false);
        }

        UserGroupProvider.setActive(userGroup.getUuid(), true);

        EventBus.getDefault().post(new UserGroupChangeEvent());

        //CammentSDK.getInstance().connectToIoT();

        ApiManager.getInstance().getCammentApi().getUserGroupCamments();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return UserGroupProvider.getUserGroupLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<CUserGroup> userGroups = UserGroupProvider.listFromCursor(data);
        adapter.setData(userGroups);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}

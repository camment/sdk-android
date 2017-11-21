package tv.camment.cammentsdk.views;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.List;

import tv.camment.cammentsdk.BuildConfig;
import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.auth.CammentUserInfo;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.model.CUserGroup;

public class FbUserFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private ImageView ivAvatar;
    private TextView tvName;
    private TextView tvGroups;

    private OnSwitchViewListener onSwitchViewListener;

    public static FbUserFragment newInstance() {
        return new FbUserFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.cmmsdk_fragment_fb_user, container, false);

        ivAvatar = (ImageView) rootView.findViewById(R.id.cmmsdk_iv_avatar);
        tvName = (TextView) rootView.findViewById(R.id.cmmsdk_tv_name);
        tvGroups = (TextView) rootView.findViewById(R.id.cmmsdk_tv_groups);

        ImageButton ibSettings = (ImageButton) rootView.findViewById(R.id.cmmsdk_ib_settings);
        ibSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleOnSettingsClick();
            }
        });

        //TODO
//        ivAvatar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Profile profile = Profile.getCurrentProfile();
//                if (profile != null
//                        && profile.getLinkUri() != null) {
//                    Intent intent = new Intent(Intent.ACTION_VIEW, profile.getLinkUri());
//                    startActivity(intent);
//                }
//            }
//        });

        if (BuildConfig.SHOW_GROUP_LIST) {
            tvGroups.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onSwitchViewListener != null) {
                        onSwitchViewListener.switchGroupContainer();
                    }
                }
            });
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fillFbInfo();

        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof OnSwitchViewListener) {
            onSwitchViewListener = (OnSwitchViewListener) getParentFragment();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSwitchViewListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return UserGroupProvider.getUserGroupLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<CUserGroup> userGroups = UserGroupProvider.listFromCursor(data);
        if (userGroups == null
                || userGroups.size() == 0) {
            tvGroups.setText(R.string.cmmsdk_drawer_no_chat_groups);
        } else {
            tvGroups.setText(String.format(getString(R.string.cmmsdk_drawer_chat_groups), userGroups.size()));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void fillFbInfo() {
        CammentUserInfo userInfo = CammentSDK.getInstance().getAppAuthIdentityProvider().getUserInfo();

        if (userInfo != null) {

            Glide.with(CammentSDK.getInstance().getApplicationContext()).asBitmap().load(userInfo.getImageUrl()).into(new BitmapImageViewTarget(ivAvatar) {
                @Override
                protected void setResource(Bitmap resource) {
                    if (getContext() != null) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        ivAvatar.setImageDrawable(circularBitmapDrawable);
                    }
                }
            });

            tvName.setText(userInfo.getName());
        }
    }

    private void handleOnSettingsClick() {
        if (onSwitchViewListener != null) {
            onSwitchViewListener.switchToFbUserLogout();
        }
    }

}

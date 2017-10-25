package tv.camment.cammentsdk.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.facebook.Profile;
import com.facebook.internal.ImageRequest;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.helpers.FacebookHelper;

public class FbUserFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvName;

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

        fillFbInfo();

        ImageButton ibSettings = (ImageButton) rootView.findViewById(R.id.cmmsdk_ib_settings);
        ibSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleOnSettingsClick();
            }
        });

        return rootView;
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

    private void fillFbInfo() {
        if (FacebookHelper.getInstance().isLoggedIn()) {
            Profile profile = Profile.getCurrentProfile();

            if (profile != null) {
                Uri pictureUri = ImageRequest.getProfilePictureUri(profile.getId(), 270, 270);

                Glide.with(CammentSDK.getInstance().getApplicationContext()).asBitmap().load(pictureUri).into(new BitmapImageViewTarget(ivAvatar) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        ivAvatar.setImageDrawable(circularBitmapDrawable);
                    }
                });

                tvName.setText(profile.getName());
            }
        }
    }

    private void handleOnSettingsClick() {
        if (onSwitchViewListener != null) {
            onSwitchViewListener.switchToFbUserLogout();
        }
    }

}

package tv.camment.cammentsdk.views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.auth.CammentUserInfo;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.views.dialogs.LogoutCammentDialog;

public class FbUserFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvName;
    private RequestOptions requestOptions;

    public static FbUserFragment newInstance() {
        return new FbUserFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.cmmsdk_fragment_fb_user, container, false);

        ivAvatar = rootView.findViewById(R.id.cmmsdk_iv_avatar);
        tvName = rootView.findViewById(R.id.cmmsdk_tv_name);

        ImageButton ibSettings = rootView.findViewById(R.id.cmmsdk_ib_settings);
        ibSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleOnLogoutClicked();
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fillFbInfo();
    }

    private void fillFbInfo() {
        CammentUserInfo userInfo = CammentSDK.getInstance().getAppAuthIdentityProvider().getUserInfo();

        if (userInfo != null) {
            if (requestOptions == null) {
                requestOptions = new RequestOptions().placeholder(R.drawable.cmmsdk_user).error(R.drawable.cmmsdk_user).dontAnimate().circleCrop();
            }

            Glide.with(CammentSDK.getInstance().getApplicationContext())
                    .asBitmap().load(userInfo.getImageUrl())
                    .apply(requestOptions)
                    .into(ivAvatar);

            tvName.setText(userInfo.getName());
        }
    }

    private void handleOnLogoutClicked() {
        BaseMessage message = new BaseMessage();
        message.type = MessageType.LOGOUT_CONFIRMATION;

        LogoutCammentDialog.createInstance(message).show();
    }

}

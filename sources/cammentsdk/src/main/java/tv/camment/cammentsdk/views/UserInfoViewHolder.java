package tv.camment.cammentsdk.views;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.data.model.CUserInfo;
import tv.camment.cammentsdk.data.model.UserState;
import tv.camment.cammentsdk.helpers.IdentityPreferences;


final class UserInfoViewHolder extends RecyclerView.ViewHolder {

    private CUserInfo userInfo;

    private ImageView ivAvatar;
    private TextView tvName;
    private ImageButton ibRemove;

    private final UserInfoAdapter.ActionListener actionListener;
    private RequestOptions requestOptions;

    UserInfoViewHolder(final View itemView, final UserInfoAdapter.ActionListener actionListener) {
        super(itemView);

        this.actionListener = actionListener;

        ivAvatar = itemView.findViewById(R.id.cmmsdk_iv_avatar);
        tvName = itemView.findViewById(R.id.cmmsdk_tv_name);
        ibRemove = itemView.findViewById(R.id.cmmsdk_ib_remove);
    }

    void bindData(final CUserInfo userInfo, boolean isMyGroup, String hostId) {
        if (userInfo == null)
            return;

        this.userInfo = userInfo;

        final boolean showDisabledView = userInfo.getUserState() == UserState.BLOCKED || !userInfo.getIsOnline();

        tvName.setText(userInfo.getName());
        tvName.setAlpha(showDisabledView ? 0.6f : 1.0f);

        if (userInfo.getIsOnline()) {
            tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.cmmsdk_online_circle, 0);
        } else {
            tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.cmmsdk_offline_circle, 0);
        }

        if (CammentSDK.getInstance().isSyncEnabled()
                && TextUtils.equals(userInfo.getUserCognitoIdentityId(), hostId)) {
            tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.cmmsdk_host, 0);
        }

        if (requestOptions == null) {
            requestOptions = new RequestOptions().placeholder(R.drawable.cmmsdk_user).error(R.drawable.cmmsdk_user).dontAnimate().circleCrop();
        }

        Glide.with(CammentSDK.getInstance().getApplicationContext())
                .asBitmap().load(userInfo.getPicture())
                .apply(requestOptions)
                .into(ivAvatar);

        if (showDisabledView) {
            ivAvatar.clearColorFilter();
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            ivAvatar.setColorFilter(filter);
        } else {
            ivAvatar.clearColorFilter();
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(1);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            ivAvatar.setColorFilter(filter);
        }

        ibRemove.setImageResource(userInfo.getUserState() == UserState.BLOCKED ? R.drawable.cmmsdk_blocked : R.drawable.cmmsdk_block);
        ibRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleUserRemoveClick();
            }
        });

        String identityId = IdentityPreferences.getInstance().getIdentityId();

        ibRemove.setVisibility(isMyGroup && !TextUtils.equals(userInfo.getUserCognitoIdentityId(), identityId) ? View.VISIBLE : View.GONE);
    }

    private void handleUserRemoveClick() {
        if (actionListener != null) {
            actionListener.onUserBlockClick(userInfo);
        }
    }

}

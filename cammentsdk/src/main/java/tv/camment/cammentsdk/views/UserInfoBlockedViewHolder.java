package tv.camment.cammentsdk.views;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.data.model.CUserInfo;


final class UserInfoBlockedViewHolder extends RecyclerView.ViewHolder {

    private CUserInfo userInfo;

    private ImageView ivAvatar;
    private TextView tvName;
    private ImageButton ibUnblock;

    private final UserInfoAdapter.ActionListener actionListener;

    UserInfoBlockedViewHolder(final View itemView, final UserInfoAdapter.ActionListener actionListener) {
        super(itemView);

        this.actionListener = actionListener;

        ivAvatar = (ImageView) itemView.findViewById(R.id.cmmsdk_iv_avatar);
        tvName = (TextView) itemView.findViewById(R.id.cmmsdk_tv_name);
        ibUnblock = (ImageButton) itemView.findViewById(R.id.cmmsdk_ib_block);
    }

    void bindData(CUserInfo userInfo, boolean isMyGroup) {
        if (userInfo == null)
            return;

        this.userInfo = userInfo;

        tvName.setText(userInfo.getName());

        Glide.with(CammentSDK.getInstance().getApplicationContext()).asBitmap().load(userInfo.getPicture()).into(new BitmapImageViewTarget(ivAvatar) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(itemView.getContext().getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                ivAvatar.setImageDrawable(circularBitmapDrawable);

                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                ivAvatar.setColorFilter(filter);
            }
        });

        ibUnblock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleUserUnblockClick();
            }
        });

        ibUnblock.setVisibility(isMyGroup ? View.VISIBLE : View.GONE);
    }

    private void handleUserUnblockClick() {
        if (actionListener != null) {
            actionListener.onUserBlockClick(userInfo);
        }
    }

}

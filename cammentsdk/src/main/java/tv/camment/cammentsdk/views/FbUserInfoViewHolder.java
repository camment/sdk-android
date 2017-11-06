package tv.camment.cammentsdk.views;

import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.data.model.CUserInfo;


final class FbUserInfoViewHolder extends RecyclerView.ViewHolder {

    private CUserInfo userInfo;

    private ImageView ivAvatar;
    private TextView tvName;

    FbUserInfoViewHolder(final View itemView) {
        super(itemView);

        ivAvatar = (ImageView) itemView.findViewById(R.id.cmmsdk_iv_avatar);
        tvName = (TextView) itemView.findViewById(R.id.cmmsdk_tv_name);
    }

    void bindData(CUserInfo userInfo) {
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
            }
        });
    }

}

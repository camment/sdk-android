package tv.camment.cammentsdk.views;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.helpers.IdentityPreferences;
import tv.camment.cammentsdk.utils.CommonUtils;
import tv.camment.cammentsdk.utils.FileUtils;


final class CammentViewHolder extends RecyclerView.ViewHolder {

    private final CammentsAdapter.ActionListener actionListener;
    private CCamment camment;

    private SquareFrameLayout sflContainer;

    private ImageView ivThumbnail;
    private TextureView textureView;

    private ImageView ivCheck;
    private ImageView ivPlay;

    private LinearLayout llSeen;

    private RequestOptions requestOptions;

    CammentViewHolder(final View itemView, final CammentsAdapter.ActionListener actionListener) {
        super(itemView);

        this.actionListener = actionListener;

        sflContainer = itemView.findViewById(R.id.cmmsdk_sfl_container);

        sflContainer.setPivotX(0);
        sflContainer.setPivotY(0);

        ivThumbnail = itemView.findViewById(R.id.cmmsdk_iv_thumbnail);

        ivCheck = itemView.findViewById(R.id.cmmsdk_iv_check);

        ivPlay = itemView.findViewById(R.id.cmmsdk_iv_play);

        llSeen = itemView.findViewById(R.id.cmmsdk_ll_seen);

        sflContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleOnItemClick();
            }
        });

        sflContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final String cognitoId = IdentityPreferences.getInstance().getIdentityId();
                final String oldCognitoId = IdentityPreferences.getInstance().getOldIdentityId();

                if (actionListener != null) {
                    actionListener.onCammentBottomSheetDisplayed();
                }

                if (camment.getUserCognitoIdentityId() == null) {
                    camment.setUserCognitoIdentityId("");
                }

                if (TextUtils.equals(camment.getUserCognitoIdentityId(), cognitoId)
                        || TextUtils.isEmpty(camment.getUserCognitoIdentityId())
                        || TextUtils.equals(camment.getUserCognitoIdentityId(), oldCognitoId)) { //TODO this should be removed as server should overwrite this in camment
                    CammentBottomSheetDialog dialog = new CammentBottomSheetDialog(itemView.getContext());
                    dialog.setCamment(camment);
                    dialog.show();
                }
                return true;
            }
        });

        setItemViewScale(SDKConfig.CAMMENT_SMALL);
    }

    private void handleOnItemClick() {
        if (camment != null && actionListener != null) {
            if (!camment.isSeen()) {
                CammentProvider.setCammentSeen(camment.getUuid());
            }
            llSeen.setVisibility(View.GONE);

            if (getItemViewScale() == SDKConfig.CAMMENT_SMALL) {
                textureView = new SquareTextureView(itemView.getContext());
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
                int dp2 = CommonUtils.dpToPx(CammentSDK.getInstance().getApplicationContext(), 2);
                params.setMargins(dp2, dp2, dp2, dp2);
                sflContainer.addView(textureView, 1, params);
            }
            actionListener.onCammentClick(this, camment, textureView);
        }
    }

    void setItemViewScale(float scale) {
        if (getItemViewScale() != scale) {
            sflContainer.setCustomScale(scale);
            if (scale == SDKConfig.CAMMENT_SMALL) {
                setThumbnailVisibility(View.VISIBLE);
            }
            if (textureView != null
                    && scale == SDKConfig.CAMMENT_SMALL) {
                sflContainer.removeView(textureView);
            }

            ivPlay.setVisibility(scale == SDKConfig.CAMMENT_SMALL ? View.VISIBLE : View.INVISIBLE);
        }
    }

    void stopCammentIfPlaying() {
        if (actionListener != null) {
            actionListener.stopCammentIfPlaying(camment);
        }
    }

    private void setThumbnailVisibility(int visibility) {
        ivThumbnail.setVisibility(visibility);
    }

    float getItemViewScale() {
        return sflContainer.getCustomScale();
    }

    void bindData(CCamment camment) {
        if (camment == null)
            return;

        this.camment = camment;

        if (FileUtils.getInstance().isLocalVideoAvailable(camment.getUuid())) {
            loadLocalThumbnail();
        } else {
            loadThumbnailFromServer();
        }

        llSeen.setVisibility(camment.isSeen() ? View.GONE : View.VISIBLE);

        itemView.post(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams ivPlayParams = (FrameLayout.LayoutParams) ivPlay.getLayoutParams();
                ivPlayParams.width = itemView.getMeasuredHeight() / 4;
                ivPlayParams.height = itemView.getMeasuredHeight() / 4;
                ivPlayParams.setMargins(ivPlayParams.width / 3, 0, 0, ivPlayParams.width / 3);
                ivPlay.setLayoutParams(ivPlayParams);
            }
        });
    }

    private void loadLocalThumbnail() {
        Glide.with(CammentSDK.getInstance().getApplicationContext())
                .load(FileUtils.getInstance().getUploadCammentPath(camment.getUuid()))
                .into(ivThumbnail);

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        ivThumbnail.setColorFilter(filter);
    }


    private void loadThumbnailFromServer() {
        Glide.with(CammentSDK.getInstance().getApplicationContext())
                .load(camment.getThumbnail())
                .into(ivThumbnail);
    }

}
package tv.camment.cammentsdk.views;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.camment.clientsdk.model.Camment;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.api.ApiManager;

final class CammentBottomSheetDialog extends BottomSheetDialog implements DialogInterface.OnShowListener {

    private Camment camment;

    CammentBottomSheetDialog(@NonNull Context context) {
        super(context);
        init();
    }

    CammentBottomSheetDialog(@NonNull Context context, @StyleRes int theme) {
        super(context, theme);
        init();
    }

    CammentBottomSheetDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    private void init() {
        setContentView(R.layout.cmmsdk_camment_bottom_sheet);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        setOnShowListener(this);

        TextView tvDeleteCamment = (TextView) findViewById(R.id.cmmsdk_tv_delete_camment);

        if (tvDeleteCamment != null) {
            tvDeleteCamment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (camment != null) {
                        ApiManager.getInstance().getCammentApi().deleteUserGroupCamment(camment);
                    }
                    dismiss();
                }
            });
        }

        TextView tvCancel = (TextView) findViewById(R.id.cmmsdk_tv_cancel);

        if (tvCancel != null) {
            tvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancel();
                }
            });
        }
    }

    public void setCamment(Camment camment) {
        this.camment = camment;
    }


    @Override
    public void onShow(DialogInterface dialogInterface) {
        if (dialogInterface instanceof BottomSheetDialog) {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = (FrameLayout) dialog.findViewById(android.support.design.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet)
                        .setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }
}

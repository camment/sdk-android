package tv.camment.cammentdemo;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import tv.camment.cammentsdk.helpers.GeneralPreferences;

public final class CammentPasscodeDialog extends DialogFragment {

    private EditText etPasscode;
    private Button btnPositive;
    private Button btnNegative;

    private ActionListener actionListener;

    static CammentPasscodeDialog createInstance() {
        return new CammentPasscodeDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            dismiss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null) {
            Window window = getDialog().getWindow();
            if (window != null) {
                window.requestFeature(Window.FEATURE_NO_TITLE);
            }
        }

        View view = inflater.inflate(R.layout.camment_passcode_dialog, container);

        etPasscode = (EditText) view.findViewById(R.id.et_passcode);
        etPasscode.setText(GeneralPreferences.getInstance().getProviderPasscode());
        etPasscode.setSelection(etPasscode.getText().length());

        btnPositive = (Button) view.findViewById(R.id.btn_positive);
        btnNegative = (Button) view.findViewById(R.id.btn_negative);

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionListener != null) {
                    actionListener.onPositiveButtonClick(etPasscode.getText().toString());
                }
                dismiss();
            }
        });

        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return view;
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public interface ActionListener {

        void onPositiveButtonClick(String passcode);

    }

}

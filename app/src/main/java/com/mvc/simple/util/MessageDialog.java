package com.mvc.simple.util;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.mvc.simple.R;

public class MessageDialog extends DialogFragment {
    private static final String TAG = "MessageDialog";

    public OnClick listener;
    public OnClicks listeners;
    public TextView tvMsg;
    public TextView btCancel;
    public TextView btOk;

    String tvMsgText = "", cancelTxt = "", okTxt = "";
    static MessageDialog msgDialog;

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(0, R.style.MaterialDialogSheet);
        setCancelable(false);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_ok, container, false);

        btOk = view.findViewById(R.id.btOk);
        btCancel = view.findViewById(R.id.btCancel);
        tvMsg = view.findViewById(R.id.tvMsg);

        btOk.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOkClick();
            }
            dismiss();
        });

        btCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelClick();
            }
            dismiss();
        });
        return view;
    }

    public static MessageDialog getInstance() {
        if (msgDialog == null)
            msgDialog = new MessageDialog();
        return msgDialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            tvMsgText = getArguments().getString("tvMsgText", null);

            cancelTxt = getArguments().getString("cancelTxt", "");
            okTxt = getArguments().getString("okTxt", "");

            tvMsg.setVisibility(tvMsgText == null ? View.GONE : View.VISIBLE);

            if (TextUtils.isEmpty(cancelTxt)) {
                btCancel.setVisibility(View.GONE);
            } else {
                btCancel.setVisibility(View.VISIBLE);
            }

            btCancel.setText(cancelTxt);
            btOk.setText(okTxt);

            tvMsg.setText(tvMsgText);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        listener = null;
    }

    // Interface
    public interface OnClick {
        void onOkClick();
        void onCancelClick();
    }
    public void setListener(OnClicks listener) {
        this.listeners = listener;
    }

    public interface OnClicks {
        public void set(boolean ok);
    }


    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        try {
            // Add a remove transaction before each add transaction to prevent continuous add
            manager.beginTransaction().remove(this).commit();
            super.show(manager, tag);
        } catch (Exception e) {
            // The same instance will use different tags will be an exception, capture here
            e.printStackTrace();
        }
    }
}

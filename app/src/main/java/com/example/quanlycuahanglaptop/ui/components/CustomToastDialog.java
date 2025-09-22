package com.example.quanlycuahanglaptop.ui.components;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.quanlycuahanglaptop.R;
import com.google.android.material.button.MaterialButton;

public class CustomToastDialog extends DialogFragment {

    public interface OnPrimaryClickListener {
        void onClick();
    }

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_MESSAGE = "arg_message";
    private static final String ARG_BUTTON = "arg_button";

    private OnPrimaryClickListener listener;

    public static CustomToastDialog newInstance(String title, String message, String buttonText) {
        CustomToastDialog dialog = new CustomToastDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_BUTTON, buttonText);
        dialog.setArguments(args);
        dialog.setCancelable(true);
        return dialog;
    }

    public void setOnPrimaryClickListener(OnPrimaryClickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.dialog_custom_toast, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null || getDialog().getWindow() == null) return;
        Window window = getDialog().getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
        window.getAttributes().windowAnimations = R.style.DialogFadeScaleAnimation;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        String title = args != null ? args.getString(ARG_TITLE, "") : "";
        String message = args != null ? args.getString(ARG_MESSAGE, "") : "";
        String buttonText = args != null ? args.getString(ARG_BUTTON, getString(android.R.string.ok)) : getString(android.R.string.ok);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        MaterialButton btnPrimary = view.findViewById(R.id.btnPrimary);

        tvTitle.setText(title);
        tvMessage.setText(message);
        btnPrimary.setText(buttonText);

        btnPrimary.setOnClickListener(v -> {
            if (listener != null) listener.onClick();
            dismissAllowingStateLoss();
        });
    }
}



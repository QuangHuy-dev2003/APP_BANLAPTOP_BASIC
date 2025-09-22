package com.example.quanlycuahanglaptop.ui.components;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.quanlycuahanglaptop.R;
import com.google.android.material.button.MaterialButton;

public class DeleteConfirmationDialog extends DialogFragment {

    public interface OnDeleteConfirmListener {
        void onConfirm();
        void onCancel();
    }

    private static final String ARG_PRODUCT_NAME = "arg_product_name";

    private OnDeleteConfirmListener listener;

    public static DeleteConfirmationDialog newInstance(String productName) {
        DeleteConfirmationDialog dialog = new DeleteConfirmationDialog();
        Bundle args = new Bundle();
        args.putString(ARG_PRODUCT_NAME, productName);
        dialog.setArguments(args);
        dialog.setCancelable(true);
        return dialog;
    }

    public void setOnDeleteConfirmListener(OnDeleteConfirmListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.dialog_delete_confirmation, container, false);
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
        String productName = args != null ? args.getString(ARG_PRODUCT_NAME, "") : "";

        ImageView ivWarningIcon = view.findViewById(R.id.iv_warning_icon);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvMessage = view.findViewById(R.id.tv_message);
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel);
        MaterialButton btnDelete = view.findViewById(R.id.btn_delete);

        // Thiết lập nội dung
        tvTitle.setText("Xác nhận xóa");
        if (!productName.isEmpty()) {
            tvMessage.setText("Bạn có chắc chắn muốn xóa \"" + productName + "\" khỏi giỏ hàng?");
        } else {
            tvMessage.setText("Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?");
        }

        // Xử lý nút hủy
        btnCancel.setOnClickListener(v -> {
            if (listener != null) listener.onCancel();
            dismissAllowingStateLoss();
        });

        // Xử lý nút xóa
        btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onConfirm();
            dismissAllowingStateLoss();
        });
    }
}

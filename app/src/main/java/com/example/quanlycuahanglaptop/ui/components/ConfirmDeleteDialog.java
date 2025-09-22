package com.example.quanlycuahanglaptop.ui.components;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.quanlycuahanglaptop.R;

/**
 * Dialog xác nhận xóa sản phẩm
 * Hiển thị thông tin sản phẩm và yêu cầu người dùng xác nhận trước khi xóa
 */
public class ConfirmDeleteDialog extends DialogFragment {

    public interface OnConfirmListener {
        void onConfirm();
        void onCancel();
    }

    private OnConfirmListener listener;
    private String productName;

    /**
     * Factory method để tạo dialog với tên sản phẩm
     */
    public static ConfirmDeleteDialog newInstance(String productName) {
        ConfirmDeleteDialog dialog = new ConfirmDeleteDialog();
        Bundle args = new Bundle();
        args.putString("productName", productName);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productName = getArguments().getString("productName", "sản phẩm này");
        }
        // Thiết lập style cho dialog
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_confirm_delete, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Thiết lập dialog có thể đóng bằng cách bấm bên ngoài
        if (getDialog() != null) {
            getDialog().setCancelable(true);
            getDialog().setCanceledOnTouchOutside(true);
            // Đảm bảo dialog có kích thước phù hợp
            getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            // Làm nền cửa sổ trong suốt để thấy bo góc của layout
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        
        // Tìm các view components
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);
        
        // Hiển thị tên sản phẩm trong message
        if (tvMessage != null) {
            tvMessage.setText("Bạn chắc chắn muốn xoá " + productName + "?");
        }
        
        // Xử lý nút Huỷ
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancel();
                }
                dismiss();
            });
        }
        
        // Xử lý nút Đồng ý
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConfirm();
                }
                dismiss();
            });
        }
    }

    /**
     * Thiết lập listener để xử lý sự kiện xác nhận/hủy
     */
    public void setOnConfirmListener(OnConfirmListener listener) {
        this.listener = listener;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Đảm bảo dialog có kích thước phù hợp khi hiển thị
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            // Giữ nền cửa sổ trong suốt khi hiển thị
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onCancel(@NonNull android.content.DialogInterface dialog) {
        super.onCancel(dialog);
        // Xử lý khi dialog bị đóng bằng cách bấm bên ngoài hoặc nút back
        if (listener != null) {
            listener.onCancel();
        }
    }
}

package com.example.quanlycuahanglaptop.ui.checkout;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.util.CustomToast;

public class PaymentMethodDialog extends DialogFragment {

    public interface OnPaymentMethodSelectedListener {
        void onPaymentMethodSelected(String method);
    }

    private OnPaymentMethodSelectedListener listener;
    private String currentMethod = "Thanh toán khi nhận hàng";

    public static PaymentMethodDialog newInstance() {
        return new PaymentMethodDialog();
    }

    public void setOnPaymentMethodSelectedListener(OnPaymentMethodSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.PaymentMethodDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_payment_method, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = android.view.Gravity.BOTTOM;
            window.setAttributes(params);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Tìm các view
        ImageView btnClose = view.findViewById(R.id.btn_close);
        LinearLayout layoutCod = view.findViewById(R.id.layout_cod);
        LinearLayout layoutVnpay = view.findViewById(R.id.layout_vnpay);
        LinearLayout layoutBankTransfer = view.findViewById(R.id.layout_bank_transfer);
        TextView btnConfirm = view.findViewById(R.id.btn_confirm);

        // Xử lý đóng dialog
        btnClose.setOnClickListener(v -> dismiss());

        // Xử lý chọn COD (mặc định)
        layoutCod.setOnClickListener(v -> {
            currentMethod = "Thanh toán khi nhận hàng";
            updateSelection(view);
        });

        // Xử lý chọn VNPAY (không khả dụng)
        layoutVnpay.setOnClickListener(v -> {
            CustomToast.showWarning(getContext(), "Chức năng đang được phát triển");
        });

        // Xử lý chọn chuyển khoản (không khả dụng)
        layoutBankTransfer.setOnClickListener(v -> {
            CustomToast.showWarning(getContext(), "Chức năng đang được phát triển");
        });

        // Xử lý nút xác nhận
        btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentMethodSelected(currentMethod);
            }
            dismiss();
        });

        // Thiết lập trạng thái ban đầu
        updateSelection(view);
    }

    private void updateSelection(View view) {
        LinearLayout layoutCod = view.findViewById(R.id.layout_cod);
        LinearLayout layoutVnpay = view.findViewById(R.id.layout_vnpay);
        LinearLayout layoutBankTransfer = view.findViewById(R.id.layout_bank_transfer);

        // Reset tất cả về trạng thái bình thường
        layoutCod.setBackgroundResource(R.drawable.bg_payment_method_selected);
        layoutVnpay.setBackgroundResource(R.drawable.bg_payment_method_disabled);
        layoutBankTransfer.setBackgroundResource(R.drawable.bg_payment_method_disabled);

        // Hiển thị trạng thái cho phương thức được chọn
        ImageView statusCod = layoutCod.findViewById(R.id.ic_status);
        if (statusCod != null) {
            statusCod.setImageResource(R.drawable.ic_status_available);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}

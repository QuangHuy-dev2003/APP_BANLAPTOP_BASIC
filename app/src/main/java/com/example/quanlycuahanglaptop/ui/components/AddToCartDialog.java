package com.example.quanlycuahanglaptop.ui.components;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.domain.Product;
import com.google.android.material.button.MaterialButton;

import java.io.File;

public class AddToCartDialog extends DialogFragment {

    public interface OnAddToCartClickListener {
        void onAddToCart(Product product, int quantity);
    }

    private static final String ARG_PRODUCT = "arg_product";

    private Product product;
    private int currentQuantity = 1;
    private int maxQuantity;
    private OnAddToCartClickListener listener;

    // UI Components
    private ImageView imgProduct;
    private TextView tvProductName;
    private TextView tvProductPrice;
    private TextView tvStockInfo;
    private ImageButton btnDecrease;
    private ImageButton btnIncrease;
    private TextView tvQuantity;
    private MaterialButton btnAddToCart;
    private MaterialButton btnCancel;

    public static AddToCartDialog newInstance(Product product) {
        AddToCartDialog dialog = new AddToCartDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, product);
        dialog.setArguments(args);
        dialog.setCancelable(true);
        return dialog;
    }

    public void setOnAddToCartClickListener(OnAddToCartClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            product = (Product) args.getSerializable(ARG_PRODUCT);
            if (product != null) {
                maxQuantity = product.getQuantity();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.dialog_add_to_cart, container, false);
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
        
        initViews(view);
        setupProductInfo();
        setupQuantityControls();
        setupButtons();
    }

    private void initViews(View view) {
        imgProduct = view.findViewById(R.id.imgProduct);
        tvProductName = view.findViewById(R.id.tvProductName);
        tvProductPrice = view.findViewById(R.id.tvProductPrice);
        tvStockInfo = view.findViewById(R.id.tvStockInfo);
        btnDecrease = view.findViewById(R.id.btnDecrease);
        btnIncrease = view.findViewById(R.id.btnIncrease);
        tvQuantity = view.findViewById(R.id.tvQuantity);
        btnAddToCart = view.findViewById(R.id.btnAddToCart);
        btnCancel = view.findViewById(R.id.btnCancel);
    }

    private void setupProductInfo() {
        if (product == null) return;

        // Set product info
        tvProductName.setText(product.getName());
        tvProductPrice.setText(String.format("%,.0f đ", product.getPrice()));
        tvStockInfo.setText(String.format("Còn lại: %d sản phẩm", maxQuantity));

        // Set product image
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            File file = new File(product.getImage());
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) {
                    imgProduct.setImageBitmap(bitmap);
                } else {
                    imgProduct.setImageResource(R.drawable.ic_products);
                }
            } else {
                imgProduct.setImageResource(R.drawable.ic_products);
            }
        } else {
            imgProduct.setImageResource(R.drawable.ic_products);
        }
    }

    private void setupQuantityControls() {
        updateQuantityDisplay();
        updateButtonStates();

        btnDecrease.setOnClickListener(v -> {
            if (currentQuantity > 1) {
                currentQuantity--;
                updateQuantityDisplay();
                updateButtonStates();
            }
        });

        btnIncrease.setOnClickListener(v -> {
            if (currentQuantity < maxQuantity) {
                currentQuantity++;
                updateQuantityDisplay();
                updateButtonStates();
            }
        });
    }

    private void updateQuantityDisplay() {
        tvQuantity.setText(String.valueOf(currentQuantity));
    }

    private void updateButtonStates() {
        // Disable decrease button if quantity is 1
        btnDecrease.setEnabled(currentQuantity > 1);
        btnDecrease.setAlpha(currentQuantity > 1 ? 1.0f : 0.5f);

        // Disable increase button if quantity reaches max
        btnIncrease.setEnabled(currentQuantity < maxQuantity);
        btnIncrease.setAlpha(currentQuantity < maxQuantity ? 1.0f : 0.5f);
    }

    private void setupButtons() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnAddToCart.setOnClickListener(v -> {
            if (listener != null && product != null) {
                listener.onAddToCart(product, currentQuantity);
            }
            dismiss();
        });
    }
}

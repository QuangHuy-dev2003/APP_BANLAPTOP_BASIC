package com.example.quanlycuahanglaptop.ui.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.domain.Product;
import com.example.quanlycuahanglaptop.service.CartService;
import com.example.quanlycuahanglaptop.service.ProductService;
import com.example.quanlycuahanglaptop.ui.components.AddToCartDialog;
import com.example.quanlycuahanglaptop.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.io.File;

public class ProductDetailFragment extends Fragment {

    public static final String ARG_PRODUCT_ID = "product_id";

    private ProductService productService;
    private CartService cartService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productService = new ProductService(requireContext());
        cartService = new CartService(requireContext());

        ImageView img = view.findViewById(R.id.imgProduct);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        TextView tvDesc = view.findViewById(R.id.tvDescription);
        MaterialButton btnAdd = view.findViewById(R.id.btnAddToCart);
        View btnBack = view.findViewById(R.id.btnBack);

        long productId = requireArguments().getLong(ARG_PRODUCT_ID, -1);
        if (productId <= 0) return;

        Product p = productService.findById(productId);
        if (p == null) return;

        tvName.setText(p.getName());
        tvPrice.setText(String.format("%,.0f đ", p.getPrice()));
        tvDesc.setText(p.getDescription() != null ? p.getDescription() : "");

        if (p.getImage() != null && !p.getImage().isEmpty()) {
            File f = new File(p.getImage());
            if (f.exists()) {
                Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath());
                if (b != null) img.setImageBitmap(b); else img.setImageResource(R.drawable.ic_products);
            } else {
                img.setImageResource(R.drawable.ic_products);
            }
        } else {
            img.setImageResource(R.drawable.ic_products);
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        }

        btnAdd.setOnClickListener(v -> {
            SessionManager sm = new SessionManager(requireContext());
            if (!sm.isLoggedIn()) {
                // Hiển thị dialog yêu cầu đăng nhập
                com.example.quanlycuahanglaptop.ui.components.CustomToastDialog dialog = com.example.quanlycuahanglaptop.ui.components.CustomToastDialog.newInstance(
                        getString(R.string.title_require_login),
                        getString(R.string.msg_require_login),
                        getString(R.string.btn_agree)
                );
                dialog.show(getParentFragmentManager(), "require_login_dialog");
            } else {
                // Hiển thị dialog xác nhận thêm vào giỏ hàng
                showAddToCartDialog(p);
            }
        });
    }

    private void showAddToCartDialog(Product product) {
        AddToCartDialog dialog = AddToCartDialog.newInstance(product);
        dialog.setOnAddToCartClickListener((selectedProduct, quantity) -> {
            SessionManager sm = new SessionManager(requireContext());
            long userId = sm.getUserId();
            
            try {
                cartService.addToCart(userId, selectedProduct.getId() != null ? selectedProduct.getId() : 0L, quantity);
                com.example.quanlycuahanglaptop.util.CustomToast.showSuccess(requireContext(), getString(R.string.added_to_cart_success));
                
                // Cập nhật badge giỏ hàng nếu đang ở HomeFragment
                refreshCartBadgeIfNeeded();
            } catch (Exception e) {
                com.example.quanlycuahanglaptop.util.CustomToast.showError(requireContext(), "Có lỗi xảy ra khi thêm vào giỏ hàng");
            }
        });
        dialog.show(getParentFragmentManager(), "add_to_cart_dialog");
    }
    
    private void refreshCartBadgeIfNeeded() {
        // Tìm HomeFragment trong back stack và cập nhật badge
        androidx.fragment.app.FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        java.util.List<androidx.fragment.app.Fragment> fragments = fragmentManager.getFragments();
        
        for (androidx.fragment.app.Fragment fragment : fragments) {
            if (fragment instanceof HomeFragment && fragment.isVisible()) {
                // Nếu HomeFragment đang hiển thị, cập nhật badge
                ((HomeFragment) fragment).refreshCartBadge();
                break;
            }
        }
    }
}



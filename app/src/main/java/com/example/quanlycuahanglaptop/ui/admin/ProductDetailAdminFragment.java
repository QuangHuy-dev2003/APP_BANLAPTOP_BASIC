package com.example.quanlycuahanglaptop.ui.admin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.domain.Product;
import com.example.quanlycuahanglaptop.service.ProductService;
import com.example.quanlycuahanglaptop.ui.components.ConfirmDeleteDialog;
import com.example.quanlycuahanglaptop.util.CustomToast;

import java.io.File;

/**
 * Fragment hiển thị chi tiết sản phẩm cho admin
 * Hiển thị đầy đủ thông tin sản phẩm và các nút hành động
 */
public class ProductDetailAdminFragment extends Fragment {

    private ProductService productService;
    private Product currentProduct; // Sản phẩm đang xem chi tiết
    
    // UI Components
    private ImageView imgProduct;
    private TextView tvProductName, tvProductPrice, tvProductQuantity, tvProductDescription;
    private TextView tvProductId, tvStockStatus, tvStockValue;
    private Button btnEdit, btnDelete;

    /**
     * Factory method để tạo fragment với dữ liệu sản phẩm
     */
    public static ProductDetailAdminFragment newInstance(Product product) {
        ProductDetailAdminFragment fragment = new ProductDetailAdminFragment();
        Bundle args = new Bundle();
        args.putSerializable("product", product);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_detail_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Khởi tạo service và UI components
        productService = new ProductService(requireContext());
        initViews(view);
        
        // Load dữ liệu sản phẩm từ arguments
        loadProductData();
        
        // Thiết lập event listeners
        setupEventListeners();
    }

    /**
     * Khởi tạo các view components
     */
    private void initViews(View view) {
        imgProduct = view.findViewById(R.id.imgProduct);
        tvProductName = view.findViewById(R.id.tvProductName);
        tvProductPrice = view.findViewById(R.id.tvProductPrice);
        tvProductQuantity = view.findViewById(R.id.tvProductQuantity);
        tvProductDescription = view.findViewById(R.id.tvProductDescription);
        tvProductId = view.findViewById(R.id.tvProductId);
        tvStockStatus = view.findViewById(R.id.tvStockStatus);
        tvStockValue = view.findViewById(R.id.tvStockValue);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);
    }

    /**
     * Load dữ liệu sản phẩm hiện tại vào UI
     */
    private void loadProductData() {
        if (getArguments() != null) {
            currentProduct = (Product) getArguments().getSerializable("product");
            if (currentProduct != null) {
                // Hiển thị thông tin cơ bản
                tvProductName.setText(currentProduct.getName());
                tvProductPrice.setText(String.format("%,.0f đ", currentProduct.getPrice()));
                tvProductQuantity.setText(String.valueOf(currentProduct.getQuantity()));
                tvProductId.setText(String.valueOf(currentProduct.getId()));
                
                // Hiển thị mô tả hoặc "Không có mô tả"
                String description = currentProduct.getDescription();
                if (description != null && !description.trim().isEmpty()) {
                    tvProductDescription.setText(description);
                } else {
                    tvProductDescription.setText("Không có mô tả");
                }
                
                // Hiển thị ảnh sản phẩm
                loadProductImage();
                
                // Tính toán và hiển thị thông tin bổ sung
                calculateAndDisplayAdditionalInfo();
            }
        }
    }

    /**
     * Load ảnh sản phẩm từ file
     */
    private void loadProductImage() {
        if (currentProduct.getImage() != null && !currentProduct.getImage().isEmpty()) {
            File imageFile = new File(currentProduct.getImage());
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
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

    /**
     * Tính toán và hiển thị thông tin bổ sung
     */
    private void calculateAndDisplayAdditionalInfo() {
        int quantity = currentProduct.getQuantity();
        double price = currentProduct.getPrice();
        
        // Trạng thái tồn kho
        if (quantity > 0) {
            tvStockStatus.setText("Còn hàng");
            tvStockStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
        } else {
            tvStockStatus.setText("Hết hàng");
            tvStockStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        
        // Giá trị tồn kho (số lượng × giá)
        double stockValue = quantity * price;
        tvStockValue.setText(String.format("%,.0f đ", stockValue));
    }

    /**
     * Thiết lập các event listeners
     */
    private void setupEventListeners() {
        // Nút quay lại
        ImageButton btnBack = getView().findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Nút chỉnh sửa
        btnEdit.setOnClickListener(v -> {
            // Navigate to EditProductFragment với dữ liệu sản phẩm
            EditProductFragment editFragment = EditProductFragment.newInstance(currentProduct);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.admin_content, editFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Nút xóa
        btnDelete.setOnClickListener(v -> {
            // Hiển thị dialog xác nhận xóa
            ConfirmDeleteDialog dialog = ConfirmDeleteDialog.newInstance(currentProduct.getName());
            dialog.setOnConfirmListener(new ConfirmDeleteDialog.OnConfirmListener() {
                @Override
                public void onConfirm() {
                    // Thực hiện xóa sản phẩm
                    int deleted = productService.delete(currentProduct.getId());
                    if (deleted > 0) {
                        CustomToast.showSuccess(requireContext(), "Xóa sản phẩm thành công");
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    } else {
                        CustomToast.showError(requireContext(), "Xóa sản phẩm thất bại");
                    }
                }

                @Override
                public void onCancel() {
                    // Không làm gì khi hủy
                }
            });
            dialog.show(getParentFragmentManager(), "confirm_delete");
        });
    }
}

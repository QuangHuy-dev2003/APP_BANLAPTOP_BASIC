package com.example.quanlycuahanglaptop.ui.admin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.domain.Product;
import com.example.quanlycuahanglaptop.service.ProductService;
import com.example.quanlycuahanglaptop.util.CustomToast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Fragment để cập nhật thông tin sản phẩm
 * Load dữ liệu sản phẩm hiện tại vào form và cho phép chỉnh sửa
 */
public class EditProductFragment extends Fragment {

    private static final int REQUEST_IMAGE_PICK = 1001;
    private ProductService productService;
    private Product currentProduct; // Sản phẩm đang được chỉnh sửa
    
    // UI Components
    private EditText edtProductName, edtProductPrice, edtProductQuantity, edtProductDescription;
    private ImageView imgPreview;
    private TextView tvImagePath;
    private String selectedImagePath = null; // Đường dẫn ảnh mới được chọn

    // Factory method để tạo fragment với dữ liệu sản phẩm
    public static EditProductFragment newInstance(Product product) {
        EditProductFragment fragment = new EditProductFragment();
        Bundle args = new Bundle();
        args.putSerializable("product", product);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_product, container, false);
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
        edtProductName = view.findViewById(R.id.edtProductName);
        edtProductPrice = view.findViewById(R.id.edtProductPrice);
        edtProductQuantity = view.findViewById(R.id.edtProductQuantity);
        edtProductDescription = view.findViewById(R.id.edtProductDescription);
        imgPreview = view.findViewById(R.id.imgPreview);
        tvImagePath = view.findViewById(R.id.tvImagePath);
    }

    /**
     * Load dữ liệu sản phẩm hiện tại vào form
     */
    private void loadProductData() {
        if (getArguments() != null) {
            currentProduct = (Product) getArguments().getSerializable("product");
            if (currentProduct != null) {
                // Điền dữ liệu vào các trường input
                edtProductName.setText(currentProduct.getName());
                edtProductPrice.setText(String.valueOf(currentProduct.getPrice()));
                edtProductQuantity.setText(String.valueOf(currentProduct.getQuantity()));
                edtProductDescription.setText(currentProduct.getDescription());
                
                // Load ảnh hiện tại nếu có
                if (currentProduct.getImage() != null && !currentProduct.getImage().isEmpty()) {
                    File imageFile = new File(currentProduct.getImage());
                    if (imageFile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                        if (bitmap != null) {
                            imgPreview.setImageBitmap(bitmap);
                            tvImagePath.setText("Ảnh hiện tại");
                        }
                    }
                }
            }
        }
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

        // Nút chọn ảnh mới
        Button btnSelectImage = getView().findViewById(R.id.btnSelectImage);
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), REQUEST_IMAGE_PICK);
        });

        // Nút cập nhật sản phẩm
        Button btnUpdateProduct = getView().findViewById(R.id.btnUpdateProduct);
        btnUpdateProduct.setOnClickListener(v -> handleUpdateProduct());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    // Lưu ảnh mới vào internal storage
                    selectedImagePath = saveImageToInternal(imageUri);
                    imgPreview.setImageURI(imageUri);
                    tvImagePath.setText("Ảnh mới đã chọn");
                } catch (Exception e) {
                    CustomToast.showError(requireContext(), "Lỗi khi chọn ảnh: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Lưu ảnh mới vào internal storage
     */
    private String saveImageToInternal(Uri imageUri) throws Exception {
        // Tạo thư mục images trong internal storage
        File imagesDir = new File(requireContext().getFilesDir(), "images");
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }

        // Tạo tên file unique
        String fileName = "product_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(imagesDir, fileName);

        // Copy ảnh từ URI vào file
        InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
        FileOutputStream outputStream = new FileOutputStream(imageFile);
        
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        
        inputStream.close();
        outputStream.close();

        return imageFile.getAbsolutePath();
    }

    /**
     * Xử lý cập nhật sản phẩm
     */
    private void handleUpdateProduct() {
        // Lấy dữ liệu từ form
        String name = edtProductName.getText().toString().trim();
        String priceText = edtProductPrice.getText().toString().trim();
        String quantityText = edtProductQuantity.getText().toString().trim();
        String description = edtProductDescription.getText().toString().trim();

        // Validation dữ liệu đầu vào
        if (name.isEmpty()) {
            CustomToast.showError(requireContext(), "Vui lòng nhập tên sản phẩm");
            return;
        }

        if (priceText.isEmpty()) {
            CustomToast.showError(requireContext(), "Vui lòng nhập giá sản phẩm");
            return;
        }

        if (quantityText.isEmpty()) {
            CustomToast.showError(requireContext(), "Vui lòng nhập số lượng");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            int quantity = Integer.parseInt(quantityText);
            
            if (price <= 0) {
                CustomToast.showError(requireContext(), "Giá sản phẩm phải lớn hơn 0");
                return;
            }
            
            if (quantity < 0) {
                CustomToast.showError(requireContext(), "Số lượng không được âm");
                return;
            }

            // Cập nhật thông tin sản phẩm
            currentProduct.setName(name);
            currentProduct.setPrice(price);
            currentProduct.setQuantity(quantity);
            currentProduct.setDescription(description.isEmpty() ? null : description);
            
            // Sử dụng ảnh mới nếu đã chọn, ngược lại giữ ảnh cũ
            if (selectedImagePath != null) {
                currentProduct.setImage(selectedImagePath);
            }

            // Gọi service để cập nhật
            int updatedRows = productService.update(currentProduct);
            if (updatedRows > 0) {
                // Ẩn bàn phím trước khi hiển thị thông báo
                hideKeyboard();
                CustomToast.showSuccess(requireContext(), "Cập nhật sản phẩm thành công");
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            } else {
                CustomToast.showError(requireContext(), "Cập nhật sản phẩm thất bại");
            }
        } catch (NumberFormatException e) {
            CustomToast.showError(requireContext(), "Dữ liệu nhập vào không hợp lệ");
        } catch (Exception e) {
            CustomToast.showError(requireContext(), "Lỗi: " + e.getMessage());
        }
    }

    /**
     * Ẩn bàn phím ảo
     */
    private void hideKeyboard() {
        View view = getView();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

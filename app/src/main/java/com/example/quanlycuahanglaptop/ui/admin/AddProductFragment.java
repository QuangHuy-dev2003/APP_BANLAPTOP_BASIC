package com.example.quanlycuahanglaptop.ui.admin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
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

public class AddProductFragment extends Fragment {

    private static final int REQUEST_IMAGE_PICK = 1001;
    private ProductService productService;
    private EditText edtProductName, edtProductPrice, edtProductQuantity, edtProductDescription;
    private ImageView imgPreview;
    private TextView tvImagePath;
    private String selectedImagePath = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productService = new ProductService(requireContext());

        edtProductName = view.findViewById(R.id.edtProductName);
        edtProductPrice = view.findViewById(R.id.edtProductPrice);
        edtProductQuantity = view.findViewById(R.id.edtProductQuantity);
        edtProductDescription = view.findViewById(R.id.edtProductDescription);
        imgPreview = view.findViewById(R.id.imgPreview);
        tvImagePath = view.findViewById(R.id.tvImagePath);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), REQUEST_IMAGE_PICK);
        });

        Button btnAddProduct = view.findViewById(R.id.btnAddProduct);
        btnAddProduct.setOnClickListener(v -> handleAddProduct());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    // Lưu ảnh vào thư mục assets hoặc internal storage
                    selectedImagePath = saveImageToInternal(imageUri);
                    imgPreview.setImageURI(imageUri);
                    tvImagePath.setText("Đã chọn ảnh");
                } catch (Exception e) {
                    CustomToast.showError(requireContext(), "Lỗi khi chọn ảnh: " + e.getMessage());
                }
            }
        }
    }

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

    private void handleAddProduct() {
        String name = edtProductName.getText().toString().trim();
        String priceText = edtProductPrice.getText().toString().trim();
        String quantityText = edtProductQuantity.getText().toString().trim();
        String description = edtProductDescription.getText().toString().trim();

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

            Product product = new Product();
            product.setName(name);
            product.setPrice(price);
            product.setQuantity(quantity);
            product.setDescription(description.isEmpty() ? null : description);
            product.setImage(selectedImagePath);

            long productId = productService.create(product);
            if (productId > 0) {
                // Ẩn bàn phím trước khi hiển thị thông báo
                hideKeyboard();
                CustomToast.showSuccess(requireContext(), "Thêm sản phẩm thành công");
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            } else {
                CustomToast.showError(requireContext(), "Thêm sản phẩm thất bại");
            }
        } catch (NumberFormatException e) {
            CustomToast.showError(requireContext(), "Giá sản phẩm không hợp lệ");
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

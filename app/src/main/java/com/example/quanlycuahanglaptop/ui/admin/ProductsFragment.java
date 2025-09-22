package com.example.quanlycuahanglaptop.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.app.AdminActivity;
import com.example.quanlycuahanglaptop.domain.Product;
import com.example.quanlycuahanglaptop.service.ProductService;
import com.example.quanlycuahanglaptop.ui.components.ConfirmDeleteDialog;
import com.example.quanlycuahanglaptop.util.CustomToast;

import java.util.List;

public class ProductsFragment extends Fragment {
    private ProductService productService;
    private ProductAdapter adapter;
    private int page = 1;
    private static final int PAGE_SIZE = 5;
    private TextView tvPageInfo, tvEmpty;
    private EditText edtSearch;
    private String currentKeyword = "";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productService = new ProductService(requireContext());

        RecyclerView rv = view.findViewById(R.id.rvProducts);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProductAdapter(new ProductAdapter.Listener() {
            @Override
            public void onEdit(Product product) {
                // Navigate to EditProductFragment với dữ liệu sản phẩm
                EditProductFragment editFragment = EditProductFragment.newInstance(product);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.admin_content, editFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDelete(Product product) {
                // Hiển thị dialog xác nhận xóa
                ConfirmDeleteDialog dialog = ConfirmDeleteDialog.newInstance(product.getName());
                dialog.setOnConfirmListener(new ConfirmDeleteDialog.OnConfirmListener() {
                    @Override
                    public void onConfirm() {
                        // Thực hiện xóa sản phẩm
                        int deleted = productService.delete(product.getId());
                        if (deleted > 0) {
                            CustomToast.showSuccess(requireContext(), getString(R.string.delete_success));
                            loadPage(page);
                        } else {
                            CustomToast.showError(requireContext(), getString(R.string.delete_fail));
                        }
                    }

                    @Override
                    public void onCancel() {
                        // Không làm gì khi hủy
                    }
                });
                dialog.show(getParentFragmentManager(), "confirm_delete");
            }

            @Override
            public void onViewDetail(Product product) {
                // Navigate to ProductDetailAdminFragment để xem chi tiết
                ProductDetailAdminFragment detailFragment = ProductDetailAdminFragment.newInstance(product);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.admin_content, detailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        rv.setAdapter(adapter);

        Button btnAdd = view.findViewById(R.id.btnAddProduct);
        btnAdd.setOnClickListener(v -> {
            // Navigate to AddProductFragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.admin_content, new AddProductFragment())
                    .addToBackStack(null)
                    .commit();
        });

        Button btnPrev = view.findViewById(R.id.btnPrev);
        Button btnNext = view.findViewById(R.id.btnNext);
        tvPageInfo = view.findViewById(R.id.tvPageInfo);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        edtSearch = view.findViewById(R.id.edtSearch);
        View btnSearch = view.findViewById(R.id.btnSearch);

        btnPrev.setOnClickListener(v -> {
            if (page > 1) {
                page--;
                loadPage(page);
            }
        });
        btnNext.setOnClickListener(v -> {
            int total = productService.countByName(currentKeyword);
            int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
            if (page < totalPages) {
                page++;
                loadPage(page);
            }
        });

        btnSearch.setOnClickListener(v -> {
            currentKeyword = edtSearch.getText().toString();
            page = 1;
            loadPage(page);
        });

        // Xử lý nút mở drawer menu (giống như DashboardFragment)
        ImageButton btnOpenDrawer = view.findViewById(R.id.btnOpenDrawer);
        btnOpenDrawer.setOnClickListener(v -> {
            if (getActivity() instanceof AdminActivity) {
                ((AdminActivity) getActivity()).openDrawer();
            }
        });

        loadPage(page);
    }

    private void loadPage(int p) {
        int total = productService.countByName(currentKeyword);
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        if (p > totalPages) p = totalPages;
        
        List<Product> products;
        if (currentKeyword == null || currentKeyword.trim().isEmpty()) {
            products = productService.findPage(p, PAGE_SIZE);
        } else {
            products = productService.searchByName(currentKeyword, p, PAGE_SIZE);
        }
        
        adapter.submitList(products);
        
        // Hiển thị empty state nếu không có sản phẩm
        if (tvEmpty != null) {
            tvEmpty.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
        }
        
        if (tvPageInfo != null) {
            tvPageInfo.setText(p + "/" + totalPages);
        }
    }
}



package com.example.quanlycuahanglaptop.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.app.AdminActivity;
import com.example.quanlycuahanglaptop.domain.Order;
import com.example.quanlycuahanglaptop.service.OrderService;
import com.example.quanlycuahanglaptop.ui.adapters.RecentOrdersAdapter;

public class DashboardFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View btnMenu = view.findViewById(R.id.btnOpenDrawer);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof AdminActivity) {
                    ((AdminActivity) getActivity()).openDrawer();
                }
            });
        }

        // Setup RecyclerView recent orders
        RecyclerView rv = view.findViewById(R.id.rvRecentOrders);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            OrderService orderService = new OrderService(requireContext());
            java.util.List<Order> recent = orderService.getLatestOrders(4);
            rv.setAdapter(new RecentOrdersAdapter(recent));
        }

        // Làm nhẹ style hai nút nhanh (nếu tồn tại)
        View btnAddProduct = view.findViewById(R.id.btnAddProduct);
        View btnViewOrders = view.findViewById(R.id.btnViewOrders);
        if (btnAddProduct != null) {
            btnAddProduct.setAlpha(0.96f);
        }
        if (btnViewOrders != null) {
            btnViewOrders.setAlpha(0.96f);
        }
    }
}



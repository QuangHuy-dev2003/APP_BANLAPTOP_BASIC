package com.example.quanlycuahanglaptop.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.app.AdminActivity;
import com.example.quanlycuahanglaptop.domain.Order;
import com.example.quanlycuahanglaptop.service.OrderService;
 

import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.text.NumberFormat;
import java.util.Locale;

public class OrdersFragment extends Fragment {
    private OrderService orderService;
    private OrdersListAdapter adapter;
    private int page = 1;
    private static final int PAGE_SIZE = 5;
    private TextView tvPageInfo, tvEmpty;
    private EditText edtSearch;
    private String currentKeyword = "";
    private com.example.quanlycuahanglaptop.domain.OrderStatus currentStatus = null;
    

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        orderService = new OrderService(requireContext());

        RecyclerView rv = view.findViewById(R.id.rvOrders);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OrdersListAdapter(order -> {
            android.content.Intent intent = new android.content.Intent(requireContext(), com.example.quanlycuahanglaptop.ui.admin.OrderDetailAdminActivity.class);
            intent.putExtra("order_id", order.getId());
            startActivityForResult(intent, 1001);
        });
        rv.setAdapter(adapter);

        Button btnPrev = view.findViewById(R.id.btnPrev);
        Button btnNext = view.findViewById(R.id.btnNext);
        tvPageInfo = view.findViewById(R.id.tvPageInfo);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        edtSearch = view.findViewById(R.id.edtSearch);
        View btnSearch = view.findViewById(R.id.btnSearch);
        View btnClear = view.findViewById(R.id.btnClear);
        Spinner spStatus = view.findViewById(R.id.spStatus);
        

        ArrayAdapter<CharSequence> adapterStatus = ArrayAdapter.createFromResource(requireContext(), R.array.order_status_entries, R.layout.item_spinner_center);
        adapterStatus.setDropDownViewResource(R.layout.item_spinner_center_dropdown);
        spStatus.setAdapter(adapterStatus);
        spStatus.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int position, long id) {
                switch (position) {
                    case 0: currentStatus = null; break;
                    case 1: currentStatus = com.example.quanlycuahanglaptop.domain.OrderStatus.RECEIVED; break;
                    case 2: currentStatus = com.example.quanlycuahanglaptop.domain.OrderStatus.SHIPPING; break;
                    case 3: currentStatus = com.example.quanlycuahanglaptop.domain.OrderStatus.DELIVERED; break;
                    case 4: currentStatus = com.example.quanlycuahanglaptop.domain.OrderStatus.CANCELLED; break;
                }
                page = 1;
                loadPage(page);
                
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        btnPrev.setOnClickListener(v -> {
            if (page > 1) {
                page--;
                loadPage(page);
            }
        });
        btnNext.setOnClickListener(v -> {
            int total = orderService.countByKeywordAndStatus(currentKeyword, currentStatus);
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

        btnClear.setOnClickListener(v -> {
            edtSearch.setText("");
            currentKeyword = "";
            page = 1;
            loadPage(page);
            
        });

        ImageButton btnOpenDrawer = view.findViewById(R.id.btnOpenDrawer);
        if (btnOpenDrawer != null) {
            btnOpenDrawer.setOnClickListener(v -> {
                if (getActivity() instanceof AdminActivity) {
                    ((AdminActivity) getActivity()).openDrawer();
                }
            });
        }

        loadPage(page);
    }

    private void loadPage(int p) {
        int total = orderService.countByKeywordAndStatus(currentKeyword, currentStatus);
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        if (p > totalPages) p = totalPages;
        List<Order> pageData = orderService.searchByKeywordAndStatus(currentKeyword, currentStatus, p, PAGE_SIZE);

        adapter.submitList(pageData);
        if (tvEmpty != null) {
            tvEmpty.setVisibility(pageData.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (tvPageInfo != null) {
            tvPageInfo.setText(p + "/" + totalPages);
        }
    }

    

    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == android.app.Activity.RESULT_OK) {
            // Reload current page to reflect possible status change
            loadPage(page);
        }
    }
}



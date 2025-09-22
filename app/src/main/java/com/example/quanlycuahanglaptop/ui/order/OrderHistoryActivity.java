package com.example.quanlycuahanglaptop.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.domain.Order;
import com.example.quanlycuahanglaptop.domain.OrderStatus;
import com.example.quanlycuahanglaptop.service.OrderService;
import com.example.quanlycuahanglaptop.ui.adapters.OrderHistoryAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity hiển thị lịch sử mua hàng của người dùng với filter trạng thái
 */
public class OrderHistoryActivity extends AppCompatActivity {
    
    private RecyclerView recyclerViewOrders;
    private LinearLayout textViewEmpty;
    private Spinner spinnerStatusFilter;
    private OrderHistoryAdapter orderAdapter;
    private OrderService orderService;
    private long currentUserId;
    private List<Order> allOrders;
    private List<Order> filteredOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        
        // Lấy user ID từ intent
        currentUserId = getIntent().getLongExtra("user_id", -1);
        if (currentUserId == -1) {
            finish();
            return;
        }
        
        initViews();
        initServices();
        setupRecyclerView();
        setupStatusFilter();
        loadOrders();
    }

    private void initViews() {
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        
        // Setup toolbar
        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
    }

    private void initServices() {
        orderService = new OrderService(this);
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderHistoryAdapter(new ArrayList<>(), this::onOrderClick);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    private void setupStatusFilter() {
        // Tạo danh sách trạng thái cho spinner
        List<String> statusList = new ArrayList<>();
        statusList.add("Tất cả");
        for (OrderStatus status : OrderStatus.values()) {
            statusList.add(status.getDisplayName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, statusList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(adapter);

        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterOrdersByStatus(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không làm gì
            }
        });
    }

    private void loadOrders() {
        allOrders = orderService.getOrdersByUserId(currentUserId);
        filteredOrders = new ArrayList<>(allOrders);
        updateUI();
    }

    /**
     * Refresh lại danh sách đơn hàng và giữ nguyên filter hiện tại
     */
    private void refreshOrders() {
        // Lưu lại vị trí filter hiện tại
        int currentFilterPosition = spinnerStatusFilter.getSelectedItemPosition();
        
        // Load lại tất cả đơn hàng
        allOrders = orderService.getOrdersByUserId(currentUserId);
        
        // Áp dụng lại filter hiện tại
        filterOrdersByStatus(currentFilterPosition);
    }

    private void filterOrdersByStatus(int position) {
        if (position == 0) {
            // "Tất cả"
            filteredOrders = new ArrayList<>(allOrders);
        } else {
            // Filter theo trạng thái
            OrderStatus selectedStatus = OrderStatus.values()[position - 1];
            filteredOrders = orderService.getOrdersByUserIdAndStatus(currentUserId, selectedStatus);
        }
        updateUI();
    }

    private void updateUI() {
        if (filteredOrders.isEmpty()) {
            recyclerViewOrders.setVisibility(View.GONE);
            textViewEmpty.setVisibility(View.VISIBLE);

        } else {
            recyclerViewOrders.setVisibility(View.VISIBLE);
            textViewEmpty.setVisibility(View.GONE);
            orderAdapter.updateOrders(filteredOrders);
        }
    }

    private void onOrderClick(Order order) {
        // Mở chi tiết đơn hàng với startActivityForResult
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("order_id", order.getId());
        intent.putExtra("user_id", currentUserId);
        startActivityForResult(intent, 1001); // Request code 1001
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Kiểm tra nếu quay lại từ OrderDetailActivity và có thay đổi
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // Refresh lại danh sách đơn hàng và giữ nguyên filter
            refreshOrders();
        }
    }
}

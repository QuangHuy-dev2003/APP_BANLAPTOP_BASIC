package com.example.quanlycuahanglaptop.ui.admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.domain.Order;
import com.example.quanlycuahanglaptop.domain.OrderItem;
import com.example.quanlycuahanglaptop.domain.OrderStatus;
import com.example.quanlycuahanglaptop.domain.Product;
import com.example.quanlycuahanglaptop.service.OrderService;
import com.example.quanlycuahanglaptop.service.ProductService;
import com.example.quanlycuahanglaptop.ui.adapters.OrderDetailAdapter;
import com.example.quanlycuahanglaptop.util.CustomToast;
import com.example.quanlycuahanglaptop.util.TimeUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailAdminActivity extends AppCompatActivity {
    private TextView tvOrderId, tvOrderAddress, tvOrderPhone, tvOrderTotal, tvOrderDate;
    private Spinner spStatus;
    private Button btnUpdate;
    private ImageButton btnBack;
    private RecyclerView rvItems;
    private OrderDetailAdapter adapter;
    private OrderService orderService;
    private ProductService productService;
    private long orderId;
    private Order currentOrder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_detail);

        orderId = getIntent().getLongExtra("order_id", -1);
        if (orderId == -1) { finish(); return; }

        orderService = new OrderService(this);
        productService = new ProductService(this);

        tvOrderId = findViewById(R.id.textViewOrderId);
        tvOrderAddress = findViewById(R.id.textViewOrderAddress);
        tvOrderPhone = findViewById(R.id.textViewOrderPhone);
        tvOrderTotal = findViewById(R.id.textViewOrderTotal);
        tvOrderDate = findViewById(R.id.textViewOrderDate);
        spStatus = findViewById(R.id.spStatus);
        btnUpdate = findViewById(R.id.buttonUpdateStatus);
        btnBack = findViewById(R.id.buttonBack);
        rvItems = findViewById(R.id.recyclerViewOrderItems);

        btnBack.setOnClickListener(v -> finish());
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderDetailAdapter(new ArrayList<>());
        rvItems.setAdapter(adapter);

        String[] statusEntriesVi = getResources().getStringArray(R.array.order_status_entries);
        final String[] statusValuesEn = getResources().getStringArray(R.array.order_status_values);
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, R.layout.item_spinner_center,
                new String[]{statusEntriesVi[1], statusEntriesVi[2], statusEntriesVi[3], statusEntriesVi[4]});
        spAdapter.setDropDownViewResource(R.layout.item_spinner_center_dropdown);
        spStatus.setAdapter(spAdapter);

        loadData();

        btnUpdate.setOnClickListener(v -> {
            if (currentOrder == null) return;
            int pos = spStatus.getSelectedItemPosition();
            // pos 0..3 tương ứng entries 1..4 trong resources
            String en = statusValuesEn[pos + 1];
            OrderStatus newStatus = OrderStatus.fromString(en);
            if (newStatus == null) return;
            boolean ok = orderService.updateOrderStatus(currentOrder.getId(), newStatus);
            if (ok) {
                CustomToast.showSuccess(this, getString(R.string.update_success));
                currentOrder.setStatus(newStatus);
                displayOrder(currentOrder);
                // thông báo list bên ngoài refresh
                setResult(RESULT_OK);
            } else {
                CustomToast.showError(this, getString(R.string.update_fail));
            }
        });
    }

    private void loadData() {
        currentOrder = orderService.getOrderById(orderId);
        if (currentOrder == null) { finish(); return; }
        displayOrder(currentOrder);

        List<OrderItem> items = orderService.getOrderItemsByOrderId(orderId);
        List<OrderDetailAdapter.OrderItemWithProduct> viewItems = new ArrayList<>();
        for (OrderItem it : items) {
            Product p = productService.findById(it.getProductId());
            if (p != null) viewItems.add(new OrderDetailAdapter.OrderItemWithProduct(it, p));
        }
        adapter.updateItems(viewItems);
    }

    private void displayOrder(Order order) {
        tvOrderId.setText("Đơn hàng #" + order.getId());
        tvOrderAddress.setText(order.getAddress() != null ? order.getAddress() : "-");
        tvOrderPhone.setText(order.getPhone() != null ? order.getPhone() : "-");
        NumberFormat vn = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvOrderTotal.setText(vn.format(order.getTotalPrice()));
        String dateText = order.getCreatedAt() != null ? TimeUtils.formatDatabaseTimeToVietnam(order.getCreatedAt()) : "-";
        tvOrderDate.setText(dateText);
        if (order.getStatus() != null) {
            String[] statusValuesEn = getResources().getStringArray(R.array.order_status_values);
            String en = order.getStatus().toString();
            int pos = -1;
            for (int i = 1; i < statusValuesEn.length; i++) {
                if (statusValuesEn[i].equals(en)) { pos = i - 1; break; }
            }
            if (pos >= 0) spStatus.setSelection(pos);
        }
    }
}



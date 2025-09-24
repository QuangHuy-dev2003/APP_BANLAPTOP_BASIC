package com.example.quanlycuahanglaptop.ui.order;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.domain.Order;
import com.example.quanlycuahanglaptop.domain.OrderItem;
import com.example.quanlycuahanglaptop.domain.Product;
import com.example.quanlycuahanglaptop.service.OrderService;
import com.example.quanlycuahanglaptop.service.ProductService;
import com.example.quanlycuahanglaptop.ui.adapters.OrderDetailAdapter;
import com.example.quanlycuahanglaptop.util.CustomToast;
import com.example.quanlycuahanglaptop.util.TimeUtils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Activity hiển thị chi tiết đơn hàng
 */
public class OrderDetailActivity extends AppCompatActivity {
    
    private TextView textViewOrderId;
    private TextView textViewOrderStatus;
    private TextView textViewOrderAddress;
    private TextView textViewOrderPhone;
    private TextView textViewOrderTotal;
    private TextView textViewOrderDate;
    private TextView textViewCancelInfo;
    private RecyclerView recyclerViewOrderItems;
    private Button buttonCancelOrder;
    
    private OrderService orderService;
    private ProductService productService;
    private OrderDetailAdapter orderDetailAdapter;
    private long orderId;
    private long userId;
    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        
        // Lấy order ID và user ID từ intent
        orderId = getIntent().getLongExtra("order_id", -1);
        userId = getIntent().getLongExtra("user_id", -1);
        
        if (orderId == -1 || userId == -1) {
            finish();
            return;
        }
        
        initViews();
        initServices();
        setupRecyclerView();
        loadOrderDetail();
    }

    private void initViews() {
        textViewOrderId = findViewById(R.id.textViewOrderId);
        textViewOrderStatus = findViewById(R.id.textViewOrderStatus);
        textViewOrderAddress = findViewById(R.id.textViewOrderAddress);
        textViewOrderPhone = findViewById(R.id.textViewOrderPhone);
        textViewOrderTotal = findViewById(R.id.textViewOrderTotal);
        textViewOrderDate = findViewById(R.id.textViewOrderDate);
        textViewCancelInfo = findViewById(R.id.textViewCancelInfo);
        recyclerViewOrderItems = findViewById(R.id.recyclerViewOrderItems);
        buttonCancelOrder = findViewById(R.id.buttonCancelOrder);
        
        // Setup toolbar
        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
        
        // Setup cancel button
        buttonCancelOrder.setOnClickListener(v -> showCancelOrderDialog());
    }

    private void initServices() {
        orderService = new OrderService(this);
        productService = new ProductService(this);
    }

    private void setupRecyclerView() {
        orderDetailAdapter = new OrderDetailAdapter(new ArrayList<>());
        recyclerViewOrderItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrderItems.setAdapter(orderDetailAdapter);
    }

    private void loadOrderDetail() {
        // Lấy thông tin đơn hàng
        currentOrder = orderService.getOrderById(orderId);
        if (currentOrder == null) {
            finish();
            return;
        }
        
        // Hiển thị thông tin đơn hàng
        displayOrderInfo(currentOrder);
        
        // Kiểm tra điều kiện huỷ đơn hàng
        checkCancelOrderCondition();
        
        // Lấy danh sách sản phẩm trong đơn hàng
        List<OrderItem> orderItems = orderService.getOrderItemsByOrderId(orderId);
        List<OrderDetailAdapter.OrderItemWithProduct> itemsWithProducts = new ArrayList<>();
        
        for (OrderItem item : orderItems) {
            Product product = productService.findById(item.getProductId());
            if (product != null) {
                itemsWithProducts.add(new OrderDetailAdapter.OrderItemWithProduct(item, product));
            }
        }
        
        orderDetailAdapter.updateItems(itemsWithProducts);
    }

    private void displayOrderInfo(Order order) {
        // ID đơn hàng
        textViewOrderId.setText("Đơn hàng #" + order.getId());

        // Trạng thái
        if (order.getStatus() != null) {
            textViewOrderStatus.setText(order.getStatus().getDisplayName());
            textViewOrderStatus.setBackgroundResource(getStatusBackgroundResource(order.getStatus()));
        }

        // Địa chỉ
        textViewOrderAddress.setText(order.getAddress() != null ? order.getAddress() : "Chưa có địa chỉ");

        // Số điện thoại
        textViewOrderPhone.setText(order.getPhone() != null ? order.getPhone() : "Chưa có số điện thoại");

        // Tổng tiền
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        textViewOrderTotal.setText(currencyFormat.format(order.getTotalPrice()));

        // Ngày tạo với múi giờ Việt Nam
        String dateText = "Chưa có ngày";
        if (order.getCreatedAt() != null && !order.getCreatedAt().isEmpty()) {
            // Format thời gian từ database (đã là giờ Việt Nam)
            dateText = TimeUtils.formatDatabaseTimeToVietnam(order.getCreatedAt());
        }
        textViewOrderDate.setText(dateText);
        
        // Hiển thị thông tin hủy đơn hàng riêng biệt
        if (order.getStatus() == com.example.quanlycuahanglaptop.domain.OrderStatus.RECEIVED) {
            if (TimeUtils.canCancelOrderFromDatabase(order.getCreatedAt())) {
                int remainingMinutes = TimeUtils.getRemainingCancelMinutesFromDatabase(order.getCreatedAt());
                textViewCancelInfo.setText("⏰ Bạn có thể huỷ đơn hàng trong " + remainingMinutes + " phút nữa");
            } else {
                textViewCancelInfo.setText("❌ Đã quá 30 phút - Không thể huỷ đơn hàng");
            }
        } else {
            textViewCancelInfo.setText("⚠️ Đơn hàng đã được xử lý, không thể huỷ");
        }
    }

    private int getStatusBackgroundResource(com.example.quanlycuahanglaptop.domain.OrderStatus status) {
        switch (status) {
            case RECEIVED:
                return R.drawable.status_received_background;
            case SHIPPING:
                return R.drawable.status_shipping_background;
            case DELIVERED:
                return R.drawable.status_delivered_background;
            case CANCELLED:
                return R.drawable.status_cancelled_background;
            default:
                return R.drawable.status_received_background;
        }
    }

    /**
     * Kiểm tra điều kiện có thể huỷ đơn hàng (trong vòng 30 phút)
     */
    private void checkCancelOrderCondition() {
        if (currentOrder == null || currentOrder.getCreatedAt() == null) {
            buttonCancelOrder.setEnabled(false);
            textViewCancelInfo.setText("Không thể huỷ đơn hàng");
            return;
        }

        // Sử dụng method mới để kiểm tra với thời gian từ database
        boolean canCancel = currentOrder.getStatus() == com.example.quanlycuahanglaptop.domain.OrderStatus.RECEIVED 
                          && TimeUtils.canCancelOrderFromDatabase(currentOrder.getCreatedAt());

        if (canCancel) {
            buttonCancelOrder.setEnabled(true);
            buttonCancelOrder.setBackgroundResource(R.drawable.btn_cancel_order_background);
            buttonCancelOrder.setTextColor(getResources().getColor(R.color.white));
            buttonCancelOrder.setText("Huỷ đặt hàng");
            
            int remainingMinutes = TimeUtils.getRemainingCancelMinutesFromDatabase(currentOrder.getCreatedAt());
            textViewCancelInfo.setText("⏰ Bạn có thể huỷ đơn hàng trong " + remainingMinutes + " phút nữa");
        } else {
            buttonCancelOrder.setEnabled(false);
            
            if (currentOrder.getStatus() != com.example.quanlycuahanglaptop.domain.OrderStatus.RECEIVED) {
                // Đơn hàng đã xử lý - button disabled
                buttonCancelOrder.setBackgroundResource(R.drawable.btn_cancel_order_disabled_background);
                buttonCancelOrder.setTextColor(getResources().getColor(R.color.white));
                buttonCancelOrder.setText("Huỷ đặt hàng");
                textViewCancelInfo.setText("⚠️ Đơn hàng đã được xử lý, không thể huỷ");
            } else {
                // Quá thời gian - button tối màu nhưng vẫn hiển thị text
                buttonCancelOrder.setBackgroundResource(R.drawable.btn_cancel_order_expired_background);
                buttonCancelOrder.setTextColor(getResources().getColor(R.color.textPrimary));
                buttonCancelOrder.setText("Huỷ đặt hàng");
                textViewCancelInfo.setText("❌ Đã quá 30 phút - Không thể huỷ đơn hàng");
            }
        }
    }

    /**
     * Format thời gian quá hạn để hiển thị dễ hiểu
     */
    private String formatTimeOver(long overMinutes) {
        if (overMinutes < 60) {
            return "Đã quá " + overMinutes + " phút";
        } else if (overMinutes < 1440) { // Dưới 24 giờ
            long hours = overMinutes / 60;
            long minutes = overMinutes % 60;
            if (minutes == 0) {
                return "Đã quá " + hours + " giờ";
            } else {
                return "Đã quá " + hours + " giờ " + minutes + " phút";
            }
        } else { // Trên 24 giờ
            long days = overMinutes / 1440;
            long hours = (overMinutes % 1440) / 60;
            if (hours == 0) {
                return "Đã quá " + days + " ngày";
            } else {
                return "Đã quá " + days + " ngày " + hours + " giờ";
            }
        }
    }

    /**
     * Hiển thị dialog xác nhận huỷ đơn hàng với giao diện custom
     */
    private void showCancelOrderDialog() {
        // Tạo custom dialog với style đẹp
        Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.dialog_cancel_order);
        dialog.setCancelable(true);
        
        // Thiết lập kích thước dialog
        dialog.getWindow().setLayout(
            (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        
        // Lấy các view từ dialog
        TextView textViewOrderInfo = dialog.findViewById(R.id.textViewOrderInfo);
        TextView textViewOrderTotal = dialog.findViewById(R.id.textViewOrderTotal);
        Button buttonCancel = dialog.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialog.findViewById(R.id.buttonConfirm);
        
        // Hiển thị thông tin đơn hàng
        textViewOrderInfo.setText("Đơn hàng #" + currentOrder.getId());
        
        // Format tổng tiền
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        textViewOrderTotal.setText("Tổng tiền: " + currencyFormat.format(currentOrder.getTotalPrice()));
        
        // Setup button listeners
        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            cancelOrder();
        });
        
        // Hiển thị dialog với animation
        dialog.show();
    }

    /**
     * Xử lý huỷ đơn hàng
     */
    private void cancelOrder() {
        if (currentOrder == null) {
            CustomToast.showError(this, "Không tìm thấy đơn hàng");
            return;
        }

        // Thực hiện huỷ trong transaction và cộng lại kho
        boolean success = orderService.cancelOrderAndRestoreStock(currentOrder.getId());
        if (success) {
            currentOrder.setStatus(com.example.quanlycuahanglaptop.domain.OrderStatus.CANCELLED);
        }

        if (success) {
            CustomToast.showSuccess(this, "Đã huỷ đơn hàng thành công");
            // Cập nhật giao diện
            displayOrderInfo(currentOrder);
            checkCancelOrderCondition();
            
            // Gửi result về OrderHistoryActivity để cập nhật danh sách
            setResult(RESULT_OK);
        } else {
            CustomToast.showError(this, "Có lỗi xảy ra khi huỷ đơn hàng");
        }
    }
}

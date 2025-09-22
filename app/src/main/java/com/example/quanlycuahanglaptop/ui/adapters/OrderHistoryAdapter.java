package com.example.quanlycuahanglaptop.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.domain.Order;
import com.example.quanlycuahanglaptop.domain.OrderStatus;
import com.example.quanlycuahanglaptop.util.TimeUtils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView hiển thị danh sách đơn hàng
 */
public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {
    
    private List<Order> orders;
    private OnOrderClickListener onOrderClickListener;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderHistoryAdapter(List<Order> orders, OnOrderClickListener listener) {
        this.orders = new ArrayList<>(orders);
        this.onOrderClickListener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders.clear();
        this.orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewOrderId;
        private TextView textViewOrderStatus;
        private TextView textViewOrderAddress;
        private TextView textViewOrderPhone;
        private TextView textViewOrderTotal;
        private TextView textViewOrderDate;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOrderId = itemView.findViewById(R.id.textViewOrderId);
            textViewOrderStatus = itemView.findViewById(R.id.textViewOrderStatus);
            textViewOrderAddress = itemView.findViewById(R.id.textViewOrderAddress);
            textViewOrderPhone = itemView.findViewById(R.id.textViewOrderPhone);
            textViewOrderTotal = itemView.findViewById(R.id.textViewOrderTotal);
            textViewOrderDate = itemView.findViewById(R.id.textViewOrderDate);

            itemView.setOnClickListener(v -> {
                if (onOrderClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onOrderClickListener.onOrderClick(orders.get(position));
                    }
                }
            });
        }

        public void bind(Order order) {
            // ID đơn hàng
            textViewOrderId.setText("Đơn hàng #" + order.getId());

            // Trạng thái đơn hàng
            OrderStatus status = order.getStatus() != null ? order.getStatus() : OrderStatus.RECEIVED;
            textViewOrderStatus.setText(status.getDisplayName());
            textViewOrderStatus.setBackgroundResource(getStatusBackgroundResource(status));

            // Địa chỉ
            textViewOrderAddress.setText(order.getAddress() != null ? order.getAddress() : "Chưa có địa chỉ");

            // Số điện thoại
            textViewOrderPhone.setText(order.getPhone() != null ? order.getPhone() : "Chưa có số điện thoại");

            // Tổng tiền
            textViewOrderTotal.setText(currencyFormat.format(order.getTotalPrice()));

            // Ngày tạo với múi giờ Việt Nam
            String dateText = "Chưa có ngày";
            if (order.getCreatedAt() != null && !order.getCreatedAt().isEmpty()) {
                // Sử dụng method mới để format thời gian từ database
                dateText = TimeUtils.formatDatabaseTimeToVietnam(order.getCreatedAt());
            }
            textViewOrderDate.setText(dateText);
        }

        private int getStatusBackgroundResource(OrderStatus status) {
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
    }
}

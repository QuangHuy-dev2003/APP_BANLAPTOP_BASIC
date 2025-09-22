package com.example.quanlycuahanglaptop.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.domain.Order;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RecentOrdersAdapter extends RecyclerView.Adapter<RecentOrdersAdapter.ViewHolder> {
    private final List<Order> orders;

    public RecentOrdersAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.tvOrderId.setText("#DH-" + String.format(Locale.getDefault(), "%06d", order.getId()));
        holder.tvOrderStatus.setText(order.getStatus() != null ? mapStatusToVi(order.getStatus().toString()) : "");
        holder.tvOrderCreatedAt.setText(order.getCreatedAt() != null ? order.getCreatedAt() : "");

        NumberFormat vnCurrency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvOrderTotal.setText(vnCurrency.format(order.getTotalPrice()));
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    private String mapStatusToVi(String statusEn) {
        if (statusEn == null) return "";
        switch (statusEn) {
            case "RECEIVED":
                return "Đã nhận";
            case "SHIPPING":
                return "Đang giao";
            case "DELIVERED":
                return "Đã giao";
            case "CANCELLED":
                return "Đã hủy";
            default:
                return statusEn;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderStatus, tvOrderCreatedAt, tvOrderTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderCreatedAt = itemView.findViewById(R.id.tvOrderCreatedAt);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
        }
    }
}



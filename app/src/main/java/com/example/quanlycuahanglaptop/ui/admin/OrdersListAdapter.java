package com.example.quanlycuahanglaptop.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.domain.Order;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrdersListAdapter extends RecyclerView.Adapter<OrdersListAdapter.ViewHolder> {
    public interface Listener {
        void onClick(Order order);
    }

    private final Listener listener;
    private final List<Order> data = new ArrayList<>();

    public OrdersListAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Order> orders) {
        data.clear();
        if (orders != null) data.addAll(orders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order o = data.get(position);
        holder.bind(o, listener);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvCreatedAt, tvTotal, tvPhone;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvCreatedAt = itemView.findViewById(R.id.tvOrderCreatedAt);
            tvTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvPhone = itemView.findViewById(R.id.tvOrderPhone);
        }

        void bind(Order o, Listener listener) {
            tvOrderId.setText("#DH-" + String.format(Locale.getDefault(), "%06d", o.getId()));
            if (o.getStatus() != null) {
                tvStatus.setText(o.getStatus().getDisplayName());
                switch (o.getStatus()) {
                    case RECEIVED:
                        tvStatus.setBackgroundResource(R.drawable.bg_chip_received);
                        tvStatus.setTextColor(0xFF2196F3);
                        break;
                    case SHIPPING:
                        tvStatus.setBackgroundResource(R.drawable.bg_chip_shipping);
                        tvStatus.setTextColor(0xFF4CAF50);
                        break;
                    case DELIVERED:
                        tvStatus.setBackgroundResource(R.drawable.bg_chip_delivered);
                        tvStatus.setTextColor(0xFFFF9800);
                        break;
                    case CANCELLED:
                        tvStatus.setBackgroundResource(R.drawable.bg_chip_cancelled);
                        tvStatus.setTextColor(0xFFF44336);
                        break;
                }
            } else {
                tvStatus.setText("");
                tvStatus.setBackground(null);
            }
            tvCreatedAt.setText(o.getCreatedAt() != null ? o.getCreatedAt() : "");
            NumberFormat vn = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvTotal.setText(vn.format(o.getTotalPrice()));
            tvPhone.setText(o.getPhone() != null ? o.getPhone() : "");
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(o);
            });
        }
    }
}



package com.example.quanlycuahanglaptop.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.domain.OrderItem;
import com.example.quanlycuahanglaptop.domain.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView hiển thị chi tiết sản phẩm trong đơn hàng
 */
public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder> {
    
    private List<OrderItemWithProduct> items;
    private NumberFormat currencyFormat;

    public static class OrderItemWithProduct {
        private OrderItem orderItem;
        private Product product;

        public OrderItemWithProduct(OrderItem orderItem, Product product) {
            this.orderItem = orderItem;
            this.product = product;
        }

        public OrderItem getOrderItem() {
            return orderItem;
        }

        public Product getProduct() {
            return product;
        }
    }

    public OrderDetailAdapter(List<OrderItemWithProduct> items) {
        this.items = new ArrayList<>(items);
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail, parent, false);
        return new OrderDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
        OrderItemWithProduct itemWithProduct = items.get(position);
        holder.bind(itemWithProduct);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<OrderItemWithProduct> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }

    class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewProduct;
        private TextView textViewProductName;
        private TextView textViewProductPrice;
        private TextView textViewQuantity;
        private TextView textViewSubtotal;

        public OrderDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewSubtotal = itemView.findViewById(R.id.textViewSubtotal);
        }

        public void bind(OrderItemWithProduct itemWithProduct) {
            OrderItem orderItem = itemWithProduct.getOrderItem();
            Product product = itemWithProduct.getProduct();

            // Tên sản phẩm
            textViewProductName.setText(product.getName());

            // Giá sản phẩm
            textViewProductPrice.setText(currencyFormat.format(product.getPrice()));

            // Số lượng
            textViewQuantity.setText("x" + orderItem.getQuantity());

            // Thành tiền
            double subtotal = orderItem.getPrice() * orderItem.getQuantity();
            textViewSubtotal.setText(currencyFormat.format(subtotal));

            // Load hình ảnh sản phẩm
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(product.getImage())
                    .placeholder(R.drawable.ic_product_placeholder)
                    .error(R.drawable.ic_product_placeholder)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(12)))
                    .into(imageViewProduct);
            } else {
                imageViewProduct.setImageResource(R.drawable.ic_product_placeholder);
            }
        }
    }
}

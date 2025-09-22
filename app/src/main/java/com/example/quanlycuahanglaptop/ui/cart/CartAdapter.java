package com.example.quanlycuahanglaptop.ui.cart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.domain.Product;
import com.example.quanlycuahanglaptop.repository.CartItemRepository;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItemRepository.CartItemWithProduct> cartItems;
    private OnCartItemListener listener;

    public interface OnCartItemListener {
        void onQuantityChanged(long cartItemId, int newQuantity);
        void onRemoveItem(long cartItemId);
    }

    public CartAdapter(List<CartItemRepository.CartItemWithProduct> cartItems, OnCartItemListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItemRepository.CartItemWithProduct item = cartItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void updateCartItems(List<CartItemRepository.CartItemWithProduct> newCartItems) {
        this.cartItems = newCartItems;
        notifyDataSetChanged();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName;
        private TextView tvProductPrice;
        private TextView tvQuantity;
        private ImageButton btnDecrease;
        private ImageButton btnIncrease;
        private ImageButton btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(CartItemRepository.CartItemWithProduct item, OnCartItemListener listener) {
            Product product = item.getProduct();
            int quantity = item.getCartItem().getQuantity();
            long cartItemId = item.getCartItem().getId();

            // Hiển thị thông tin sản phẩm
            tvProductName.setText(product.getName());
            
            // Format giá tiền
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvProductPrice.setText(formatter.format(product.getPrice()));
            
            // Hiển thị số lượng
            tvQuantity.setText(String.valueOf(quantity));

            // Load ảnh sản phẩm từ URL bằng Glide
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                Glide.with(ivProductImage.getContext())
                    .load(product.getImage())
                    .placeholder(R.drawable.bg_product_placeholder)
                    .error(R.drawable.bg_product_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(ivProductImage);
            } else {
                // Nếu không có URL ảnh, hiển thị placeholder
                ivProductImage.setImageResource(R.drawable.ic_products);
            }

            // Xử lý button giảm số lượng
            btnDecrease.setOnClickListener(v -> {
                if (quantity > 1) {
                    listener.onQuantityChanged(cartItemId, quantity - 1);
                }
            });

            // Xử lý button tăng số lượng
            btnIncrease.setOnClickListener(v -> {
                if (quantity < product.getQuantity()) { // Không vượt quá số lượng trong kho
                    listener.onQuantityChanged(cartItemId, quantity + 1);
                }
            });

            // Xử lý button xóa
            btnDelete.setOnClickListener(v -> {
                listener.onRemoveItem(cartItemId);
            });

            // Disable button tăng nếu đã đạt giới hạn kho
            btnIncrease.setEnabled(quantity < product.getQuantity());
        }
    }
}

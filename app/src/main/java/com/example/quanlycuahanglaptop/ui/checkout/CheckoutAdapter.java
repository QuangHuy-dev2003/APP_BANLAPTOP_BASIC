package com.example.quanlycuahanglaptop.ui.checkout;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.repository.CartItemRepository;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị danh sách sản phẩm trong giao diện checkout.
 * Hiển thị thông tin sản phẩm, số lượng và thành tiền.
 */
public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private List<CartItemRepository.CartItemWithProduct> cartItems;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CartItemRepository.CartItemWithProduct item);
    }

    public CheckoutAdapter(List<CartItemRepository.CartItemWithProduct> cartItems, OnItemClickListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout_product, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
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

    static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProduct;
        private TextView tvProductName;
        private TextView tvProductPrice;
        private TextView tvQuantity;
        private TextView tvItemTotal;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvItemTotal = itemView.findViewById(R.id.tv_item_total);
        }

        public void bind(CartItemRepository.CartItemWithProduct item, OnItemClickListener listener) {
            // Hiển thị thông tin sản phẩm
            tvProductName.setText(item.getProduct().getName());
            tvQuantity.setText(String.valueOf(item.getCartItem().getQuantity()));
            
            // Format giá tiền
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            String priceText = formatter.format(item.getProduct().getPrice()) + " đ";
            tvProductPrice.setText(priceText);
            
            // Tính thành tiền
            double itemTotal = item.getProduct().getPrice() * item.getCartItem().getQuantity();
            String totalText = formatter.format(itemTotal) + " đ";
            tvItemTotal.setText(totalText);
            
            // Hiển thị ảnh sản phẩm
            loadProductImage(item.getProduct().getImage());
            
            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }

        private void loadProductImage(String imagePath) {
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    if (bitmap != null) {
                        imgProduct.setImageBitmap(bitmap);
                    } else {
                        imgProduct.setImageResource(R.drawable.ic_products);
                    }
                } else {
                    imgProduct.setImageResource(R.drawable.ic_products);
                }
            } else {
                imgProduct.setImageResource(R.drawable.ic_products);
            }
        }
    }
}

package com.example.quanlycuahanglaptop.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.domain.Product;

import java.io.File;

public class HomeProductAdapter extends ListAdapter<Product, HomeProductAdapter.ProductVH> {

    public interface Listener {
        void onClick(Product product);
        void onAddToCart(Product product);
    }

    private final Listener listener;

    public HomeProductAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
        setHasStableIds(true);
    }

    private static final DiffUtil.ItemCallback<Product> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getName().equals(newItem.getName())
                    && oldItem.getPrice() == newItem.getPrice()
                    && ((oldItem.getImage() == null && newItem.getImage() == null) ||
                        (oldItem.getImage() != null && oldItem.getImage().equals(newItem.getImage())));
        }
    };

    @Override
    public long getItemId(int position) {
        Product p = getItem(position);
        return p.getId() != null ? p.getId() : position;
    }

    @NonNull
    @Override
    public ProductVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_grid, parent, false);
        return new ProductVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductVH holder, int position) {
        Product p = getItem(position);
        holder.tvName.setText(p.getName());
        holder.tvPrice.setText(String.format("%,.0f đ", p.getPrice()));

        if (p.getImage() != null && !p.getImage().isEmpty()) {
            File imageFile = new File(p.getImage());
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                if (bitmap != null) {
                    holder.imgProduct.setImageBitmap(bitmap);
                } else {
                    holder.imgProduct.setImageResource(R.drawable.ic_products);
                }
            } else {
                holder.imgProduct.setImageResource(R.drawable.ic_products);
            }
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_products);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(p));
    }

    static class ProductVH extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice;
        
        ProductVH(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}



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

public class ProductSmallAdapter extends ListAdapter<Product, ProductSmallAdapter.VH> {

    public interface Listener {
        void onClick(Product product);
    }

    private final Listener listener;

    public ProductSmallAdapter(Listener listener) {
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
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_small, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = getItem(position);
        holder.tvName.setText(p.getName());
        holder.tvPrice.setText(String.format("%,.0f đ", p.getPrice()));
        if (p.getImage() != null && !p.getImage().isEmpty()) {
            File f = new File(p.getImage());
            if (f.exists()) {
                Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath());
                if (b != null) holder.img.setImageBitmap(b);
                else holder.img.setImageResource(R.drawable.ic_products);
            } else holder.img.setImageResource(R.drawable.ic_products);
        } else holder.img.setImageResource(R.drawable.ic_products);
        holder.itemView.setOnClickListener(v -> listener.onClick(p));
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img; TextView tvName; TextView tvPrice;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}



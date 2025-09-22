package com.example.quanlycuahanglaptop.ui.admin;

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
import com.example.quanlycuahanglaptop.domain.Product;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductVH> {

    public interface Listener {
        void onEdit(Product product);
        void onDelete(Product product);
        void onViewDetail(Product product);
    }

    private final Listener listener;
    private final List<Product> data = new ArrayList<>();

    public ProductAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Product> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_card, parent, false);
        return new ProductVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductVH holder, int position) {
        Product p = data.get(position);
        holder.tvName.setText(p.getName());
        holder.tvPrice.setText(String.format("%,.0f đ", p.getPrice()));
        holder.tvQuantity.setText("SL: " + p.getQuantity());
        
        // Hiển thị ảnh sản phẩm
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
        
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(p));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(p));
        
        // Click vào card để xem chi tiết
        holder.itemView.setOnClickListener(v -> listener.onViewDetail(p));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ProductVH extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity;
        ImageView imgProduct, btnEdit, btnDelete;
        ProductVH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}



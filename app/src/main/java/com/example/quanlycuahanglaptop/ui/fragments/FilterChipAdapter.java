package com.example.quanlycuahanglaptop.ui.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.google.android.material.chip.Chip;

import java.util.List;

public class FilterChipAdapter extends ListAdapter<AllProductsFragment.FilterOption, FilterChipAdapter.FilterChipVH> {

    public interface Listener {
        void onFilterSelected(String sortBy);
    }

    private final Listener listener;

    public FilterChipAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<AllProductsFragment.FilterOption> DIFF = 
        new DiffUtil.ItemCallback<AllProductsFragment.FilterOption>() {
            @Override
            public boolean areItemsTheSame(@NonNull AllProductsFragment.FilterOption oldItem, 
                                        @NonNull AllProductsFragment.FilterOption newItem) {
                return oldItem.getValue().equals(newItem.getValue());
            }

            @Override
            public boolean areContentsTheSame(@NonNull AllProductsFragment.FilterOption oldItem, 
                                           @NonNull AllProductsFragment.FilterOption newItem) {
                return oldItem.getValue().equals(newItem.getValue()) && 
                       oldItem.isSelected() == newItem.isSelected();
            }
        };

    @NonNull
    @Override
    public FilterChipVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_chip, parent, false);
        return new FilterChipVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterChipVH holder, int position) {
        AllProductsFragment.FilterOption option = getItem(position);
        holder.chip.setText(option.getLabel());
        holder.chip.setChecked(option.isSelected());
        
        holder.chip.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                // Uncheck all other chips
                for (int i = 0; i < getItemCount(); i++) {
                    if (i != position) {
                        AllProductsFragment.FilterOption otherOption = getItem(i);
                        otherOption.setSelected(false);
                        notifyItemChanged(i);
                    }
                }
                option.setSelected(true);
                listener.onFilterSelected(option.getValue());
            }
        });
    }

    static class FilterChipVH extends RecyclerView.ViewHolder {
        Chip chip;
        
        FilterChipVH(@NonNull View itemView) {
            super(itemView);
            chip = (Chip) itemView;
        }
    }
}

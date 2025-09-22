package com.example.quanlycuahanglaptop.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;

import java.util.ArrayList;
import java.util.List;

public class ChipsAdapter extends RecyclerView.Adapter<ChipsAdapter.VH> {

    public interface Listener { void onClick(String chip); }

    private final Listener listener;
    private final List<String> data = new ArrayList<>();

    public ChipsAdapter(Listener listener) { this.listener = listener; }

    public void submit(List<String> chips) {
        data.clear();
        if (chips != null) data.addAll(chips);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chip, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String text = data.get(position);
        holder.tv.setText(text);
        holder.itemView.setOnClickListener(v -> listener.onClick(text));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        VH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tvChip);
        }
    }
}



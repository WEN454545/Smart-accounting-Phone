package com.example.myapplication.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.stats.StatsViewModel;
import com.example.myapplication.ui.view.TrendBarView;

import java.util.ArrayList;
import java.util.List;

public class TrendAdapter extends RecyclerView.Adapter<TrendAdapter.ViewHolder> {

    private List<StatsViewModel.PeriodSum> data = new ArrayList<>();

    public void setData(List<StatsViewModel.PeriodSum> data) {
        this.data = data != null ? data : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trend_bar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.trendView.setData(data);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TrendBarView trendView;

        ViewHolder(View itemView) {
            super(itemView);
            trendView = itemView.findViewById(R.id.trend_view);
        }
    }
}
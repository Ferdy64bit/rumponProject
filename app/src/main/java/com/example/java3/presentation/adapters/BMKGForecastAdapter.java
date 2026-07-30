package com.example.java3.presentation.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.java3.R;
import com.example.java3.databinding.ItemBmkgForecastBinding;
import com.example.java3.domain.model.BMKGForecast;

import java.util.List;

public class BMKGForecastAdapter extends RecyclerView.Adapter<BMKGForecastAdapter.ViewHolder> {
    private final List<BMKGForecast> items;

    public BMKGForecastAdapter(List<BMKGForecast> items) {
        this.items = items;
    }

    public void updateData(List<BMKGForecast> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBmkgForecastBinding binding = ItemBmkgForecastBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemBmkgForecastBinding binding;

        ViewHolder(ItemBmkgForecastBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BMKGForecast forecast) {
            binding.tvForecastDate.setText(forecast.getDisplayDate());
            binding.tvForecastWeather.setText(forecast.getWeatherLabel());
            binding.tvForecastWave.setText(forecast.getWaveLabel());
            binding.tvForecastWind.setText(forecast.getWindLabel());
            binding.tvForecastWarning.setText(forecast.hasWarning() ? forecast.getWarning() : "Warning nihil");

            String label = forecast.hasWarning() ? "Waspada" : "Prakiraan";
            binding.tvForecastBadge.setText(label);

            int color = forecast.hasWarning() ? R.color.warning : R.color.primary;
            binding.tvForecastBadge.getBackground().setTint(ContextCompat.getColor(itemView.getContext(), color));
        }
    }
}

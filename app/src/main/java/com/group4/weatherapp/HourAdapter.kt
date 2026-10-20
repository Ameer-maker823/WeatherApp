package com.group4.weatherapp

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.group4.weatherapp.databinding.ItemHourBinding

class HourAdapter : ListAdapter<HourForecast, HourAdapter.HourViewHolder>(DiffCallback()) {

    class HourViewHolder(private val binding: ItemHourBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(hour: HourForecast) {
            binding.tvHour.text = hour.label
            binding.tvHourTemp.text = "${hour.temp}°"
            binding.ivHourIcon.setImageResource(hour.iconRes)
            binding.ivHourIcon.imageTintList = ColorStateList.valueOf(hour.tint)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourViewHolder {
        val binding = ItemHourBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HourViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<HourForecast>() {
        override fun areItemsTheSame(old: HourForecast, new: HourForecast) = old.label == new.label
        override fun areContentsTheSame(old: HourForecast, new: HourForecast) = old == new
    }
}
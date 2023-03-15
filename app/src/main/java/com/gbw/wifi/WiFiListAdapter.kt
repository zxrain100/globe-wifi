package com.gbw.wifi

import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gbw.wifi.databinding.WifiListItemBinding

class WiFiListAdapter : ListAdapter<ScanResult, WiFiListAdapter.ItemHolder>(ScanResultDiffCallback) {

    class ItemHolder(val binding: WifiListItemBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = WifiListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item = getItem(position)
        holder.binding.itemLabel.text = item.SSID
        holder.binding.root.setOnClickListener {
            l?.invoke(item)
        }
    }


    private var l: ((item: ScanResult) -> Unit)? = null
    fun setOnItemClickListener(l: (item: ScanResult) -> Unit) {
        this.l = l
    }


    object ScanResultDiffCallback : DiffUtil.ItemCallback<ScanResult>() {
        override fun areItemsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
            return oldItem.BSSID == newItem.BSSID
        }
    }


}
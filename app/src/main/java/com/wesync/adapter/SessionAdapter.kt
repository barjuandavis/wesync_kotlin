package com.wesync.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wesync.connection.DiscoveredEndpoint
import com.wesync.databinding.CardViewBinding

class SessionAdapter(private val clickListener: SessionClickListener):
    ListAdapter<DiscoveredEndpoint, SessionAdapter.CardViewHolder>(SessionDiffCallback()) {

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        Log.d("bindViewHolder","bounding session $position to view")
        holder.bind(getItem(position)!!,clickListener)
    }

    override fun onCurrentListChanged(
        previousList: MutableList<DiscoveredEndpoint>,
        currentList: MutableList<DiscoveredEndpoint>
    ) {
       for (i in currentList) {
           Log.d("currentListChanged","${i.endpointId}: ${i.info.endpointName}")
       }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return CardViewHolder.from(parent)
    }

    class CardViewHolder private constructor(
        private val binding: CardViewBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(discoveredEndpoint: DiscoveredEndpoint, clickListener: SessionClickListener) {
            binding.discoveredEndpoint = discoveredEndpoint
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): CardViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CardViewBinding
                    .inflate(layoutInflater, parent, false)
                return CardViewHolder(binding)
            }
        }
    }

}

class SessionDiffCallback : DiffUtil.ItemCallback<DiscoveredEndpoint> () {
    override fun areItemsTheSame(oldItem: DiscoveredEndpoint, newItem: DiscoveredEndpoint): Boolean {
        return oldItem.endpointId == newItem.endpointId
    }
    override fun areContentsTheSame(oldItem: DiscoveredEndpoint, newItem: DiscoveredEndpoint): Boolean {
        return oldItem == newItem
    }
}

class SessionClickListener(val clickListener: (discoveredEndpoint: DiscoveredEndpoint) -> Unit) {
    fun onClick(discoveredEndpoint: DiscoveredEndpoint) = clickListener(discoveredEndpoint)
}




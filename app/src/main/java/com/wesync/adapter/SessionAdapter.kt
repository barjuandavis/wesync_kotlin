package com.wesync.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wesync.connection.Endpoint
import com.wesync.databinding.CardViewBinding

class SessionAdapter(private val clickListener: SessionClickListener):
    ListAdapter<Endpoint, SessionAdapter.CardViewHolder>(SessionDiffCallback()) {

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        Log.d("bindViewHolder","bounding session $position to view")
        holder.bind(getItem(position)!!,clickListener)
    }

    override fun onCurrentListChanged(
        previousList: MutableList<Endpoint>,
        currentList: MutableList<Endpoint>
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

        fun bind(endpoint: Endpoint, clickListener: SessionClickListener) {
            binding.endpoint = endpoint
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

class SessionDiffCallback : DiffUtil.ItemCallback<Endpoint> () {
    override fun areItemsTheSame(oldItem: Endpoint, newItem: Endpoint): Boolean {
        return oldItem.endpointId == newItem.endpointId
    }
    override fun areContentsTheSame(oldItem: Endpoint, newItem: Endpoint): Boolean {
        return oldItem == newItem
    }
}

class SessionClickListener(val clickListener: (endpoint: Endpoint) -> Unit) {
    fun onClick(endpoint: Endpoint) = clickListener(endpoint)
}




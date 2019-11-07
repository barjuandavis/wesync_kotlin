package com.wesync.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wesync.R
import com.wesync.connection.Endpoint
import com.wesync.databinding.CardViewBinding
import com.wesync.ui.connection.ConnectionViewModel

class SessionAdapter(private val viewmodel: ConnectionViewModel):
    ListAdapter<Endpoint, SessionAdapter.CardViewHolder>(SessionDiffCallback()) {

    inner class CardViewHolder internal constructor(val binding: CardViewBinding)
        : RecyclerView.ViewHolder(binding.root) {}

    private var layoutId = 0
    var sessions = mutableListOf<Endpoint>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): CardViewHolder {
        val binding =
            DataBindingUtil.inflate<CardViewBinding>(LayoutInflater.from(viewGroup.context),
            R.layout.card_view,viewGroup,false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        Log.d("bindViewHolder","bounding session$position to view")
        holder.binding.sessionName.text = sessions[position].endpointId
    }


}

class SessionDiffCallback : DiffUtil.ItemCallback<Endpoint> () {
    override fun areItemsTheSame(oldItem: Endpoint, newItem: Endpoint): Boolean {
        return oldItem == newItem
    }
    override fun areContentsTheSame(oldItem: Endpoint, newItem: Endpoint): Boolean {
        return oldItem == newItem
    }
}




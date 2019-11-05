package com.wesync.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.wesync.R
import com.wesync.connection.Endpoint
import com.wesync.databinding.CardViewBinding
import com.wesync.ui.connection.ConnectionViewModel
import java.lang.Exception


class CardViewHolder(val binding: CardViewBinding)
    : RecyclerView.ViewHolder(binding.root)

class SessionAdapter: RecyclerView.Adapter<CardViewHolder>() {

    private var layoutId = 0
    var sessions = listOf<Endpoint>()
    private lateinit var viewmodel: ConnectionViewModel
    override fun getItemCount() = sessions.size

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int):
            CardViewHolder {
        val binding =
            DataBindingUtil.inflate<CardViewBinding>(
                LayoutInflater.from(viewGroup.context),
            R.layout.card_view,viewGroup,false)
        return CardViewHolder(binding)
    }
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.binding.sessionName.text = sessions[position].toString()
    }
}



package com.wesync.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.wesync.R
import com.wesync.databinding.CardViewBinding
import com.wesync.ui.connection.ConnectionViewModel
import java.lang.Exception


class CardViewHolder(val binding: CardViewBinding)
    : RecyclerView.ViewHolder(binding.root)

class SessionAdapter: RecyclerView.Adapter<CardViewHolder>() {

    private var layoutId = 0
    lateinit var sessions : LiveData<List<String>>
    private lateinit var viewmodel: ConnectionViewModel

    override fun getItemCount(): Int {
        return try {sessions.value!!.size} catch (e:Exception) {0}
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int):
            CardViewHolder {
        val binding =
            DataBindingUtil.inflate<CardViewBinding>(
                LayoutInflater.from(viewGroup.context),
            R.layout.card_view,viewGroup,false)
        return CardViewHolder(binding)
    }
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        /*
        TODO: get current session, and then set the current
         CardViewBinding with the corresponding Session Maker ("Music Director")
        */
        holder.binding.sessionName.text = sessions.value!![position]
    }
}


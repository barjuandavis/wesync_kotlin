package com.wesync.ui.main

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.wesync.MetronomeService
import com.wesync.R

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var v:View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.v = inflater.inflate(R.layout.main_fragment, container, false)
        viewModel = MainViewModel(this.activity!!.application)
        return this.v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        val button = this.v.findViewById<Button>(R.id.play_button)
        button.setOnClickListener { onPlayClicked() }
    }

    private fun onPlayClicked() {
        viewModel.onPlayButtonPressed()
    }
}

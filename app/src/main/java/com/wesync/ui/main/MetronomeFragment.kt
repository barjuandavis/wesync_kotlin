package com.wesync.ui.main

import android.content.ServiceConnection
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.wesync.MetronomeService
import com.wesync.R

class MetronomeFragment : Fragment() {

    companion object {
        fun newInstance() = MetronomeFragment()
    }

    private lateinit var viewModel: MetronomeViewModel
    private lateinit var v:View
    private lateinit var metronomeConnnection: ServiceConnection

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.v = inflater.inflate(R.layout.main_fragment, container, false)
        viewModel = MetronomeViewModel()

        return this.v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MetronomeViewModel::class.java)
        val button = this.v.findViewById<Button>(R.id.play_button)
        button.setOnClickListener { onPlayClicked() }
    }

    private fun onPlayClicked() {

    }
}

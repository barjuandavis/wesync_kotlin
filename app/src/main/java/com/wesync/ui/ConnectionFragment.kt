package com.wesync.ui

import android.app.AlertDialog
import androidx.lifecycle.Observer
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration

import com.wesync.R
import com.wesync.MainViewModel
import com.wesync.adapter.SessionAdapter
import com.wesync.adapter.SessionClickListener
import com.wesync.databinding.ConnectionFragmentBinding
import kotlinx.android.synthetic.main.metronome_fragment.*


class ConnectionFragment : Fragment() {

    private lateinit var mainViewModel      : MainViewModel
    private lateinit var binding            : ConnectionFragmentBinding
    private lateinit var sessionAdapter     : SessionAdapter
    private lateinit var toolbar            : ActionBar
    private var discovering = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.connection_fragment,container,false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.let {
            mainViewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)
            binding.viewmodel = mainViewModel
            binding.lifecycleOwner = this
            mainViewModel.subscriber.subscribe()
            it.lifecycle.addObserver(mainViewModel)
            mainViewModel.currentFragment.value = 2
            toolbar = (it as AppCompatActivity).supportActionBar!!
            subscribeToViewModel()
        }
    }
    override fun onResume() {
        mainViewModel.currentFragment.value = 2
        super.onResume()
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val builder = AlertDialog.Builder(context)

        sessionAdapter = SessionAdapter(
            SessionClickListener {
                builder.setTitle("What is your Name?")
                val input = EditText(context)
                input.inputType = InputType.TYPE_CLASS_TEXT
                builder.setView(input)
                builder.setPositiveButton("OK") { _, _ ->
                    mainViewModel.onJoinSession(input.text?.toString(),it)
                    findNavController().navigate(
                        ConnectionFragmentDirections
                            .actionConnectionFragmentToMetronomeFragment())
                }
                builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                builder.show()
        })
        binding.recyclerView.adapter = sessionAdapter
        binding.recyclerView.apply {
            addItemDecoration(DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL))
        }
        mainViewModel.subscriber.connServiceConnected.observe(this, Observer {
            if (!discovering && it) {
                mainViewModel.startDiscovery()
            }
        })
        mainViewModel.isDiscovering.observe(this, Observer {
            discovering = it
        })
    }
    override fun onDestroy() {
        mainViewModel.stopDiscovery()
        super.onDestroy()
    }
    private fun subscribeToViewModel() {
       mainViewModel.foundSessions.observe(this, Observer {
           if (it.isNotEmpty()) {
               sessionAdapter.submitList(it)
               mainViewModel.stopDiscovery()
           }
       })
    }



}

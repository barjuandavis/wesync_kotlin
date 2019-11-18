package com.wesync.ui



import android.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration

import com.wesync.R
import com.wesync.MainViewModel
import com.wesync.adapter.SessionAdapter
import com.wesync.adapter.SessionClickListener
import com.wesync.connection.ConnectionManagerService
import com.wesync.databinding.ConnectionFragmentBinding
import com.wesync.util.service.ServiceSubscriber
import java.lang.NullPointerException


class ConnectionFragment : Fragment() {

    private lateinit var mainViewModel    : MainViewModel
    private lateinit var binding            : ConnectionFragmentBinding
    private var mCService                   : ConnectionManagerService? = null
    private lateinit var subscriber         : ServiceSubscriber
    private lateinit var sessionAdapter     : SessionAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        doBindService()
        binding = DataBindingUtil.inflate(inflater,R.layout.connection_fragment,container,false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.let {
            mainViewModel = ViewModelProviders.of(it,
                SavedStateViewModelFactory(it.application,it)
            ).get(MainViewModel::class.java)
            binding.viewmodel = mainViewModel
        }
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
                    mainViewModel.onJoinSession(input.text?.toString())
                    mCService?.connect(it,mainViewModel.getSessionName())
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
    }

    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()
    }

    private fun subscribeToService() {
       mCService?.endpoints?.observe(this, Observer {
            it.let { if (it.isNotEmpty()) { sessionAdapter.submitList(it) } } })
    }

    private fun doBindService() {
        try {
            subscriber =
                ServiceSubscriber(activity!!.applicationContext, activity)
            subscriber.connServiceConnected.observe(this, Observer {
                if (it) mCService = subscriber.connectionService
                subscribeToService()
                startDiscovery()
            })
            subscriber.subscribe()
        } catch (e: NullPointerException) {}
    }

    private fun doUnbindService() {
       subscriber.unsubscribe()
       mCService?.stopDiscovering()
       mCService = null
    }

    private fun startDiscovery() {
        mCService?.startDiscovery()
    }


}

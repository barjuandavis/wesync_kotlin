package com.wesync.ui.connection


import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration


import com.wesync.R
import com.wesync.SharedViewModel
import com.wesync.adapter.SessionAdapter
import com.wesync.adapter.SessionClickListener
import com.wesync.connection.ConnectionManagerService
import com.wesync.databinding.ConnectionFragmentBinding
import com.wesync.metronome.MetronomeService
import com.wesync.util.ServiceSubscriber
import com.wesync.util.UserTypes
import java.lang.NullPointerException


class ConnectionFragment : Fragment() {

    private lateinit var sharedViewModel    : SharedViewModel
    private lateinit var viewModel          : ConnectionViewModel
    private lateinit var binding            : ConnectionFragmentBinding
    private val userType                    = UserTypes.SLAVE
    private var mCService                   : ConnectionManagerService? = null
    private var mService                    : MetronomeService? = null
    private lateinit var subscriber         : ServiceSubscriber
    private val args                        : ConnectionFragmentArgs by navArgs()
    private lateinit var sessionAdapter     : SessionAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.connection_fragment,container,false)
        viewModel = ViewModelProviders.of(this)
            .get(ConnectionViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.let {
            sharedViewModel = ViewModelProviders.of(it).get(SharedViewModel::class.java)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        doBindService()
        sessionAdapter = SessionAdapter(SessionClickListener {
            Toast.makeText(this.context,"${it.endpointId} clicked",Toast.LENGTH_SHORT).show()
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

    private fun subscribeToViewModel() {
        subscribeToConnectionManagerService() //also listen changes of ConnectionManagerService's found Endpoints
    }

    private fun subscribeToConnectionManagerService() {
        mCService?.endpoints?.observe(this, Observer {
            it.let {
                sessionAdapter.submitList(it)
                Log.d("submitList","list submitted!")
            }
        })
    }

    private fun doBindService() {
        try {
            subscriber = ServiceSubscriber(activity!!.applicationContext, activity)
            subscriber.connServiceConnected.observe(this, Observer {
                if (it) mCService = subscriber.connectionService
                subscribeToConnectionManagerService()
                startDiscovery()
            })
            subscriber.metronomeConnected.observe(this, Observer {
                if (it) mService = subscriber.metronomeService
            })
            subscriber.subscribe()
        } catch (e: NullPointerException) {}
        subscribeToViewModel()
    }

    private fun doUnbindService() {
       subscriber.unsubscribe()
       mCService?.stopDiscovering()
       mService = null
       mCService = null
    }

    private fun startDiscovery() {
        mCService?.startDiscovery()
    }


}

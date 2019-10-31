package com.wesync.ui.connection


import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager


import com.wesync.R
import com.wesync.SharedViewModel
import com.wesync.adapter.SessionAdapter
import com.wesync.connection.ConnectionManagerService
import com.wesync.databinding.ConnectionFragmentBinding
import com.wesync.metronome.MetronomeService
import com.wesync.util.ConnectionCodes
import com.wesync.util.ServiceSubscriber
import com.wesync.util.UserTypes
import java.lang.IllegalStateException
import java.lang.NullPointerException


class ConnectionFragment : Fragment() {

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var binding: ConnectionFragmentBinding
    private lateinit var userType: UserTypes
    private var mCService: ConnectionManagerService? = null
    private var mService: MetronomeService? = null
    private lateinit var subscriber: ServiceSubscriber
    private val args: ConnectionFragmentArgs by navArgs()
    private lateinit var sessionAdapter: SessionAdapter
    private val sessionObserver = Observer<List<String>> {
        sessionAdapter.sessions = viewModel.getAllSessions()
        try {binding.recyclerView.recycledViewPool.clear()
        sessionAdapter.notifyDataSetChanged()} catch (e:Exception) {}
    }
    private lateinit var viewModel: ConnectionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.connection_fragment,container,false)
        binding.viewmodel = viewModel
        val rv = binding.recyclerView
        rv.layoutManager = LinearLayoutManager(this.context)
        rv.setHasFixedSize(true)
        rv.adapter = sessionAdapter
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)
            .get(ConnectionViewModel::class.java)
        sessionAdapter = SessionAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.let {
            sharedViewModel = ViewModelProviders.of(it).get(SharedViewModel::class.java)
        }
        when (args.connectionType) {
            ConnectionCodes.NEW_SESSION.v -> {
                userType = UserTypes.SESSION_HOST
            }
            ConnectionCodes.JOIN_SESSION.v -> {
                userType = UserTypes.SLAVE
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        doBindService()
    }

    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()
    }

    private fun subscribeToViewModel() {
        getAllSessions()
    }

    private fun getAllSessions() {viewModel.getAllSessions().
        observe(this, sessionObserver)}

    private fun doBindService() {
        try {
            subscriber = ServiceSubscriber(activity!!.applicationContext, activity)
            subscriber.connServiceConnected.observe(this, Observer {
                if (it) mCService = subscriber.connectionService!!
            })
            subscriber.metronomeConnected.observe(this, Observer {
                if (it) mService = subscriber.metronomeService!!
            })
            subscriber.subscribe()
        } catch (e: NullPointerException) {}
        subscribeToViewModel()
    }

    private fun doUnbindService() {
       subscriber.unsubscribe()
       mService = null
       mCService = null
    }


}

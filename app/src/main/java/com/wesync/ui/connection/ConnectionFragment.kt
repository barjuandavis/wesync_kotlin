package com.wesync.ui.connection

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager


import com.wesync.R
import com.wesync.SharedViewModel
import com.wesync.adapter.SessionAdapter
import com.wesync.connection.ConnectionManagerService
import com.wesync.databinding.ConnectionFragmentBinding
import com.wesync.util.ConnectionCodes
import com.wesync.util.UserTypes



class ConnectionFragment : Fragment() {

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var binding: ConnectionFragmentBinding
    private var mBound: Boolean = false
    private lateinit var userType: UserTypes
    private lateinit var mService: ConnectionManagerService
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ConnectionManagerService.LocalBinder
            mService = binder.getService()
            mBound = true
            when (userType) {
                UserTypes.SESSION_DIRECTOR -> {
                    //mService.startAdvertising()
                    Toast.makeText(this@ConnectionFragment.context,"advertising!",Toast.LENGTH_SHORT).show()
                }
                UserTypes.SLAVE -> {
                    //mService.startDiscovery()
                    Toast.makeText(this@ConnectionFragment.context,"discovering!",Toast.LENGTH_SHORT).show()
                }
            }

        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }
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
                userType = UserTypes.SESSION_DIRECTOR
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
        observe(this.viewLifecycleOwner, sessionObserver)}

    private fun doBindService() {
        Intent(activity?.applicationContext, ConnectionManagerService::class.java).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            subscribeToViewModel()
        }
    }

    private fun doUnbindService() {
        activity!!.unbindService(connection)
        mBound = false
    }


}

package com.wesync.ui.connection

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.wesync.R
import com.wesync.SharedViewModel
import com.wesync.connection.ConnectionManagerService
import com.wesync.databinding.ConnectionFragmentBinding
import com.wesync.metronome.MetronomeService

class ConnectionFragment : Fragment() {

    private var sharedViewModel: SharedViewModel? = null
    private lateinit var binding: ConnectionFragmentBinding
    private var mBound: Boolean = false
    private lateinit var mService: ConnectionManagerService
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ConnectionManagerService.LocalBinder
            mService = binder.getService()
            mBound = true

        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }


    companion object {
        fun newInstance() = ConnectionFragment()
    }

    private lateinit var viewModel: ConnectionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.connection_fragment,container,false)
        binding.viewmodel = viewModel
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
    }

    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()
    }

    private fun subscribeToViewModel() {
        /*
        * TODO: figure out what to make at the viewmodel
        */
    }

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

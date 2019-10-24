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
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs

import com.wesync.R
import com.wesync.SharedViewModel
import com.wesync.connection.ConnectionManagerService
import com.wesync.databinding.ConnectionFragmentBinding
import com.wesync.metronome.MetronomeService
import com.wesync.ui.main.MetronomeFragment
import com.wesync.ui.main.MetronomeFragmentDirections
import com.wesync.util.ConnectionCodes
import java.lang.Exception

class ConnectionFragment : Fragment() {

    private lateinit var sharedViewModel: SharedViewModel
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
    val args: ConnectionFragmentArgs by navArgs()

    private lateinit var viewModel: ConnectionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.connection_fragment,container,false)
        binding.viewmodel = viewModel
        return null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.let {
            sharedViewModel = ViewModelProviders.of(it).get(SharedViewModel::class.java)
        }
        when (args.connectionType) {
            ConnectionCodes.NEW_SESSION.v -> {
                try {mService.startAdvertising()}
                catch(e:Exception) {
                    Toast.makeText(this.context,"preparing",
                        Toast.LENGTH_SHORT).show()
                }
            }
            ConnectionCodes.JOIN_SESSION.v -> {
                try {mService.startAdvertising()}
                catch(e:Exception) {
                    Toast.makeText(this.context,"preparing",
                        Toast.LENGTH_SHORT).show()
                }
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

package com.wesync.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.wesync.MetronomeConfig
import com.wesync.MetronomeService
import com.wesync.R
import com.wesync.databinding.MetronomeFragmentBinding

class MetronomeFragment : Fragment() {

    companion object {
        fun newInstance() = MetronomeFragment()
    }

    private lateinit var viewModel: MetronomeViewModel
    private lateinit var v:View
    private lateinit var binding:MetronomeFragmentBinding

    private var mBound: Boolean = false
    private lateinit var mService: MetronomeService

    private val changeObserver = Observer<String> {
        //make change on repo
        Log.d("changed","changed bpm to " + it)
        MetronomeConfig.bpm = it.toLong()
    }

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MetronomeService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

   override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.metronome_fragment,container,false)
        viewModel = MetronomeViewModel()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val button = binding.root.findViewById<Button>(R.id.play_button)
        binding.viewmodel = viewModel
        button.setOnClickListener { onPlayClicked() }
        doBindService()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
       viewModel.bpmString.observe(this,changeObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()
    }


    private fun onPlayClicked() {
        mService.onPlay()
    }

    private fun doBindService() {
        Intent(activity!!.applicationContext, MetronomeService::class.java).also { intent ->
            activity!!.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun doUnbindService() {
        activity!!.unbindService(connection)
        mBound = false
    }
}

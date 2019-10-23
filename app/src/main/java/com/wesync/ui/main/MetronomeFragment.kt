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
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.wesync.metronome.MetronomeService
import com.wesync.R
import com.wesync.SharedViewModel
import com.wesync.databinding.MetronomeFragmentBinding


class MetronomeFragment : Fragment() {

    companion object {
        fun newInstance() = MetronomeFragment()
    }

    private lateinit var viewModel: MetronomeViewModel
    private lateinit var v:View
    private lateinit var binding: MetronomeFragmentBinding
    private val sharedViewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)
    private var mBound: Boolean = false
    private lateinit var mService: MetronomeService
    private val playObserver = Observer<Boolean>() {
        try {
            Log.d("playObserver","playObserved:$it")
            if (it) {
                binding.playButton.setText(R.string.stop_button)
            } else binding.playButton.setText(R.string.play_button)
            mService.onPlay()
        }
        catch (e: Exception) {
           // Toast.makeText(this@MetronomeFragment.context, "Preparing...", Toast.LENGTH_SHORT).show() # for debug purposes
        }
    }
    private val bpmObserver = Observer<Long> {
        try {
            mService.onBPMChanged(it)
            sharedViewModel.config.postValue(it)
            binding.bpmTextView.text = "$it"
        }
        catch (e: Exception) { }
    }


    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
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
       binding.viewmodel = viewModel
       return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)
            .get(MetronomeViewModel::class.java)
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
        viewModel.bpm.observe(viewLifecycleOwner,bpmObserver)
        viewModel.isPlaying.observe(viewLifecycleOwner,playObserver)
    }

    private fun doBindService() {
        Intent(activity?.applicationContext, MetronomeService::class.java).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            subscribeToViewModel()
        }

    }

    private fun doUnbindService() {
        activity!!.unbindService(connection)
        mBound = false
    }


}

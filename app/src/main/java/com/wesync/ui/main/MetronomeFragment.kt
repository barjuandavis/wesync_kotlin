package com.wesync.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Resources
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.wesync.metronome.MetronomeService
import com.wesync.R
import com.wesync.SharedViewModel
import com.wesync.databinding.MetronomeFragmentBinding
import com.wesync.util.ConnectionCodes
import java.util.*


class MetronomeFragment : Fragment() {

    companion object {
        fun newInstance() = MetronomeFragment()
    }

    private lateinit var viewModel: MetronomeViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var binding: MetronomeFragmentBinding
    private var mBound: Boolean = false
    private lateinit var mService: MetronomeService
    private val playObserver = Observer<Boolean>() {
        try {
            Log.d("playObserver","playObserved:$it")
            if (it) {
                binding.playButton.setText(R.string.stop_button)
                binding.playButton.setBackgroundColor(resources.getColor(R.color.colorStop))
            } else {
                binding.playButton.setText(R.string.play_button)
                binding.playButton.setBackgroundColor(resources.getColor(R.color.colorPlay))
            }
            mService.onPlay()
        }
        catch (e: Exception) {
           Toast.makeText(this@MetronomeFragment.context, "Preparing...", Toast.LENGTH_SHORT).show()
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

    private class OnConnectionFragmentClickListener: View.OnClickListener {
        override fun onClick(v: View) {
            var args = 0
            when (v.id) {
                R.id.new_session -> args = ConnectionCodes.NEW_SESSION.v
                R.id.join_session -> args = ConnectionCodes.JOIN_SESSION.v
            }
            val action = MetronomeFragmentDirections.
                actionMetronomeFragmentToConnectionFragment()
            action.connectionType = args
            v.findNavController().navigate(action)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = DataBindingUtil.inflate(inflater,R.layout.metronome_fragment,container,false)
       binding.viewmodel = viewModel
       return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this.requireActivity())
            .get(MetronomeViewModel::class.java)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.let {
            sharedViewModel = ViewModelProviders.of(it).get(SharedViewModel::class.java)
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d("activity","activity${activity.toString()}")
        doBindService()
    }
    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()
    }

    private fun doBindService() {
        Intent(activity?.applicationContext, MetronomeService::class.java).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
          subscribeToViewModel()
        }
    }
    private fun subscribeToViewModel() {
        viewModel.bpm.observe(this,bpmObserver)
        viewModel.isPlaying.observe(this,playObserver)
        binding.newSession.setOnClickListener(OnConnectionFragmentClickListener())
        binding.joinSession.setOnClickListener(OnConnectionFragmentClickListener())
    }

    private fun doUnbindService() {
        activity!!.unbindService(connection)
        mBound = false
    }






}

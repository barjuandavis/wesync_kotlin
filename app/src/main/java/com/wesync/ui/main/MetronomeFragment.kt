package com.wesync.ui.main

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
import androidx.lifecycle.Observer
import com.wesync.metronome.MetronomeService
import com.wesync.R
import com.wesync.databinding.MetronomeFragmentBinding


class MetronomeFragment : Fragment() {

    companion object {
        fun newInstance() = MetronomeFragment()
    }

    private lateinit var viewModel: MetronomeViewModel
    private lateinit var v:View
    private lateinit var binding: MetronomeFragmentBinding

    private var mBound: Boolean = false
    private lateinit var mService: MetronomeService
    private lateinit var playObserver: Observer<Boolean>
    private lateinit var bpmObserver : Observer<Long>


    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MetronomeService.LocalBinder
            mService = binder.getService()
            playObserver = Observer {
                if (mService.isPlaying()) {

                    binding.playButton.setText(R.string.stop_button)
                }
                else binding.playButton.setText(R.string.play_button)
                mService.onPlay()
            }
            bpmObserver = Observer {
                mService.onBPMChanged(it)
                binding.bpmTextView.text = "$it"
            }
            subscribeToViewModel()
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
        viewModel.isPlaying.observe(viewLifecycleOwner,playObserver)
        viewModel.bpm.observe(viewLifecycleOwner,bpmObserver)
    }

    private fun doBindService() {
        Intent(activity?.applicationContext, MetronomeService::class.java).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

    }

    private fun doUnbindService() {
        activity!!.unbindService(connection)
        mBound = false
    }
}

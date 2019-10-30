package com.wesync.ui.main


import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.wesync.metronome.MetronomeService
import com.wesync.R
import com.wesync.SharedViewModel
import com.wesync.connection.ConnectionManagerService
import com.wesync.databinding.MetronomeFragmentBinding
import com.wesync.util.ConnectionCodes
import com.wesync.util.ServiceSubscriber
import java.lang.IllegalStateException
import java.lang.NullPointerException


class MetronomeFragment : Fragment() {

    //TODO : IMPLEMENT TWO-WAY DATABINDING!!!!!111!!! PRIORITY FOR 30/10/2019

    companion object {
        fun newInstance() = MetronomeFragment()
    }

    private lateinit var viewModel: MetronomeViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var binding: MetronomeFragmentBinding
    private lateinit var subscriber: ServiceSubscriber
    private lateinit var mService: MetronomeService
    private lateinit var mCService: ConnectionManagerService
    private val bpmObserver = Observer<Long> {
        try {
            mService.onBPMChanged(it)
            sharedViewModel.config.value = it
            binding.bpmTextView.text = ""
            binding.bpmTextView.text = "$it"
        }
        catch (e: Exception) { }
    }

    inner class OnConnectionFragmentClickListener: View.OnClickListener {
        override fun onClick(v: View) {
            var args = 0
            when (v.id) {
                R.id.new_session -> {
                    //mCService.startAdvertising()
                   // binding.newSession.visibility = View.GONE
                  //  binding.joinSession.visibility = View.GONE
                  //  binding.notification.text = "You are leading a new Session."
                }
                R.id.join_session -> {
                    args = ConnectionCodes.JOIN_SESSION.v
                    val action = MetronomeFragmentDirections.
                        actionMetronomeFragmentToConnectionFragment()
                    action.connectionType = args
                    v.findNavController().navigate(action)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = DataBindingUtil.inflate(inflater,R.layout.metronome_fragment,container,false)
       binding.viewmodel = viewModel
       return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)
            .get(MetronomeViewModel::class.java)
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
        } catch (e: NullPointerException) {
            Log.d("ServiceMightBeNull","NullPointerException thrown. Service might be null.")
        } catch (e: IllegalStateException) {
            Log.d("IllegalState_found","IllegalStateException thrown. Eh?")
        }
        subscribeToViewModel()
    }
    private fun subscribeToViewModel() {
        viewModel.bpm.observe(this, bpmObserver)
        viewModel.isPlaying.observe( this, Observer<Boolean> {
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
        } catch (e: Exception) {
            Log.d("playObserver", "Preparing...")
        }})
        binding.newSession.setOnClickListener(OnConnectionFragmentClickListener())
        binding.joinSession.setOnClickListener(OnConnectionFragmentClickListener())
    }

    private fun doUnbindService() {
       subscriber.unsubscribe()
    }
}

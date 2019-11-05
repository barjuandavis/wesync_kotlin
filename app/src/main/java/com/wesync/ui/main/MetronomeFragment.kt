package com.wesync.ui.main


import android.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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






class MetronomeFragment : Fragment() {

    private lateinit var viewModel: MetronomeViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var binding: MetronomeFragmentBinding
    private lateinit var subscriber: ServiceSubscriber
    private var mService: MetronomeService? = null
    private var mCService: ConnectionManagerService? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = DataBindingUtil.inflate(inflater,R.layout.metronome_fragment,container,false)
        viewModel = ViewModelProviders.of(this)
            .get(MetronomeViewModel::class.java)
       binding.setLifecycleOwner { this.lifecycle }
       binding.viewmodel = viewModel
       return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("onViewCreated","MetronomeFragment Created!")
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
        } catch (e: Exception) {}
        subscribeToViewModel()
    }
    private fun subscribeToViewModel() {
        viewModel.bpm.observe(this, Observer<Long>{
            try {
                mService?.onBPMChanged(it)
                sharedViewModel.config.value = it
            }
            catch (e: Exception) {}
        })
        viewModel.isPlaying.observe( this, Observer<Boolean> { try {
            Log.d("playObserver","playObserved:$it")
            if (it) {
                binding.playButton.setText(R.string.stop_button)
                binding.playButton.setBackgroundColor(resources.getColor(R.color.colorStop))
            } else {
                binding.playButton.setText(R.string.play_button)
                binding.playButton.setBackgroundColor(resources.getColor(R.color.colorPlay))
            }
            mService?.onPlay()
        } catch (e: Exception) {
            Log.d("playObserver", "Preparing...")
        }})
        binding.newSession.setOnClickListener(OnConnectionFragmentClickListener())
        binding.joinSession.setOnClickListener(OnConnectionFragmentClickListener())
    }

    inner class OnConnectionFragmentClickListener: View.OnClickListener {
        override fun onClick(v: View) {
            val args: Int
            when (v.id) {
                R.id.new_session -> {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("What is your Name?")
                    val input = EditText(context)
                    input.inputType = InputType.TYPE_CLASS_TEXT
                    builder.setView(input)
                    builder.setPositiveButton("OK") { _, _ ->
                        viewModel.onNewSessionClicked(input.text?.toString())
                        binding.notification.text = "This is ${viewModel.getSessionName()}'s Session."
                        binding.newSession.visibility = View.GONE
                        binding.joinSession.visibility = View.GONE
                        if (viewModel.isPlaying.value!!) viewModel.onPlayClicked()
                    }
                    builder.setNegativeButton("Cancel") {dialog,_ ->dialog.cancel() }
                    builder.show()
                    //mCService.startAdvertising()
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

    private fun doUnbindService() {
       subscriber.unsubscribe()
    }
}

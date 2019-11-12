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
import com.wesync.util.UserTypes

class MetronomeFragment : Fragment() {

    private lateinit var viewModel          : MetronomeViewModel
    private lateinit var sharedViewModel    : SharedViewModel
    private lateinit var binding            : MetronomeFragmentBinding
    private lateinit var subscriber         : ServiceSubscriber
    private var mService                    : MetronomeService? = null
    private var mCService                   : ConnectionManagerService? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = DataBindingUtil.inflate(inflater,R.layout.metronome_fragment,container,false)
        viewModel = ViewModelProviders.of(this).get(MetronomeViewModel::class.java)
        binding.lifecycleOwner = this.viewLifecycleOwner
       binding.viewmodel = viewModel
       return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.let {
            sharedViewModel = ViewModelProviders.of(it).get(SharedViewModel::class.java)
            binding.sharedvm = sharedViewModel
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        doBindService()
    }
    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()
        if (!viewModel.isPlaying.value!!) {
            Log.d("shouldbestopped","should be stopped")
        }
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
        viewModel.bpm.observe(viewLifecycleOwner, Observer {
            mService?.onBPMChanged(it)
            sharedViewModel.config.value = it
        })
        viewModel.isPlaying.observe( viewLifecycleOwner, Observer { mService?.onPlay()})
        sharedViewModel.userType.observe(viewLifecycleOwner, Observer {
            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            when (it) {
                UserTypes.SOLO -> {
                    if (sharedViewModel.isAdvertising.value == true) sharedViewModel.toggleAdvertise()
                }
                UserTypes.SESSION_HOST -> {
                    if (sharedViewModel.isAdvertising.value == false) sharedViewModel.toggleAdvertise()
                }
                UserTypes.SLAVE -> TODO("lupa mau ngapain")
            }
        })
        sharedViewModel.isAdvertising.observe(viewLifecycleOwner, Observer {
            if (sharedViewModel.userType.value == UserTypes.SESSION_HOST) {
                if (it) mCService?.startAdvertising(sharedViewModel.getSessionName())
                else mCService?.stopAdvertising()
            }
        })
        binding.newSession.setOnClickListener(OnConnectionFragmentClickListener())
        binding.joinSession.setOnClickListener(OnConnectionFragmentClickListener())
    }

    inner class OnConnectionFragmentClickListener: View.OnClickListener {
        override fun onClick(v: View) {
            val args: Int
            when (v.id) {
                R.id.new_session -> {
                     onNewSessionButtonClicked()
                }
                R.id.join_session -> {
                    onJoinSessionButtonClicked(v)
                }
            }
        }
    }

    private fun doUnbindService() {
       subscriber.unsubscribe()
    }

    private fun onNewSessionButtonClicked() {
        if (sharedViewModel.userType.value == UserTypes.SOLO) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("What is your Name?")
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)
            builder.setPositiveButton("OK") { dialog, i ->
                sharedViewModel.onNewSession(input.text?.toString()) //TODO: PERHATIKANNNNN INIII
                if (viewModel.isPlaying.value!!) viewModel.onPlayClicked()
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        }
        else if (sharedViewModel.userType.value == UserTypes.SESSION_HOST) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Are you sure want to end this Session?")
            builder.setPositiveButton("OK") { _, _ ->
                sharedViewModel.endSession() //TODO: PERHATIKANNNNN INIII
                if (viewModel.isPlaying.value!!) viewModel.onPlayClicked()
                if (sharedViewModel.isAdvertising.value!!) sharedViewModel.toggleAdvertise()
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        }
    }
    private fun onJoinSessionButtonClicked(v: View?) {
        if (sharedViewModel.userType.value == UserTypes.SOLO) {
            val args: Int = ConnectionCodes.JOIN_SESSION.v
            val action = MetronomeFragmentDirections.
                actionMetronomeFragmentToConnectionFragment()
            action.connectionType = args
            v!!.findNavController().navigate(action)
        }
        else if (sharedViewModel.userType.value == UserTypes.SESSION_HOST) {
            sharedViewModel.toggleAdvertise()
        }
    }
}

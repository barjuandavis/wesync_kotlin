package com.wesync.ui


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
import com.wesync.ui.MetronomeFragmentDirections
import com.wesync.util.ConnectionCodes
import com.wesync.util.ServiceSubscriber
import com.wesync.util.UserTypes

class MetronomeFragment : Fragment() {

    private lateinit var sharedViewModel    : SharedViewModel
    private lateinit var binding            : MetronomeFragmentBinding
    private lateinit var subscriber         : ServiceSubscriber
    private var mService                    : MetronomeService? = null
    private var mCService                   : ConnectionManagerService? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = DataBindingUtil.inflate(inflater,R.layout.metronome_fragment,container,false)
        binding.lifecycleOwner = this.viewLifecycleOwner
       return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        activity?.let{
            sharedViewModel = ViewModelProviders.of(it).get(SharedViewModel::class.java)
            binding.viewmodel = sharedViewModel
            sharedViewModel.unpackBundle(savedInstanceState)
            printLog()
        }
        doBindService()
    }

    private fun printLog() {
        Log.d("states","bpm::${sharedViewModel.bpm.value}")
        Log.d("states","isPlaying:${sharedViewModel.isPlaying.value}")
        Log.d("states","session: ${sharedViewModel.session.value}")
        Log.d("states","userType: ${sharedViewModel.userType.value}")
    }




    override fun onDestroy() {
        doUnbindService()
        super.onDestroy()
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
        sharedViewModel.bpm.observe(viewLifecycleOwner, Observer {
            mService?.onBPMChanged(it)
        })
        sharedViewModel.isPlaying.observe( viewLifecycleOwner, Observer { mService?.onPlay()})
        sharedViewModel.userType.observe(viewLifecycleOwner, Observer {
            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            when (it) {
                UserTypes.SOLO -> {
                    if (sharedViewModel.isAdvertising.value == true) sharedViewModel.toggleAdvertise()
                }
                UserTypes.SESSION_HOST -> {
                    if (sharedViewModel.isAdvertising.value == false) sharedViewModel.toggleAdvertise()
                }
                UserTypes.SLAVE -> {}
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
        val builder = AlertDialog.Builder(context)
        if (sharedViewModel.userType.value == UserTypes.SOLO) {
            builder.setTitle("What is your Name?")
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)
            builder.setPositiveButton("OK") { _, _ ->
                sharedViewModel.onNewSession(input.text?.toString()) //TODO: PERHATIKANNNNN INIII
                if (sharedViewModel.isPlaying.value!!) sharedViewModel.onPlayClicked()
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        }
        else  {
            var t = "Are you sure want to end this Session?"
            if (sharedViewModel.userType.value == UserTypes.SLAVE)
                t = "Are you sure want to quit this Session?"
            builder.setTitle(t)
            builder.setPositiveButton("OK") { _, _ ->
                sharedViewModel.endSession() //TODO: PERHATIKANNNNN INIII
                if (sharedViewModel.isPlaying.value!!) sharedViewModel.onPlayClicked()
                if (sharedViewModel.isAdvertising.value!!) sharedViewModel.toggleAdvertise()
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        }
    }
    private fun onJoinSessionButtonClicked(v: View?) {
        if (sharedViewModel.userType.value == UserTypes.SOLO) {
            val args: Int = ConnectionCodes.JOIN_SESSION.v
            val action =
                MetronomeFragmentDirections.actionMetronomeFragmentToConnectionFragment()
            action.connectionType = args
            v!!.findNavController().navigate(action)
        }
        else if (sharedViewModel.userType.value == UserTypes.SESSION_HOST) {
            sharedViewModel.toggleAdvertise()
        }
    }
}
